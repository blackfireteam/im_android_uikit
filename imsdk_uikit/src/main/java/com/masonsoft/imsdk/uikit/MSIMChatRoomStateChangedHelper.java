package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomManager;
import com.masonsoft.imsdk.MSIMChatRoomStateListener;
import com.masonsoft.imsdk.MSIMChatRoomStateListenerProxy;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

public abstract class MSIMChatRoomStateChangedHelper implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;
    private final MSIMChatRoomContext mChatRoomContext;
    private final MSIMChatRoomStateListener mChatRoomStateListener;
    private boolean mClosed;

    public MSIMChatRoomStateChangedHelper(@NonNull MSIMChatRoomContext chatRoomContext) {
        mChatRoomContext = chatRoomContext;
        mChatRoomStateListener = new MSIMChatRoomStateListenerProxy(this::notifyChatRoomStateChanged, true);

        mChatRoomContext.getChatRoomManager().addChatRoomStateListener(mChatRoomStateListener);
    }

    @Override
    public void close() throws IOException {
        mClosed = true;
        mChatRoomContext.getChatRoomManager().removeChatRoomStateListener(mChatRoomStateListener);
    }

    private void notifyChatRoomStateChanged(MSIMChatRoomManager manager) {
        if (mClosed) {
            return;
        }
        this.onChatRoomStateChanged(manager);
    }

    protected void onChatRoomStateChanged(MSIMChatRoomManager manager) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onChatRoomStateChanged %s", Objects.defaultObjectTag(this), manager);
        }
    }

}
