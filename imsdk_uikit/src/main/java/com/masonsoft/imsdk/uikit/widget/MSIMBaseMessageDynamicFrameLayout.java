package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMSelfUpdateListener;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

public abstract class MSIMBaseMessageDynamicFrameLayout extends FrameLayout {

    protected final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public MSIMBaseMessageDynamicFrameLayout(Context context) {
        this(context, null);
    }

    public MSIMBaseMessageDynamicFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMBaseMessageDynamicFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMBaseMessageDynamicFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    protected MSIMBaseMessage mBaseMessage;
    @SuppressWarnings("FieldCanBeLocal")
    private MSIMSelfUpdateListener mSelfUpdateListener;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    public void setBaseMessage(@NonNull MSIMBaseMessage baseMessage) {
        mBaseMessage = baseMessage;
        mSelfUpdateListener = () -> onBaseMessageChanged(mBaseMessage);
        mBaseMessage.addOnSelfUpdateListener(mSelfUpdateListener);
        onBaseMessageChanged(mBaseMessage);
    }

    protected abstract void onBaseMessageChanged(@Nullable MSIMBaseMessage baseMessage);

}
