package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.core.math.MathUtils;

import com.masonsoft.imsdk.uikit.MSIMUikitConstants;

import java.util.concurrent.TimeUnit;

import io.github.idonans.core.util.DimenUtil;

public class ResizeAudioView extends FrameLayout {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public ResizeAudioView(Context context) {
        this(context, null);
    }

    public ResizeAudioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResizeAudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ResizeAudioView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private final long MAX_DURATION_MS = TimeUnit.SECONDS.toMillis(60);
    private long mDurationMs;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    public void setDurationMs(long durationMs) {
        if (mDurationMs != durationMs) {
            mDurationMs = durationMs;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 根据 duration 调整
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        boolean requireMeasure = false;
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            if (mDurationMs > 0) {
                requireMeasure = true;
            }
        }
        if (widthSize <= 0) {
            widthSize = DimenUtil.getSmallScreenWidth();
        }

        if (requireMeasure) {
            final float minPercent = 0.15f;
            final float maxPercent = 0.60f;

            float minSize = Math.max(getMinimumWidth(), DimenUtil.getSmallScreenWidth() * minPercent);
            minSize = Math.min(minSize, widthSize);
            minSize = Math.max(minSize, getMinimumWidth());
            float maxSize = Math.max(minSize, DimenUtil.getSmallScreenWidth() * maxPercent);

            float durationPercent = mDurationMs * 1f / MAX_DURATION_MS;
            durationPercent = MathUtils.clamp(durationPercent, 0, 1);

            float bestWidth = minSize + (maxSize - minSize) * durationPercent;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) bestWidth, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
