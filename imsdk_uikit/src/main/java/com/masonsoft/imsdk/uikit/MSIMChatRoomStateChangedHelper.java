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

    public MSIMChatRoomStateChangedHelper(@NonNull MSIMChatRoomContext chatRoomContext) {
        mChatRoomContext = chatRoomContext;
        mChatRoomStateListener = new MSIMChatRoomStateListenerProxy(this::onChatRoomStateChanged, true);

        mChatRoomContext.getChatRoomManager().addChatRoomStateListener(mChatRoomStateListener);
    }

    @Override
    public void close() throws IOException {
        mChatRoomContext.getChatRoomManager().removeChatRoomStateListener(mChatRoomStateListener);
    }

    protected void onChatRoomStateChanged(MSIMChatRoomManager manager) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onChatRoomStateChanged %s", Objects.defaultObjectTag(this), manager);
        }
    }

}
