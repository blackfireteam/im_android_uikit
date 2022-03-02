package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMMessageListener;
import com.masonsoft.imsdk.MSIMMessageListenerProxy;
import com.masonsoft.imsdk.MSIMMessagePageContext;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

public abstract class MSIMMessageChangedHelper implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;
    private final MSIMMessageListener mMessageListener;
    private boolean mClosed;

    public MSIMMessageChangedHelper() {
        this(MSIMMessagePageContext.GLOBAL);
    }

    public MSIMMessageChangedHelper(@NonNull MSIMMessagePageContext messagePageContext) {
        mMessageListener = new MSIMMessageListenerProxy(this::notifyMessageChanged, true);
        MSIMManager.getInstance().getMessageManager().addMessageListener(messagePageContext, mMessageListener);
    }

    @Override
    public void close() throws IOException {
        mClosed = true;
        MSIMManager.getInstance().getMessageManager().removeMessageListener(mMessageListener);
    }

    private void notifyMessageChanged(@NonNull MSIMMessage message) {
        if (mClosed) {
            return;
        }
        this.onMessageChanged(message);
    }

    protected void onMessageChanged(@NonNull MSIMMessage message) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onMessageChanged %s", Objects.defaultObjectTag(this), message);
        }
    }

}
