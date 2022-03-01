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

    public MSIMChatRoomMessageChangedHelper(@NonNull MSIMChatRoomContext chatRoomContext) {
        mChatRoomContext = chatRoomContext;
        mChatRoomMessageListener = new MSIMChatRoomMessageListenerProxy(new MSIMChatRoomMessageListener() {
            @Override
            public void onMessageChanged(@NonNull MSIMChatRoomMessage message) {
                MSIMChatRoomMessageChangedHelper.this.onMessageChanged(message);
            }

            @Override
            public void onReceivedTipMessageList(@NonNull List<CharSequence> list) {
                MSIMChatRoomMessageChangedHelper.this.onReceivedTipMessageList(list);
            }
        }, true);

        mChatRoomContext.getChatRoomManager().addChatRoomMessageListener(mChatRoomMessageListener);
    }

    @Override
    public void close() throws IOException {
        mChatRoomContext.getChatRoomManager().removeChatRoomMessageListener(mChatRoomMessageListener);
    }

    protected void onMessageChanged(@NonNull MSIMChatRoomMessage message) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onMessageChanged %s", Objects.defaultObjectTag(this), message);
        }
    }

    protected void onReceivedTipMessageList(@NonNull List<CharSequence> list) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onReceivedTipMessageList %s", Objects.defaultObjectTag(this), list);
        }
    }

}
