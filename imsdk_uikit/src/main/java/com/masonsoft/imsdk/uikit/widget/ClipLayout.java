package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import com.masonsoft.imsdk.uikit.R;

import io.github.idonans.appcontext.AppContext;

public class ClipLayout extends FrameLayout {

    public ClipLayout(Context context) {
        this(context, null);
    }

    public ClipLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ClipLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private float mRoundSize;
    private boolean mRoundAsCircle;
    private boolean mShowBorder;
    private float mBorderSize;
    private int mBorderColor;

    private Paint mBorderPaint;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClipLayout, defStyleAttr,
                defStyleRes);
        mRoundSize = a.getDimension(R.styleable.ClipLayout_roundSize, mRoundSize);
        mRoundAsCircle = a.getBoolean(R.styleable.ClipLayout_roundAsCircle, mRoundAsCircle);
        mShowBorder = a.getBoolean(R.styleable.ClipLayout_showBorder, mShowBorder);
        mBorderSize = a.getDimension(R.styleable.ClipLayout_borderSize, mBorderSize);
        mBorderColor = a.getColor(R.styleable.ClipLayout_borderColor, mBorderColor);
        a.recycle();

        setOutlineProvider(mClipOutlineProvider);
        setClipToOutline(true);

        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setDither(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderSize);
        mBorderPaint.setColor(mBorderColor);
    }

    public void setShowBorder(boolean showBorder) {
        if (mShowBorder != showBorder) {
            mShowBorder = showBorder;
            invalidate();
        }
    }

    public boolean isShowBorder() {
        return mShowBorder;
    }

    public void setBorderColor(boolean white) {
        setBorderColor(white ? Color.WHITE : Color.BLACK);
    }

    public void setBorderColor(int borderColor) {
        if (mBorderColor != borderColor) {
            mBorderColor = borderColor;
            mBorderPaint.setColor(mBorderColor);

            if (mShowBorder) {
                invalidate();
            }
        }
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderSize(float borderSize) {
        if (mBorderSize != borderSize) {
            mBorderSize = borderSize;
            mBorderPaint.setStrokeWidth(mBorderSize);

            if (mShowBorder) {
                invalidate();
            }
        }
    }

    public float getBorderSize() {
        return mBorderSize;
    }

    public void setRoundSize(float roundSize) {
        if (mRoundSize != roundSize) {
            mRoundSize = roundSize;
            invalidateOutline();

            if (mShowBorder) {
                invalidate();
            }
        }
    }

    public float getRoundSize() {
        return mRoundSize;
    }

    public void setRoundAsCircle(boolean roundAsCircle) {
        if (mRoundAsCircle != roundAsCircle) {
            mRoundAsCircle = roundAsCircle;
            invalidateOutline();

            if (mShowBorder) {
                invalidate();
            }
        }
    }

    public boolean isRoundAsCircle() {
        return mRoundAsCircle;
    }

    private final ViewOutlineProvider mClipOutlineProvider = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            float width = view.getWidth();
            float height = view.getHeight();

            if (mRoundAsCircle) {
                float circleCenterX = width / 2f;
                float circleCenterY = height / 2f;
                float circleR = Math.min(circleCenterX, circleCenterY);
                outline.setRoundRect(((int) (circleCenterX - circleR)),
                        ((int) (circleCenterY - circleR)),
                        ((int) (circleCenterX + circleR)),
                        ((int) (circleCenterY + circleR)),
                        circleR);
            } else {
                outline.setRoundRect(0, 0, (int) width, (int) height, mRoundSize);
            }
        }
    };

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (!mShowBorder) {
            return;
        }

        final float halfBorderSize = mBorderSize * 0.5f;
        float width = getWidth();
        float height = getHeight();

        if (mRoundAsCircle) {
            float circleCenterX = width * 0.5f;
            float circleCenterY = height * 0.5f;
            float circleR = Math.min(circleCenterX, circleCenterY);
            canvas.drawCircle(circleCenterX, circleCenterY, circleR - halfBorderSize, mBorderPaint);
        } else {
            canvas.drawRoundRect(
                    halfBorderSize,
                    halfBorderSize,
                    width - halfBorderSize,
                    height - halfBorderSize,
                    mRoundSize,
                    mRoundSize,
                    mBorderPaint
            );
        }
    }

}
