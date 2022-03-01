package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomMessage;
import com.masonsoft.imsdk.MSIMChatRoomMessageListener;
import com.masonsoft.imsdk.MSIMChatRoomMessageListenerProxy;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public abstract class MSIMChatRoomMessageChangedHelper implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;
    private final MSIMChatRoomContext mChatRoomContext;
    private final MSIMChatRoomMessageListener mChatRoomMessageListener;
    private boolean mClosed;

    public MSIMChatRoomMessageChangedHelper(@NonNull MSIMChatRoomContext chatRoomContext) {
        mChatRoomContext = chatRoomContext;
        mChatRoomMessageListener = new MSIMChatRoomMessageListenerProxy(new MSIMChatRoomMessageListener() {
            @Override
            public void onMessageChanged(@NonNull MSIMChatRoomMessage message) {
                MSIMChatRoomMessageChangedHelper.this.notifyMessageChanged(message);
            }

            @Override
            public void onReceivedTipMessageList(@NonNull List<CharSequence> list) {
                MSIMChatRoomMessageChangedHelper.this.notifyReceivedTipMessageList(list);
            }
        }, true);

        mChatRoomContext.getChatRoomManager().addChatRoomMessageListener(mChatRoomMessageListener);
    }

    @Override
    public void close() throws IOException {
        mClosed = true;
        mChatRoomContext.getChatRoomManager().removeChatRoomMessageListener(mChatRoomMessageListener);
    }

    private void notifyMessageChanged(@NonNull MSIMChatRoomMessage message) {
        if (mClosed) {
            return;
        }
        this.onMessageChanged(message);
    }

    protected void onMessageChanged(@NonNull MSIMChatRoomMessage message) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onMessageChanged %s", Objects.defaultObjectTag(this), message);
        }
    }

    private void notifyReceivedTipMessageList(@NonNull List<CharSequence> list) {
        if (mClosed) {
            return;
        }
        this.onReceivedTipMessageList(list);
    }

    protected void onReceivedTipMessageList(@NonNull List<CharSequence> list) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onReceivedTipMessageList %s", Objects.defaultObjectTag(this), list);
        }
    }

}
