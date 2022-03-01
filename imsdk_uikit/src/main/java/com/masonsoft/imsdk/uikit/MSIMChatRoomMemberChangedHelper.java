package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomMember;
import com.masonsoft.imsdk.MSIMChatRoomMemberListener;
import com.masonsoft.imsdk.MSIMChatRoomMemberListenerProxy;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

public abstract class MSIMChatRoomMemberChangedHelper implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;
    private final MSIMChatRoomContext mChatRoomContext;
    private final MSIMChatRoomMemberListener mChatRoomMemberListener;

    public MSIMChatRoomMemberChangedHelper(@NonNull MSIMChatRoomContext chatRoomContext) {
        mChatRoomContext = chatRoomContext;
        mChatRoomMemberListener = new MSIMChatRoomMemberListenerProxy(this::onChatRoomMemberChanged, true);

        mChatRoomContext.getChatRoomManager().addChatRoomMemberListener(mChatRoomMemberListener);
    }

    @Override
    public void close() throws IOException {
        mChatRoomContext.getChatRoomManager().removeChatRoomMemberListener(mChatRoomMemberListener);
    }

    protected void onChatRoomMemberChanged(MSIMChatRoomContext chatRoomContext, MSIMChatRoomMember member) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onChatRoomMemberChanged %s %s", Objects.defaultObjectTag(this), chatRoomContext, member);
        }
    }

}
