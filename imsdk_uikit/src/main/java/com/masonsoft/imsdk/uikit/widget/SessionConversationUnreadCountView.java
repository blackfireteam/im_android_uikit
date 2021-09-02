package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMConstants;

public class SessionConversationUnreadCountView extends IMConversationUnreadCountView {

    public SessionConversationUnreadCountView(Context context) {
        this(context, null);
    }

    public SessionConversationUnreadCountView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SessionConversationUnreadCountView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SessionConversationUnreadCountView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private SessionUserIdChangedViewHelper mSessionUserIdChangedViewHelper;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mSessionUserIdChangedViewHelper = new SessionUserIdChangedViewHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                SessionConversationUnreadCountView.this.setConversation(sessionUserId, MSIMConstants.ID_ANY);
            }
        };
        setConversation(mSessionUserIdChangedViewHelper.getSessionUserId(), MSIMConstants.ID_ANY);
    }

}
