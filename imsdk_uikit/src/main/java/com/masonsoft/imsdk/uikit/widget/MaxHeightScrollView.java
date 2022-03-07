package com.masonsoft.imsdk.uikit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.masonsoft.imsdk.uikit.R;

public class MaxHeightScrollView extends NestedScrollView {

    public MaxHeightScrollView(@NonNull Context context) {
        this(context, null);
    }

    public MaxHeightScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaxHeightScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr, 0);
    }

    private int mMaxHeight;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView, defStyleAttr,
                defStyleRes);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.MaxHeightScrollView_android_maxHeight, mMaxHeight);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mMaxHeight > 0) {
            int height = getMeasuredHeight();
            if (height > mMaxHeight) {
                int width = getMeasuredWidth();
                super.onMeasure(
                        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.EXACTLY)
                );
            }
        }
    }

    public void setMaxHeight(int maxHeight) {
        if (mMaxHeight != maxHeight) {
            mMaxHeight = maxHeight;
            requestLayout();
        }
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

}
