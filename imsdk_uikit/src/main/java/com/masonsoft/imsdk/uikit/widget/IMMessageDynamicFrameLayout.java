package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

public abstract class IMMessageDynamicFrameLayout extends FrameLayout {

    protected final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMMessageDynamicFrameLayout(Context context) {
        this(context, null);
    }

    public IMMessageDynamicFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMMessageDynamicFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMMessageDynamicFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    protected IMMessageChangedViewHelper mMessageChangedViewHelper;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mMessageChangedViewHelper = new IMMessageChangedViewHelper() {
            @Nullable
            @Override
            protected Object loadCustomObject() {
                return IMMessageDynamicFrameLayout.this.loadCustomObject();
            }

            @Override
            protected void onMessageChanged(@Nullable MSIMMessage message, @Nullable Object customObject) {
                IMMessageDynamicFrameLayout.this.onMessageChanged(message, customObject);
            }
        };
    }

    @Nullable
    @WorkerThread
    protected Object loadCustomObject() {
        return null;
    }

    public void setMessage(@NonNull MSIMMessage message) {
        mMessageChangedViewHelper.setMessage(message);
    }

    public void setMessage(long sessionUserId, int conversationType, long targetUserId, long localMessageId) {
        mMessageChangedViewHelper.setMessage(sessionUserId, conversationType, targetUserId, localMessageId);
    }

    public long getSessionUserId() {
        return mMessageChangedViewHelper.getSessionUserId();
    }

    public int getConversationType() {
        return mMessageChangedViewHelper.getConversationType();
    }

    public long getTargetUserId() {
        return mMessageChangedViewHelper.getTargetUserId();
    }

    public long getLocalMessageId() {
        return mMessageChangedViewHelper.getLocalMessageId();
    }

    protected abstract void onMessageChanged(@Nullable MSIMMessage message, @Nullable Object customObject);

}
