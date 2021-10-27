package com.masonsoft.imsdk.uikit.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMMessageListener;
import com.masonsoft.imsdk.MSIMMessageListenerProxy;
import com.masonsoft.imsdk.MSIMMessagePageContext;
import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.DisposableHolder;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class IMMessageChangedViewHelper {

    private final DisposableHolder mRequestHolder = new DisposableHolder();

    private long mSessionUserId = Long.MIN_VALUE / 2;
    private int mConversationType = Integer.MIN_VALUE / 2;
    private long mTargetUserId = Long.MIN_VALUE / 2;
    private long mLocalMessageId = Long.MIN_VALUE / 2;

    public IMMessageChangedViewHelper() {
        MSIMManager.getInstance().getMessageManager().addMessageListener(
                MSIMMessagePageContext.GLOBAL,
                mMessageListener
        );
    }

    public void setMessage(@NonNull MSIMMessage message) {
        setMessage(message.getSessionUserId(),
                message.getConversationType(),
                message.getTargetUserId(),
                message.getMessageId());
    }

    public void setMessage(long sessionUserId, int conversationType, long targetUserId, long localMessageId) {
        if (mSessionUserId != sessionUserId
                || mConversationType != conversationType
                || mTargetUserId != targetUserId
                || mLocalMessageId != localMessageId) {
            mSessionUserId = sessionUserId;
            mConversationType = conversationType;
            mTargetUserId = targetUserId;
            mLocalMessageId = localMessageId;
            requestLoadData(true);
        }
    }

    public String getDebugString() {
        //noinspection StringBufferReplaceableByString
        final StringBuilder builder = new StringBuilder();
        builder.append(Objects.defaultObjectTag(this));
        builder.append(" sessionUserId:").append(this.mSessionUserId);
        builder.append(" conversationType:").append(this.mConversationType);
        builder.append(" targetUserId:").append(this.mTargetUserId);
        builder.append(" localMessageId:").append(this.mLocalMessageId);
        return builder.toString();
    }

    public long getSessionUserId() {
        return mSessionUserId;
    }

    public int getConversationType() {
        return mConversationType;
    }

    public long getTargetUserId() {
        return mTargetUserId;
    }

    public long getLocalMessageId() {
        return mLocalMessageId;
    }

    @UiThread
    public void requestLoadData(boolean reset) {
        Preconditions.checkArgument(Threads.isUi());

        // abort last
        mRequestHolder.set(null);

        if (reset) {
            onMessageChanged(null, null);
        }
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    final MSIMMessage message = MSIMManager.getInstance().getMessageManager().getMessage(
                            mSessionUserId,
                            mConversationType,
                            mTargetUserId,
                            mLocalMessageId
                    );
                    return new ObjectWrapper(message);
                })
                .map(input -> {
                    final Object customObject = loadCustomObject();
                    return Pair.create(input, customObject);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Preconditions.checkNotNull(pair.first);
                    onMessageChanged((MSIMMessage) pair.first.getObject(), pair.second);
                }, MSIMUikitLog::e));
    }

    @Nullable
    @WorkerThread
    protected Object loadCustomObject() {
        return null;
    }

    protected abstract void onMessageChanged(@Nullable MSIMMessage message, @Nullable Object customObject);

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMMessageListener mMessageListener = new MSIMMessageListenerProxy(new MSIMMessageListener() {
        private boolean notMatch(long sessionUserId, int conversationType, long targetUserId, long localMessageId) {
            return !MSIMConstants.isIdMatch(mSessionUserId, sessionUserId)
                    || !MSIMConstants.isIdMatch(mConversationType, conversationType)
                    || !MSIMConstants.isIdMatch(mTargetUserId, targetUserId)
                    || !MSIMConstants.isIdMatch(mLocalMessageId, localMessageId);
        }

        @Override
        public void onMessageChanged(long sessionUserId, int conversationType, long targetUserId, long localMessageId) {
            onMessageChangedInternal(sessionUserId, conversationType, targetUserId, localMessageId);
        }

        private void onMessageChangedInternal(long sessionUserId, int conversationType, long targetUserId, long localMessageId) {
            if (notMatch(sessionUserId, conversationType, targetUserId, localMessageId)) {
                return;
            }

            Threads.postUi(() -> {
                if (notMatch(sessionUserId, conversationType, targetUserId, localMessageId)) {
                    return;
                }
                requestLoadData(false);
            });
        }
    });

}
