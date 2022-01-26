package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomMessage;

import io.github.idonans.lang.util.ViewUtil;

public class IMChatRoomLastMessage extends IMChatRoomStateDynamicFrameLayout {

    public IMChatRoomLastMessage(Context context) {
        this(context, null);
    }

    public IMChatRoomLastMessage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMChatRoomLastMessage(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMChatRoomLastMessage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private MSIMBaseMessageSendStatusTextView mLastMessageView;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mLastMessageView = new MSIMBaseMessageSendStatusTextView(context);
        addView(mLastMessageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onChatRoomStateChanged(@Nullable MSIMChatRoomContext chatRoomContext, @Nullable Object customObject) {
        MSIMChatRoomMessage lastMessage = null;
        if (chatRoomContext != null) {
            lastMessage = chatRoomContext.getShowMessage();
        }
        if (lastMessage == null) {
            ViewUtil.setVisibilityIfChanged(mLastMessageView, View.GONE);
        } else {
            ViewUtil.setVisibilityIfChanged(mLastMessageView, View.VISIBLE);
            mLastMessageView.setBaseMessage(lastMessage);
        }
    }

}
