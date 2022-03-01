package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMConversation;

import io.github.idonans.lang.util.ViewUtil;

public class MSIMConversationLastMessage extends MSIMConversationFrameLayout {

    public MSIMConversationLastMessage(Context context) {
        this(context, null);
    }

    public MSIMConversationLastMessage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMConversationLastMessage(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMConversationLastMessage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private MSIMBaseMessageSendStatusTextView mLastMessageView;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mLastMessageView = new MSIMBaseMessageSendStatusTextView(context);
        addView(mLastMessageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onConversationChanged(@Nullable MSIMConversation conversation) {
        MSIMBaseMessage showMessage = null;
        if (conversation != null) {
            showMessage = conversation.getShowMessage();
        }

        if (showMessage == null) {
            ViewUtil.setVisibilityIfChanged(mLastMessageView, View.GONE);
        } else {
            ViewUtil.setVisibilityIfChanged(mLastMessageView, View.VISIBLE);
            mLastMessageView.setBaseMessage(showMessage);
        }
    }

}
