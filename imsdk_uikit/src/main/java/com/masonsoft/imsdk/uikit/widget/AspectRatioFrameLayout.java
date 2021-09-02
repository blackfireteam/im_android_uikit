package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;

/**
 * 固定宽高比
 */
public class AspectRatioFrameLayout extends FrameLayout {

    public AspectRatioFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public AspectRatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AspectRatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private float mAspectRatio = 3f / 4f;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioFrameLayout, defStyleAttr,
                defStyleRes);
        mAspectRatio = a.getFloat(R.styleable.AspectRatioFrameLayout_aspectRatio, mAspectRatio);
        a.recycle();
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        requestLayout();
        postInvalidate();
    }

    public float getAspectRatio() {
        return mAspectRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            // 以宽度为基准
            //noinspection UnnecessaryLocalVariable
            int width = widthSize;
            int height;
            if (mAspectRatio != 0) {
                height = (int) (width / mAspectRatio);
            } else {
                height = 0;
            }
            setMeasuredDimension(width, height);
            measureChildInner();
            return;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            // 以高度为基准
            //noinspection UnnecessaryLocalVariable
            int height = heightSize;
            int width = (int) (height * mAspectRatio);
            setMeasuredDimension(width, height);
            measureChildInner();
            return;
        }

        MSIMUikitLog.v("neither width or height is exactly");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void measureChildInner() {
        final int count = getChildCount();
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        for (int i = 0; i < count; i++) {
            measureChildWithMargins(getChildAt(i), widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
    }

}
