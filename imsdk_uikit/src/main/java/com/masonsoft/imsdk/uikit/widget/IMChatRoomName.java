package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomInfo;

public class IMChatRoomName extends IMChatRoomStateTextView {

    public IMChatRoomName(Context context) {
        this(context, null);
    }

    public IMChatRoomName(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMChatRoomName(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMChatRoomName(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onChatRoomStateChanged(@Nullable MSIMChatRoomContext chatRoomContext, @Nullable Object customObject) {
        MSIMChatRoomInfo chatRoomInfo = null;
        if (chatRoomContext != null) {
            chatRoomInfo = chatRoomContext.getChatRoomInfo();
        }

        if (chatRoomInfo == null) {
            setText(null);
        } else {
            setText(chatRoomInfo.getName());
        }
    }

}
