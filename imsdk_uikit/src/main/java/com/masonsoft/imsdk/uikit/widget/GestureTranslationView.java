package com.masonsoft.imsdk.uikit.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.core.math.MathUtils;

import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import io.github.idonans.dynamic.DynamicLog;

public class GestureTranslationView extends FrameLayout {

    public GestureTranslationView(Context context) {
        this(context, null);
    }

    public GestureTranslationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureTranslationView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GestureTranslationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private int mTouchSlop;

    private int mActivePointerId = -1; // 用于计算滑动的手指
    private float mLastMotionX;
    private float mLastMotionY;

    private boolean mIsBeingDragged;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private float getEventX(MotionEvent event, int pointerIndex) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return event.getRawX(pointerIndex);
        } else {
            return event.getRawX();
        }
    }

    private float getEventY(MotionEvent event, int pointerIndex) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return event.getRawY(pointerIndex);
        } else {
            return event.getRawY();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        int pointerIndex;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(0);
                mIsBeingDragged = false;
                mLastMotionX = getEventX(event, 0);
                mLastMotionY = getEventY(event, 0);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId < 0) {
                    DynamicLog.e("onInterceptTouchEvent ACTION_MOVE but no active pointer id.");
                    return false;
                }

                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    DynamicLog.e("onInterceptTouchEvent ACTION_MOVE but active pointer id invalid.");
                    return false;
                }

                float x = getEventX(event, pointerIndex);
                float y = getEventY(event, pointerIndex);
                startDragging(x, y);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = -1;
                break;
        }

        return mIsBeingDragged;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        int pointerIndex;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(0);
                mIsBeingDragged = false;
                mLastMotionX = getEventX(event, 0);
                mLastMotionY = getEventY(event, 0);
                break;

            case MotionEvent.ACTION_MOVE: {
                if (mActivePointerId < 0) {
                    DynamicLog.e("onTouchEvent ACTION_MOVE but no active pointer id.");
                    return false;
                }

                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    DynamicLog.e("onTouchEvent ACTION_MOVE but active pointer id invalid.");
                    return false;
                }

                float x = getEventX(event, pointerIndex);
                float y = getEventY(event, pointerIndex);

                MSIMUikitLog.i("== x:%s, y:%s, mLastMotionX:%s, mLastMotionY:%s, pointerIndex:%s",
                        x, y, mLastMotionX, mLastMotionY, pointerIndex);

                if (!mIsBeingDragged) {
                    startDragging(x, y);
                } else {
                    final float dx = mLastMotionX - x;
                    final float dy = mLastMotionY - y;
                    applyTranslation(dx, dy);
                    mLastMotionX = x;
                    mLastMotionY = y;
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;

            case MotionEvent.ACTION_UP: {
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                } else {
                    performClick();
                }
                mActivePointerId = -1;
                break;
            }
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                }
                mActivePointerId = -1;
                break;
        }

        return true;
    }

    private void startDragging(float x, float y) {
        if (mIsBeingDragged) {
            return;
        }

        final float dx = mLastMotionX - x;
        final float dy = mLastMotionY - y;
        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);

        if (absDx > mTouchSlop || absDy > mTouchSlop) {
            mIsBeingDragged = true;
        }

        if (mIsBeingDragged) {
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent event) {
        final int pointerIndex = event.getActionIndex();
        final int pointerId = event.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // 抬起的手指正是当前用于计算滑动的手指
            // 重新设置计算滑动的手指和对应的滑动坐标
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = event.getPointerId(newPointerIndex);
            mLastMotionX = getEventX(event, newPointerIndex);
            mLastMotionY = getEventY(event, newPointerIndex);
        }
    }

    private void applyTranslation(float dx, float dy) {
        final ViewGroup parentView = (ViewGroup) getParent();
        if (parentView == null) {
            return;
        }

        final int parentWidth = parentView.getWidth();
        final int parentHeight = parentView.getHeight();
        final int width = getWidth();
        final int height = getHeight();
        if (parentWidth > 0 && parentHeight > 0 && width > 0 && height > 0) {
            final float x = getX();
            final float y = getY();

            final float minX = 0;
            final float minY = 0;
            final float maxX = parentWidth - width;
            final float maxY = parentHeight - height;

            float adjustX = MathUtils.clamp(x - dx, minX, maxX);
            float adjustY = MathUtils.clamp(y - dy, minY, maxY);

            MSIMUikitLog.i("== x:%s, y:%s, dx:%s, dy:%s, adjustX:%s, adjustY:%s",
                    x, y, dx, dy, adjustX, adjustY);

            if (adjustX >= 0 && adjustY >= 0) {
                setX(adjustX);
                setY(adjustY);
            }
        }
    }

}
