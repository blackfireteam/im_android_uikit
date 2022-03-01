package com.masonsoft.imsdk.uikit;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMSessionListener;
import com.masonsoft.imsdk.MSIMSessionListenerProxy;

import io.github.idonans.core.thread.Threads;

public abstract class SessionUserIdChangedHelper {

    private long mSessionUserId;

    public SessionUserIdChangedHelper() {
        mSessionUserId = MSIMManager.getInstance().getSessionUserId();
        MSIMManager.getInstance().addSessionListener(mSessionListener);
    }

    public long getSessionUserId() {
        return mSessionUserId;
    }

    protected abstract void onSessionUserIdChanged(long sessionUserId);

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMSessionListener mSessionListener = new MSIMSessionListenerProxy(new MSIMSessionListener() {
        @Override
        public void onSessionChanged() {
            sync();
        }

        @Override
        public void onSessionUserIdChanged() {
            sync();
        }

        private boolean isSessionUserIdChanged() {
            return mSessionUserId != MSIMManager.getInstance().getSessionUserId();
        }

        private void sync() {
            if (isSessionUserIdChanged()) {
                Threads.postUi(() -> {
                    if (isSessionUserIdChanged()) {
                        mSessionUserId = MSIMManager.getInstance().getSessionUserId();
                        SessionUserIdChangedHelper.this.onSessionUserIdChanged(mSessionUserId);
                    }
                });
            }
        }
    });

}
