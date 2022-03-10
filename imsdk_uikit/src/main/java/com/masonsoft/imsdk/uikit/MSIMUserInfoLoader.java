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

    public void setUserInfo(@NonNull MSIMUserInfo userInfo, boolean forceReplace) {
        setUserInfoInternal(userInfo, forceReplace);
    }

    private void setUserInfoInternal(@NonNull MSIMUserInfo userInfo, boolean forceReplace) {
        final MSIMUserInfo currentUserInfo = mUserInfo;
        if (!forceReplace) {
            if (currentUserInfo != null) {
                if (match(currentUserInfo, userInfo)) {
                    // 继续使用当前缓存的 UserInfo
                    userInfo = currentUserInfo;
                }
            }
        }

        mUserInfo = userInfo;
        onUserInfoLoad(userInfo);

        requestLoadData();
    }

    private void onUserInfoChangedInternal(@NonNull MSIMUserInfo userInfo) {
        final MSIMUserInfo currentUserInfo = mUserInfo;
        if (currentUserInfo == null) {
            return;
        }

        if (match(currentUserInfo, userInfo)) {
            mUserInfo = userInfo;
            onUserInfoLoad(userInfo);
        }
    }

    private boolean match(@NonNull MSIMUserInfo obj1, @NonNull MSIMUserInfo obj2) {
        final long userId = obj1.getUserId();
        return userId > 0 && userId == obj2.getUserId();
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
