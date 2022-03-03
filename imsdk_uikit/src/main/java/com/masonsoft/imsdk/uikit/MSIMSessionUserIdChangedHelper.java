package com.masonsoft.imsdk.uikit;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMSessionListener;
import com.masonsoft.imsdk.MSIMSessionListenerProxy;

import java.io.Closeable;
import java.io.IOException;

import io.github.idonans.core.thread.Threads;

public abstract class MSIMSessionUserIdChangedHelper implements Closeable {

    private long mSessionUserId;
    private boolean mClosed;

    public MSIMSessionUserIdChangedHelper() {
        mSessionUserId = MSIMManager.getInstance().getSessionUserId();
        MSIMManager.getInstance().addSessionListener(mSessionListener);
    }

    @Override
    public void close() throws IOException {
        mClosed = true;
        MSIMManager.getInstance().removeSessionListener(mSessionListener);
    }

    public long getSessionUserId() {
        return mSessionUserId;
    }

    private void notifySessionUserIdChanged(long sessionUserId) {
        if (mClosed) {
            return;
        }
        this.onSessionUserIdChanged(sessionUserId);
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
                        MSIMSessionUserIdChangedHelper.this.notifySessionUserIdChanged(mSessionUserId);
                    }
                });
            }
        }
    });

}
