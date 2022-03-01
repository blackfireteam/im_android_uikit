package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

import io.github.idonans.core.util.IOUtil;

public abstract class MSIMUserInfoLoader extends DataLoaderImpl<MSIMUserInfo> implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private final MSIMUserInfoChangedHelper mHelper;

    protected long mUserId;
    @Nullable
    protected MSIMUserInfo mUserInfo;

    public MSIMUserInfoLoader() {
        mHelper = new MSIMUserInfoChangedHelper() {
            @Override
            protected void onUserInfoChanged(@NonNull MSIMUserInfo userInfo) {
                super.onUserInfoChanged(userInfo);

                MSIMUserInfoLoader.this.onUserInfoChangedInternal(userInfo, false);
            }
        };
    }

    @Override
    public void close() throws IOException {
        super.close();
        IOUtil.closeQuietly(mHelper);
    }

    public long getUserId() {
        return mUserId;
    }

    @Nullable
    public MSIMUserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(long userId, @Nullable MSIMUserInfo userInfo) {
        setUserInfoInternal(userId, userInfo);
    }

    public void setUserInfo(@Nullable MSIMUserInfo userInfo) {
        long userId = 0;
        if (userInfo != null) {
            userId = userInfo.getUserId();
        }
        setUserInfoInternal(userId, userInfo);
    }

    private void setUserInfoInternal(long userId, @Nullable MSIMUserInfo userInfo) {
        if (userId > 0) {
            if (userInfo != null) {
                if (userInfo.getUserId() != userId) {
                    MSIMUikitLog.e("unexpected. user info is not equals user id %s => %s", userId, userInfo);
                    return;
                }
            }
        }

        if (userInfo != null) {
            mUserId = userInfo.getUserId();
            mUserInfo = userInfo;
        } else {
            mUserId = userId;
            if (mUserInfo != null) {
                if (mUserInfo.getUserId() != mUserId) {
                    mUserInfo = null;
                }
            }
        }
        onUserInfoLoad(mUserId, mUserInfo);

        requestLoadData();
    }

    private void onUserInfoChangedInternal(@Nullable MSIMUserInfo userInfo, boolean acceptNull) {
        if (!acceptNull && userInfo == null) {
            return;
        }

        if (userInfo != null && userInfo.getUserId() != mUserId) {
            return;
        }

        mUserInfo = userInfo;
        onUserInfoLoad(mUserId, mUserInfo);
    }

    protected void onUserInfoLoad(long userId, @Nullable MSIMUserInfo userInfo) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onUserInfoLoad %s %s", Objects.defaultObjectTag(this), userId, userInfo);
        }
    }

    @Nullable
    @Override
    protected MSIMUserInfo loadData() {
        return MSIMManager.getInstance().getUserInfoManager().getUserInfo(mUserId);
    }

    @Override
    protected void onDataLoad(@Nullable MSIMUserInfo userInfo) {
        onUserInfoChangedInternal(userInfo, true);
    }

}
