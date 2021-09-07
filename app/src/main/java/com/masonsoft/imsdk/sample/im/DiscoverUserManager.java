package com.masonsoft.imsdk.sample.im;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.core.IMMessageQueueManager;
import com.masonsoft.imsdk.core.message.SessionProtoByteMessageWrapper;
import com.masonsoft.imsdk.core.processor.ReceivedProtoMessageNotNullProcessor;
import com.masonsoft.imsdk.core.proto.ProtoMessage;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.observable.DiscoverUserObservable;
import com.masonsoft.imsdk.util.Objects;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.core.Singleton;
import io.github.idonans.core.thread.TaskQueue;

/**
 * 记录并同步 '探索' 中的用户列表
 */
public class DiscoverUserManager {

    private static final Singleton<DiscoverUserManager> INSTANCE = new Singleton<DiscoverUserManager>() {
        @Override
        protected DiscoverUserManager create() {
            return new DiscoverUserManager();
        }
    };

    public static DiscoverUserManager getInstance() {
        return INSTANCE.get();
    }

    private final TaskQueue mAddOrRemoveOnlineUserQueue = new TaskQueue(1);

    @NonNull
    private final List<Long> mOnlineUserList = new ArrayList<>();

    private DiscoverUserManager() {
        IMMessageQueueManager.getInstance()
                .getReceivedMessageProcessor()
                .addFirstProcessor(new ProfileOnlineOfflineProcessor());
    }

    public void start() {
        SampleLog.v(Objects.defaultObjectTag(this) + " start");
    }

    @NonNull
    public List<Long> getOnlineUserList() {
        synchronized (mOnlineUserList) {
            return new ArrayList<>(mOnlineUserList);
        }
    }

    public void clearAllAsync() {
        mAddOrRemoveOnlineUserQueue.skipQueue();
        mAddOrRemoveOnlineUserQueue.enqueue(() -> {
            synchronized (mOnlineUserList) {
                mOnlineUserList.clear();
            }
        });
    }

    private void addOnlineAsync(ProtoMessage.ProfileOnline profileOnline) {
        mAddOrRemoveOnlineUserQueue.enqueue(() -> {
            final MSIMUserInfo.Editor userInfo = createUserInfo(profileOnline);
            MSIMManager.getInstance().getUserInfoManager().insertOrUpdateUserInfo(userInfo);
            addOnline(userInfo.getUserInfo().getUserId());
        });
    }

    private void addOnline(Long userId) {
        SampleLog.v(Objects.defaultObjectTag(this) + " addOnline userId:%s", userId);
        synchronized (mOnlineUserList) {
            if (!mOnlineUserList.contains(userId)) {
                mOnlineUserList.add(userId);
            }
        }

        DiscoverUserObservable.DEFAULT.notifyDiscoverUserOnline(userId);
    }

    private void removeOnlineAsync(long userId) {
        mAddOrRemoveOnlineUserQueue.enqueue(() -> removeOnline(userId));
    }

    private void removeOnline(Long userId) {
        SampleLog.v(Objects.defaultObjectTag(this) + " removeOnline userId:%s", userId);
        synchronized (mOnlineUserList) {
            mOnlineUserList.remove(userId);
        }

        DiscoverUserObservable.DEFAULT.notifyDiscoverUserOffline(userId);
    }

    private class ProfileOnlineOfflineProcessor extends ReceivedProtoMessageNotNullProcessor {

        @Override
        protected boolean doNotNullProcess(@NonNull SessionProtoByteMessageWrapper target) {
            final Object protoMessageObject = target.getProtoByteMessageWrapper().getProtoMessageObject();
            if (protoMessageObject == null) {
                return false;
            }

            if (protoMessageObject instanceof ProtoMessage.ProfileOnline) {
                addOnlineAsync((ProtoMessage.ProfileOnline) protoMessageObject);
                return true;
            }

            if (protoMessageObject instanceof ProtoMessage.UsrOffline) {
                removeOnlineAsync(((ProtoMessage.UsrOffline) protoMessageObject).getUid());
                return true;
            }

            return false;
        }

    }

    @NonNull
    private static MSIMUserInfo.Editor createUserInfo(@NonNull ProtoMessage.ProfileOnline input) {
        return new MSIMUserInfo.Editor(input.getUid())
                .setUpdateTimeMs(input.getUpdateTime() * 1000L /* 将服务器返回的秒转换为毫秒 */)
                .setNickname(input.getNickName())
                .setAvatar(input.getAvatar())
                .setGold(input.getGold())
                .setVerified(input.getVerified());
    }

}
