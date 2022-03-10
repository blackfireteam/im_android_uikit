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

    @Nullable
    protected MSIMUserInfo mUserInfo;

    public MSIMUserInfoLoader() {
        mHelper = new MSIMUserInfoChangedHelper() {
            @Override
            protected void onUserInfoChanged(@NonNull MSIMUserInfo userInfo) {
                super.onUserInfoChanged(userInfo);

                MSIMUserInfoLoader.this.onUserInfoChangedInternal(userInfo);
            }
        };
    }

    @Override
    public void close() throws IOException {
        super.close();
        IOUtil.closeQuietly(mHelper);
    }

    @Nullable
    public MSIMUserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(@NonNull MSIMUserInfo userInfo) {
        setUserInfoInternal(userInfo);
    }

    private void setUserInfoInternal(@NonNull MSIMUserInfo userInfo) {
        mUserInfo = userInfo;
        onUserInfoLoad(userInfo);

        requestLoadData();
    }

    private void onUserInfoChangedInternal(@NonNull MSIMUserInfo userInfo) {
        final MSIMUserInfo currentUserInfo = mUserInfo;
        if (currentUserInfo == null) {
            return;
        }

        final long userId = currentUserInfo.getUserId();
        if (userId > 0 && userId == userInfo.getUserId()) {
            if (currentUserInfo == mUserInfo) {
                mUserInfo = userInfo;
                onUserInfoLoad(userInfo);
            }
        }
    }

    protected void onUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onUserInfoLoad %s", Objects.defaultObjectTag(this), userInfo);
        }
    }

    @Nullable
    @Override
    protected MSIMUserInfo loadData() {
        final MSIMUserInfo currentUserInfo = mUserInfo;
        if (currentUserInfo == null) {
            return null;
        }
        return MSIMManager.getInstance().getUserInfoManager().getUserInfo(currentUserInfo.getUserId());
    }

    @Override
    protected void onDataLoad(@Nullable MSIMUserInfo userInfo) {
        if (userInfo != null) {
            onUserInfoChangedInternal(userInfo);
        }
    }

}
