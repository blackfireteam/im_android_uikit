package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

import io.github.idonans.appcontext.AppContext;

public abstract class IMConversationDynamicFrameLayout extends FrameLayout {

    private final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMConversationDynamicFrameLayout(Context context) {
        this(context, null);
    }

    public IMConversationDynamicFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMConversationDynamicFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMConversationDynamicFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private MSIMConversationChangedViewHelper mConversationChangedViewHelper;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);

        mConversationChangedViewHelper = new MSIMConversationChangedViewHelper() {
            @Nullable
            @Override
            protected Object loadCustomObject() {
                return IMConversationDynamicFrameLayout.this.loadCustomObject();
            }

            @Override
            protected void onConversationChanged(@Nullable MSIMConversation conversation, @Nullable Object customObject) {
                IMConversationDynamicFrameLayout.this.onConversationChanged(conversation, customObject);
            }
        };
    }

    public void setConversation(long sessionUserId, long conversationId) {
        mConversationChangedViewHelper.setConversation(sessionUserId, conversationId);
    }

    public long getSessionUserId() {
        return mConversationChangedViewHelper.getSessionUserId();
    }

    public long getConversationId() {
        return mConversationChangedViewHelper.getConversationId();
    }

    @Nullable
    @WorkerThread
    protected Object loadCustomObject() {
        return null;
    }

    protected abstract void onConversationChanged(@Nullable MSIMConversation conversation, @Nullable Object customObject);

}
