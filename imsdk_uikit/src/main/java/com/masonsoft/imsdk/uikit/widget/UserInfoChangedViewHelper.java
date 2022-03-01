package com.masonsoft.imsdk.uikit.widget;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

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

@Deprecated
public abstract class UserInfoChangedViewHelper {

    private final DisposableHolder mRequestHolder = new DisposableHolder();

    @Nullable
    private MSIMUserInfo mUserInfo;
    private long mUserId = Long.MIN_VALUE / 2;

    public UserInfoChangedViewHelper() {
        MSIMManager.getInstance().getUserInfoManager().addUserInfoListener(mUserInfoListener);
    }

    public void setUserInfo(long userId) {
        if (mUserInfo != null && mUserInfo.getUserId() != userId) {
            mUserInfo = null;
        }
        mUserId = userId;

        requestLoadData();
    }

    public void setUserInfo(long userId, @Nullable MSIMUserInfo userInfo) {
        mUserId = userId;
        mUserInfo = userInfo;

        requestLoadData();
    }

    public long getUserId() {
        return mUserId;
    }

    @Nullable
    public MSIMUserInfo getUserInfo() {
        return mUserInfo;
    }

    @UiThread
    public void requestLoadData() {
        Preconditions.checkArgument(Threads.isUi());

        // abort last
        mRequestHolder.set(null);

        onUserInfoChanged(mUserId, mUserInfo);
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    final MSIMUserInfo userInfo = MSIMManager.getInstance().getUserInfoManager().getUserInfo(mUserId);
                    return new ObjectWrapper(userInfo);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectWrapper -> {
                    mUserInfo = (MSIMUserInfo) objectWrapper.getObject();
                    onUserInfoChanged(mUserId, mUserInfo);
                }, MSIMUikitLog::e));
    }

    protected abstract void onUserInfoChanged(long userId, @Nullable MSIMUserInfo userInfo);

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMUserInfoListener mUserInfoListener = new MSIMUserInfoListenerProxy(userInfo -> {
        if (mUserId == userInfo.getUserId()) {
            mUserInfo = userInfo;
            onUserInfoChanged(mUserId, mUserInfo);
        }
    }, true);

}
