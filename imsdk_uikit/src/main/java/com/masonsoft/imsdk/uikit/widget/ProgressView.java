package com.masonsoft.imsdk.uikit.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import com.masonsoft.imsdk.uikit.R;

public class ProgressView extends View {

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.ProgressView);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    private int mOrientation = HORIZONTAL;
    private Drawable mProgressDrawable;
    private ObjectAnimator mProgressAnimator;
    private float mCurrentProgress;
    private final Rect mProgressBounds = new Rect();

    private boolean mIncludeProgressStart = true;
    private boolean mIncludeProgressEnd = true;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressView, defStyleAttr,
                defStyleRes);
        mOrientation = a.getInt(R.styleable.ProgressView_android_orientation, mOrientation);
        mProgressDrawable = a.getDrawable(R.styleable.ProgressView_android_progressDrawable);
        mIncludeProgressStart = a.getBoolean(R.styleable.ProgressView_includeProgressStart, mIncludeProgressStart);
        mIncludeProgressEnd = a.getBoolean(R.styleable.ProgressView_includeProgressEnd, mIncludeProgressEnd);
        a.recycle();

        if (isInEditMode()) {
            mCurrentProgress = 0.5f;
        }
    }

    public void setOrientation(int orientation) {
        if (orientation == HORIZONTAL) {
            mOrientation = HORIZONTAL;
        } else {
            mOrientation = VERTICAL;
        }
    }

    public void setProgressDrawable(Drawable progressDrawable) {
        mProgressDrawable = progressDrawable;
    }

    public void setProgress(float progress) {
        this.setProgress(progress, true);
    }

    public void setProgress(float progress, boolean animate) {
        progress = MathUtils.clamp(progress, 0f, 1f);

        if (mProgressAnimator != null) {
            mProgressAnimator.cancel();
            mProgressAnimator = null;
        }

        if (!animate) {
            setCurrentProgress(progress);
            return;
        }

        mProgressAnimator = ObjectAnimator.ofFloat(this, "currentProgress", mCurrentProgress, progress);
        mProgressAnimator.setDuration(200);
        mProgressAnimator.start();
    }

    public void setCurrentProgress(float currentProgress) {
        mCurrentProgress = currentProgress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final Drawable drawable = mProgressDrawable;
        if (drawable == null) {
            return;
        }

        final int progressStart = 0;
        final int progressEnd = 1;
        if (mCurrentProgress == progressStart && !mIncludeProgressStart) {
            return;
        }

        if (mCurrentProgress == progressEnd && !mIncludeProgressEnd) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        if (mOrientation == HORIZONTAL) {
            int left = Math.round(width * (mCurrentProgress));
            mProgressBounds.set(left, 0, width, height);
        } else {
            int bottom = Math.round(height * (1 - mCurrentProgress));
            mProgressBounds.set(0, 0, width, bottom);
        }
        drawable.setBounds(mProgressBounds);
        drawable.draw(canvas);
    }

}
