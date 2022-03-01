package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.uikit.SessionUserIdChangedHelper;

public class SessionConversationUnreadCountView extends IMConversationAllUnreadCountView {

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
    private SessionUserIdChangedHelper mSessionUserIdChangedHelper;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mSessionUserIdChangedHelper = new SessionUserIdChangedHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                SessionConversationUnreadCountView.this.setSessionUserId(sessionUserId);
            }
        };
        setSessionUserId(mSessionUserIdChangedHelper.getSessionUserId());
    }

}
