package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

import io.github.idonans.appcontext.AppContext;

public abstract class IMConversationAllUnreadCountDynamicFrameLayout extends FrameLayout {

    private final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMConversationAllUnreadCountDynamicFrameLayout(Context context) {
        this(context, null);
    }

    public IMConversationAllUnreadCountDynamicFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMConversationAllUnreadCountDynamicFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMConversationAllUnreadCountDynamicFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private MSIMConversationAllUnreadCountChangedViewHelper mConversationAllUnreadCountChangedViewHelper;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);

        mConversationAllUnreadCountChangedViewHelper = new MSIMConversationAllUnreadCountChangedViewHelper() {
            @Nullable
            @Override
            protected Object loadCustomObject() {
                return IMConversationAllUnreadCountDynamicFrameLayout.this.loadCustomObject();
            }

            @Override
            protected void onConversationAllUnreadCountChanged(@Nullable Integer allUnreadCount, @Nullable Object customObject) {
                IMConversationAllUnreadCountDynamicFrameLayout.this.onConversationAllUnreadCountChanged(allUnreadCount, customObject);
            }
        };
    }

    public void setSessionUserId(long sessionUserId) {
        mConversationAllUnreadCountChangedViewHelper.setSessionUserId(sessionUserId);
    }

    public long getSessionUserId() {
        return mConversationAllUnreadCountChangedViewHelper.getSessionUserId();
    }

    @Nullable
    @WorkerThread
    protected Object loadCustomObject() {
        return null;
    }

    protected abstract void onConversationAllUnreadCountChanged(@Nullable Integer allUnreadCount, @Nullable Object customObject);

}
