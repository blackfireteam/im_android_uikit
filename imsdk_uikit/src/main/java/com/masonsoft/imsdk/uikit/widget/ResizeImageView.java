package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.UiThread;
import androidx.core.math.MathUtils;

import io.github.idonans.core.util.DimenUtil;

public class ResizeImageView extends FrameLayout {

    private static final boolean DEBUG = false;

    public ResizeImageView(Context context) {
        this(context, null);
    }

    public ResizeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResizeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ResizeImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private int mImageWidth;
    private int mImageHeight;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    @UiThread
    public void setImageSize(long imageWidth, long imageHeight) {
        if (mImageWidth != imageWidth || mImageHeight != imageHeight) {
            mImageWidth = (int) imageWidth;
            mImageHeight = (int) imageHeight;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 根据 image size 调整
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        boolean requireMeasure = false;
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            requireMeasure = true;
        }
        if (widthSize <= 0) {
            widthSize = DimenUtil.getSmallScreenWidth();
        }

        if (requireMeasure) {
            final float minPercent = 0.25f;
            final float maxPercent = 0.50f;
            int imageWidth = mImageWidth;
            int imageHeight = mImageHeight;
            if (imageWidth <= 0 || imageHeight <= 0) {
                // 图片宽高未设置或者设置的值不合法
                imageWidth = 100;
                imageHeight = 100;
            }

            float minSize = Math.max(getMinimumWidth(), DimenUtil.getSmallScreenWidth() * minPercent);
            minSize = Math.min(minSize, widthSize);
            float maxSize = Math.max(minSize, DimenUtil.getSmallScreenWidth() * maxPercent);

            if (imageWidth > imageHeight) {
                // 宽度固定
                final float bestWidth = MathUtils.clamp(imageWidth, minSize, maxSize);
                float bestHeight = bestWidth * imageHeight / imageWidth;
                bestHeight = MathUtils.clamp(bestHeight, minSize, maxSize);
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) bestWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) bestHeight, MeasureSpec.EXACTLY);
            } else {
                // 高度固定
                final float bestHeight = MathUtils.clamp(imageHeight, minSize, maxSize);
                float bestWidth = bestHeight * imageWidth / imageHeight;
                bestWidth = MathUtils.clamp(bestWidth, minSize, maxSize);
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) bestWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) bestHeight, MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
