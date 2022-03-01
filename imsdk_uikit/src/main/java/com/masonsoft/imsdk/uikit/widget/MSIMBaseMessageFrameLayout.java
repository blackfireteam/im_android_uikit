package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

public abstract class MSIMBaseMessageFrameLayout extends FrameLayout {

    protected final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public MSIMBaseMessageFrameLayout(Context context) {
        this(context, null);
    }

    public MSIMBaseMessageFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMBaseMessageFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMBaseMessageFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    protected MSIMBaseMessage mBaseMessage;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    public void setBaseMessage(@Nullable MSIMBaseMessage baseMessage) {
        mBaseMessage = baseMessage;
        onBaseMessageChanged(mBaseMessage);
    }

    protected void onBaseMessageChanged(@Nullable MSIMBaseMessage baseMessage) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onBaseMessageChanged %s", Objects.defaultObjectTag(this), baseMessage);
        }
    }

}
