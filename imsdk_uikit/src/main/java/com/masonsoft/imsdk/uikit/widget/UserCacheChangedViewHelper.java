package com.masonsoft.imsdk.uikit.widget;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.MSIMUserInfoListener;
import com.masonsoft.imsdk.MSIMUserInfoListenerProxy;
import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.DisposableHolder;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class UserCacheChangedViewHelper {

    private final DisposableHolder mRequestHolder = new DisposableHolder();

    private long mTargetUserId = Long.MIN_VALUE / 2;

    public UserCacheChangedViewHelper() {
        MSIMManager.getInstance().getUserInfoManager().addUserInfoListener(mUserInfoListener);
    }

    public void setTargetUserId(long targetUserId) {
        if (mTargetUserId != targetUserId) {
            mTargetUserId = targetUserId;
            requestLoadData(true);
        }
    }

    public long getTargetUserId() {
        return mTargetUserId;
    }

    @UiThread
    public void requestLoadData(boolean reset) {
        Preconditions.checkArgument(Threads.isUi());

        // abort last
        mRequestHolder.set(null);

        if (reset) {
            onUserCacheChanged(null);
        }
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    final MSIMUserInfo userInfo = MSIMManager.getInstance().getUserInfoManager().getUserInfo(mTargetUserId);
                    return new ObjectWrapper(userInfo);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectWrapper -> onUserCacheChanged((MSIMUserInfo) objectWrapper.getObject()), MSIMUikitLog::e));
    }

    protected abstract void onUserCacheChanged(@Nullable MSIMUserInfo userInfo);

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMUserInfoListener mUserInfoListener = new MSIMUserInfoListenerProxy(userId -> {
        if (MSIMConstants.isIdMatch(mTargetUserId, userId)) {
            Threads.postUi(() -> {
                if (MSIMConstants.isIdMatch(mTargetUserId, userId)) {
                    requestLoadData(false);
                }
            });
        }
    });

}
