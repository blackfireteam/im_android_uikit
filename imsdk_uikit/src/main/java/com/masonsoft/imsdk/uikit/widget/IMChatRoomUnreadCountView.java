package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.R;

import io.github.idonans.appcontext.AppContext;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.lang.util.ViewUtil;

public class IMChatRoomUnreadCountView extends FrameLayout {

    private final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    public IMChatRoomUnreadCountView(Context context) {
        this(context, null);
    }

    public IMChatRoomUnreadCountView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMChatRoomUnreadCountView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IMChatRoomUnreadCountView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private Paint mBackgroundPaint;
    private Paint mTextPaint;

    private boolean mOnlyDrawableBackground;

    private long mUnreadCount;
    private String mUnreadCountText;
    private final RectF mTextRect = new RectF();

    private int mDefaultMeasureSize;
    private int mAdjustPaddingLeftRight;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        AppContext.setContextInEditMode(this);
        if (isInEditMode()) {
            setUnreadCount((long) (1 + Math.random() * 100));
        }

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IMChatRoomUnreadCountView, defStyleAttr,
                defStyleRes);
        mOnlyDrawableBackground = a.getBoolean(R.styleable.IMConversationUnreadCountView_OnlyDrawableBackground, mOnlyDrawableBackground);
        a.recycle();

        mDefaultMeasureSize = DimenUtil.dp2px(18);
        mAdjustPaddingLeftRight = DimenUtil.dp2px(5);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setDither(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        mBackgroundPaint.setStrokeJoin(Paint.Join.ROUND);
        mBackgroundPaint.setColor(0xFFFF3333);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setStrokeCap(Paint.Cap.ROUND);
        mTextPaint.setStrokeJoin(Paint.Join.ROUND);
        mTextPaint.setColor(0xFFFFFFFF);
        mTextPaint.setTextSize(DimenUtil.sp2px(11));

        setWillNotDraw(false);
    }

    public void setOnlyDrawableBackground(boolean onlyDrawableBackground) {
        if (mOnlyDrawableBackground != onlyDrawableBackground) {
            mOnlyDrawableBackground = onlyDrawableBackground;
            requestLayout();
            invalidate();
        }
    }

    public void setUnreadCount(long unreadCount) {
        if (mUnreadCount != unreadCount) {
            mUnreadCount = unreadCount;
            mUnreadCountText = String.valueOf(mUnreadCount);
            requestLayout();
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int adjustHeight;
        if (heightMode == MeasureSpec.EXACTLY) {
            adjustHeight = heightSize;
        } else {
            adjustHeight = mDefaultMeasureSize;
        }

        int adjustWidth;
        if (widthMode == MeasureSpec.EXACTLY) {
            adjustWidth = widthSize;
        } else {
            if (mUnreadCount <= 99) {
                adjustWidth = mDefaultMeasureSize;
            } else {
                float textWidth = 0f;
                if (mUnreadCountText != null) {
                    textWidth = mTextPaint.measureText(mUnreadCountText);
                }
                textWidth += 2 * mAdjustPaddingLeftRight;
                if (textWidth < mDefaultMeasureSize) {
                    textWidth = mDefaultMeasureSize;
                }
                adjustWidth = (int) textWidth;
            }
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(adjustWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(adjustHeight, MeasureSpec.EXACTLY)
        );
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mTextRect.set(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mUnreadCount <= 0) {
            return;
        }

        float circleCenterX = getWidth() / 2f;
        float circleCenterY = getHeight() / 2f;
        float circleR = Math.min(circleCenterX, circleCenterY);

        // 绘制背景
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), circleR, circleR, mBackgroundPaint);

        if (mOnlyDrawableBackground) {
            return;
        }

        // 绘制文字
        if (mUnreadCountText != null) {
            ViewUtil.drawText(canvas, mUnreadCountText, mTextPaint, mTextRect, Gravity.CENTER);
        }
    }

}
