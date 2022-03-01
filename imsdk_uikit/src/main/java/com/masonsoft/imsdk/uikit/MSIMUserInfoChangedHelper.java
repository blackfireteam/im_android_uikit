package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.MSIMUserInfoListener;
import com.masonsoft.imsdk.MSIMUserInfoListenerProxy;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

public abstract class MSIMUserInfoChangedHelper implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;
    private final MSIMUserInfoListener mUserInfoListener;

    public MSIMUserInfoChangedHelper() {
        mUserInfoListener = new MSIMUserInfoListenerProxy(this::onUserInfoChanged, true);
        MSIMManager.getInstance().getUserInfoManager().addUserInfoListener(mUserInfoListener);
    }

    @Override
    public void close() throws IOException {
        MSIMManager.getInstance().getUserInfoManager().removeUserInfoListener(mUserInfoListener);
    }

    protected void onUserInfoChanged(@NonNull MSIMUserInfo userInfo) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onUserInfoChanged %s", Objects.defaultObjectTag(this), userInfo);
        }
    }

}
