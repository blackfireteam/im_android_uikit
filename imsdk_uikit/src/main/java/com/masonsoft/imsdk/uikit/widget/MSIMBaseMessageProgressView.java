package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMSelfUpdateListener;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.lang.util.ViewUtil;

public class MSIMBaseMessageProgressView extends ProgressView {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public MSIMBaseMessageProgressView(Context context) {
        this(context, null);
    }

    public MSIMBaseMessageProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSIMBaseMessageProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MSIMBaseMessageProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    protected MSIMBaseMessage mBaseMessage;
    @SuppressWarnings("FieldCanBeLocal")
    private MSIMSelfUpdateListener mSelfUpdateListener;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        hideProgress();
    }

    private void updateProgress(@Nullable MSIMBaseMessage baseMessage) {
        boolean showProgress = false;
        float progress = 0f;
        if (baseMessage != null) {
            final int sendState = baseMessage.getSendStatus(MSIMConstants.SendStatus.SUCCESS);
            if (sendState == MSIMConstants.SendStatus.IDLE
                    || sendState == MSIMConstants.SendStatus.SENDING) {
                showProgress = true;
                progress = baseMessage.getSendProgress();
            }
        }
        if (showProgress) {
            showProgress(progress);
        } else {
            hideProgress();
        }
    }

    private void showProgress(float progress) {
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " showProgress progress:%s", progress);
        }
        ViewUtil.setVisibilityIfChanged(this, View.VISIBLE);
        setProgress(progress);
    }

    private void hideProgress() {
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " hideProgress");
        }
        ViewUtil.setVisibilityIfChanged(this, View.GONE);
    }

    public void setBaseMessage(@Nullable MSIMBaseMessage baseMessage) {
        mBaseMessage = baseMessage;
        mSelfUpdateListener = () -> onBaseMessageChanged(mBaseMessage);
        if (mBaseMessage != null) {
            mBaseMessage.addOnSelfUpdateListener(mSelfUpdateListener);
        }
        onBaseMessageChanged(mBaseMessage);
    }

    protected void onBaseMessageChanged(@Nullable MSIMBaseMessage baseMessage) {
        updateProgress(baseMessage);
    }

}
