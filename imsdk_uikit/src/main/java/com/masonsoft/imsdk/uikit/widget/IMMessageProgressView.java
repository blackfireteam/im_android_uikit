package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.lang.util.ViewUtil;

public class IMMessageProgressView extends ProgressView {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMMessageProgressView(Context context) {
        this(context, null);
    }

    public IMMessageProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMMessageProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMMessageProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private IMMessageChangedViewHelper mMessageChangedViewHelper;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mMessageChangedViewHelper = new IMMessageChangedViewHelper() {
            @Override
            protected void onMessageChanged(@Nullable MSIMMessage message, @Nullable Object customObject) {
                IMMessageProgressView.this.updateProgress(message);
            }
        };

        hideProgress();
    }

    private void updateProgress(@Nullable MSIMMessage message) {
        boolean showProgress = false;
        float progress = 0f;
        if (message != null) {
            final int sendState = message.getSendStatus(MSIMConstants.SendStatus.SUCCESS);
            if (sendState == MSIMConstants.SendStatus.IDLE
                    || sendState == MSIMConstants.SendStatus.SENDING) {
                showProgress = true;
                progress = message.getSendProgress();
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

    public void setMessage(@NonNull MSIMMessage message) {
        mMessageChangedViewHelper.setMessage(message);
    }

    public void setMessage(long sessionUserId, int conversationType, long targetUserId, long localMessageId) {
        mMessageChangedViewHelper.setMessage(sessionUserId, conversationType, targetUserId, localMessageId);
    }

}
