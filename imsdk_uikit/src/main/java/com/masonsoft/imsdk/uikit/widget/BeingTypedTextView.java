package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.entity.CustomMessagePayload;

import io.github.idonans.lang.util.ViewUtil;

/**
 * 对方正在输入...
 */
public class BeingTypedTextView extends AppCompatTextView {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private IMReceivedCustomSignalingMessageViewHelper mReceivedCustomSignalingMessageViewHelper;

    public BeingTypedTextView(Context context) {
        this(context, null);
    }

    public BeingTypedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeingTypedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BeingTypedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mReceivedCustomSignalingMessageViewHelper = new IMReceivedCustomSignalingMessageViewHelper() {
            @Override
            protected void onReceivedCustomSignalingMessage(@NonNull MSIMMessage message, @Nullable CustomMessagePayload customMessagePayload) {
                if (customMessagePayload == null) {
                    return;
                }
                if (customMessagePayload.isTypeBeingTyped()) {
                    BeingTypedTextView.this.onReceivedCustomSignalingMessage(message, customMessagePayload);
                }
            }
        };
    }

    public void setTarget(long sessionUserId, long targetUserId) {
        mReceivedCustomSignalingMessageViewHelper.setTarget(sessionUserId, targetUserId);
    }

    protected void onReceivedCustomSignalingMessage(@NonNull MSIMMessage message, @NonNull CustomMessagePayload customMessagePayload) {
        removeCallbacks(mHide);
        ViewUtil.setVisibilityIfChanged(this, View.VISIBLE);
        // 显示一段时间之后，隐藏
        postDelayed(mHide, 5000L);
    }

    private final Runnable mHide = () -> ViewUtil.setVisibilityIfChanged(this, View.GONE);

}
