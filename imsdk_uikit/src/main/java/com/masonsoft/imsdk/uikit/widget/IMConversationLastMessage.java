package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

import io.github.idonans.lang.util.ViewUtil;

public class IMConversationLastMessage extends IMConversationDynamicFrameLayout {

    private final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMConversationLastMessage(Context context) {
        this(context, null);
    }

    public IMConversationLastMessage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMConversationLastMessage(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMConversationLastMessage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private IMMessageSendStatusTextView mLastMessageView;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mLastMessageView = new IMMessageSendStatusTextView(context);
        addView(mLastMessageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onConversationChanged(@Nullable MSIMConversation conversation, @Nullable Object customObject) {
        if (conversation == null) {
            ViewUtil.setVisibilityIfChanged(mLastMessageView, View.GONE);
        } else {
            ViewUtil.setVisibilityIfChanged(mLastMessageView, View.VISIBLE);
            mLastMessageView.setMessage(
                    conversation.getSessionUserId(),
                    conversation.getConversationType(),
                    conversation.getTargetUserId(),
                    conversation.getShowMessageId()
            );
        }
    }

}
