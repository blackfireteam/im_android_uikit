package com.masonsoft.imsdk.uikit.widget;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import io.github.idonans.lang.util.ViewUtil;

/**
 * 加载状态 view. 水平细长条样式。
 */
public class TinyLoadingView extends View {

    public TinyLoadingView(@NonNull Context context) {
        this(context, null);
    }

    public TinyLoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TinyLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TinyLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private final int mLineColor = 0xFFFFFFFF;
    private Paint mPaint;
    private final RectF mDrawRect = new RectF();

    private final long mDuration = 1000L;

    private ValueAnimator mAnimator;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mLineColor);

        mAnimator = ValueAnimator.ofPropertyValuesHolder(
                PropertyValuesHolder.ofFloat("widthPercent", 0f, 1f),
                PropertyValuesHolder.ofInt("alphaValue", 255, 255, 255, 255, 255, 200, 200, 200, 200, 200, 0));
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(mDuration);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addUpdateListener(animation -> post(() -> {
            if (getVisibility() == View.VISIBLE) {
                postInvalidateOnAnimation();
            } else {
                mAnimator.pause();
            }
        }));

        ViewUtil.setVisibilityIfChanged(this, View.GONE);
    }

    @FloatRange(from = 0f, to = 1f)
    private float getWidthPercent() {
        Object value = mAnimator.getAnimatedValue("widthPercent");
        if (value != null) {
            return MathUtils.clamp((float) value, 0f, 1f);
        }
        return 0f;
    }

    private void updateDrawRect() {
        float width = getWidth();
        float height = getHeight();
        float progress = getWidthPercent();
        float drawWidth = width * progress;
        mDrawRect.set((width - drawWidth) / 2, 0, (width + drawWidth) / 2, height);
    }

    @IntRange(from = 0, to = 255)
    private int getAlphaValue() {
        Object value = mAnimator.getAnimatedValue("alphaValue");
        if (value != null) {
            return MathUtils.clamp(((int) value), 0, 255);
        }
        return 0;
    }

    private void updatePaint() {
        int alpha = getAlphaValue();
        mPaint.setColor((mLineColor & 0xFFFFFF) | (alpha << 24));
    }

    @Deprecated
    public void show() {
        ViewUtil.setVisibilityIfChanged(this, VISIBLE);
    }

    @Deprecated
    public void hide() {
        ViewUtil.setVisibilityIfChanged(this, GONE);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (getVisibility() == View.VISIBLE) {
            mAnimator.start();
        } else {
            mAnimator.pause();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        updatePaint();
        updateDrawRect();

        canvas.drawRect(mDrawRect, mPaint);
    }

}
