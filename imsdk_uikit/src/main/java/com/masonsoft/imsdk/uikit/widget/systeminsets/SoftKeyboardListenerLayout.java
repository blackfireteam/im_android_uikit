package com.masonsoft.imsdk.uikit.widget.systeminsets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.systeminsets.SystemInsetsFrameLayout;
import io.github.idonans.systeminsets.SystemInsetsLayoutHelper;

public class SoftKeyboardListenerLayout extends SystemInsetsFrameLayout {

    public SoftKeyboardListenerLayout(Context context) {
        this(context, null);
    }

    public SoftKeyboardListenerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SoftKeyboardListenerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SoftKeyboardListenerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public interface OnSoftKeyboardChangedListener {
        void onSoftKeyboardShown();

        void onSoftKeyboardHidden();
    }

    private OnSoftKeyboardChangedListener mOnSoftKeyboardChangedListener;

    public void setOnSoftKeyboardChangedListener(OnSoftKeyboardChangedListener onSoftKeyboardChangedListener) {
        mOnSoftKeyboardChangedListener = onSoftKeyboardChangedListener;
    }

    private final int mSoftKeyboardMinHeight = DimenUtil.dp2px(80);

    public boolean isSoftKeyboardShown() {
        return getLastSystemInsets().bottom >= mSoftKeyboardMinHeight;
    }

    @Override
    protected SystemInsetsLayoutHelper createFitInsetsLayoutHelper() {
        return new SystemInsetsLayoutHelper(this) {

            private int mLastBottom;

            @Override
            public Rect dispatchSystemInsets(int left, int top, int right, int bottom) {
                Rect result = super.dispatchSystemInsets(left, top, right, bottom);

                if (mLastBottom == bottom) {
                    return result;
                }
                mLastBottom = bottom;

                if (mOnSoftKeyboardChangedListener != null) {
                    if (bottom >= mSoftKeyboardMinHeight) {
                        mOnSoftKeyboardChangedListener.onSoftKeyboardShown();
                    } else {
                        mOnSoftKeyboardChangedListener.onSoftKeyboardHidden();
                    }
                }

                return result;
            }
        };
    }

    public interface OnDispatchTouchEventListener {
        void onDispatchTouchEvent(MotionEvent event);
    }

    public abstract static class FirstMoveOrUpTouchEventListener implements OnDispatchTouchEventListener {

        private final int mTouchSlop = ViewConfiguration.get(ContextUtil.getContext()).getScaledTouchSlop();
        private boolean mAllow;
        private float mDownRawX;
        private float mDownRawY;

        @Override
        public void onDispatchTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                mAllow = true;
                mDownRawX = event.getRawX();
                mDownRawY = event.getRawY();
                return;
            }
            if (!mAllow) {
                return;
            }

            if (action == MotionEvent.ACTION_MOVE) {
                float rawX = event.getRawX();
                float rawY = event.getRawY();
                float dx = Math.abs(rawX - mDownRawX);
                float dy = Math.abs(rawY - mDownRawY);
                if (dx >= mTouchSlop || dy >= mTouchSlop) {
                    mAllow = false;
                    onFirstMoveOrUpTouchEvent(event, dx, dy);
                }
            } else if (action == MotionEvent.ACTION_UP) {
                mAllow = false;
                onFirstMoveOrUpTouchEvent(event, 0, 0);
            }
        }

        public abstract void onFirstMoveOrUpTouchEvent(MotionEvent event, float dx, float dy);

    }

    private final List<OnDispatchTouchEventListener> mOnDispatchTouchEventListeners = new ArrayList<>();

    public void addOnDispatchTouchEventListener(OnDispatchTouchEventListener listener) {
        if (!mOnDispatchTouchEventListeners.contains(listener)) {
            mOnDispatchTouchEventListeners.add(listener);
        }
    }

    public void removeOnDispatchTouchEventListener(OnDispatchTouchEventListener listener) {
        mOnDispatchTouchEventListeners.remove(listener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (OnDispatchTouchEventListener listener : mOnDispatchTouchEventListeners) {
            if (listener != null) {
                listener.onDispatchTouchEvent(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
