package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class PagerRecyclerView extends RecyclerView {

    public PagerRecyclerView(@NonNull Context context) {
        super(context);
    }

    public PagerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PagerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (mAwaysDisallowInterceptTouchEvent) {
            return false;
        }

        if (getScrollState() == SCROLL_STATE_IDLE) {
            if (e.getPointerCount() >= 2) {
                return false;
            }
        }

        return super.onInterceptTouchEvent(e);
    }

    private boolean mAwaysDisallowInterceptTouchEvent;

    public void requestAwaysDisallowInterceptTouchEvent(boolean awaysDisallowInterceptTouchEvent) {
        mAwaysDisallowInterceptTouchEvent = awaysDisallowInterceptTouchEvent;
    }

}
