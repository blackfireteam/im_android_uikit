package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.uikit.MSIMBaseMessageLoader;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.appcontext.AppContext;

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

    private MSIMBaseMessageLoader mBaseMessageLoader;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);

        mBaseMessageLoader = new MSIMBaseMessageLoader() {
            @Override
            protected void onBaseMessageLoad(@Nullable MSIMBaseMessage baseMessage) {
                super.onBaseMessageLoad(baseMessage);

                MSIMBaseMessageFrameLayout.this.onBaseMessageLoad(baseMessage);
            }
        };
    }

    public void setBaseMessage(@Nullable MSIMBaseMessage baseMessage) {
        mBaseMessageLoader.setBaseMessage(baseMessage);
    }

    @Nullable
    public MSIMBaseMessage getBaseMessage() {
        return mBaseMessageLoader.getBaseMessage();
    }

    protected void onBaseMessageLoad(@Nullable MSIMBaseMessage baseMessage) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onBaseMessageLoad %s", Objects.defaultObjectTag(this), baseMessage);
        }
    }

}
