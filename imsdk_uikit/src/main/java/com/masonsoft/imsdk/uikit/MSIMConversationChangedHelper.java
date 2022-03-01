package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMConversationListener;
import com.masonsoft.imsdk.MSIMConversationListenerProxy;
import com.masonsoft.imsdk.MSIMConversationPageContext;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

public abstract class MSIMConversationChangedHelper implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;
    private final MSIMConversationListener mConversationListener;
    private boolean mClosed;

    public MSIMConversationChangedHelper() {
        this(MSIMConversationPageContext.GLOBAL);
    }

    public MSIMConversationChangedHelper(@NonNull MSIMConversationPageContext conversationPageContext) {
        mConversationListener = new MSIMConversationListenerProxy(this::notifyConversationChanged, true);
        MSIMManager.getInstance().getConversationManager().addConversationListener(conversationPageContext, mConversationListener);
    }

    @Override
    public void close() throws IOException {
        mClosed = true;
        MSIMManager.getInstance().getConversationManager().removeConversationListener(mConversationListener);
    }

    private void notifyConversationChanged(@NonNull MSIMConversation conversation) {
        if (mClosed) {
            return;
        }
        this.onConversationChanged(conversation);
    }

    protected void onConversationChanged(@NonNull MSIMConversation conversation) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onConversationChanged %s", Objects.defaultObjectTag(this), conversation);
        }
    }

}
