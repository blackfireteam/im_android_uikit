package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

import io.github.idonans.appcontext.AppContext;

public abstract class MSIMConversationFrameLayout extends FrameLayout {

    protected final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public MSIMConversationFrameLayout(Context context) {
        this(context, null);
    }

    public MSIMConversationFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMConversationFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMConversationFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    protected MSIMConversation mConversation;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
    }

    public void setConversation(@Nullable MSIMConversation conversation) {
        mConversation = conversation;
        onConversationUpdate(mConversation);
    }

    @Nullable
    public MSIMConversation getConversation() {
        return mConversation;
    }

    protected abstract void onConversationUpdate(@Nullable MSIMConversation conversation);

}
