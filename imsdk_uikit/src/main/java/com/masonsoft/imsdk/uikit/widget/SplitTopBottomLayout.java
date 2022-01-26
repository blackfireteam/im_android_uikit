package com.masonsoft.imsdk.uikit.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

import com.masonsoft.imsdk.uikit.R;

import io.github.idonans.appcontext.AppContext;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.core.util.Preconditions;

/**
 * 上下两部分，下部分支持部分收起与滑动展开。例如：用于选择当前地理位置页面。
 */
public class SplitTopBottomLayout extends ViewGroup {

    public SplitTopBottomLayout(Context context) {
        this(context, null);
    }

    public SplitTopBottomLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplitTopBottomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SplitTopBottomLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        AppContext.setContextInEditMode(this);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private int mMinBottomHeight;
    private int mMaxTopHeight;

    private boolean mCollapsed;

    @NonNull
    private ValueAnimator mCollapseAnimator;

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mMinBottomHeight = DimenUtil.dp2px(220);
        mMaxTopHeight = DimenUtil.dp2px(180);
        mCollapsed = true;

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SplitTopBottomLayout, defStyleAttr,
                defStyleRes);
        mMinBottomHeight = a.getDimensionPixelSize(R.styleable.SplitTopBottomLayout_minBottomHeight, mMinBottomHeight);
        mMaxTopHeight = a.getDimensionPixelSize(R.styleable.SplitTopBottomLayout_maxTopHeight, mMaxTopHeight);
        mCollapsed = a.getBoolean(R.styleable.SplitTopBottomLayout_collapsed, mCollapsed);
        Preconditions.checkArgument(mMinBottomHeight >= 0);
        Preconditions.checkArgument(mMaxTopHeight >= 0);
        a.recycle();

        mCollapseAnimator = new ValueAnimator();
        mCollapseAnimator.setFloatValues(mCollapsed ? 0 : 1);
        mCollapseAnimator.setDuration(200);
        mCollapseAnimator.addUpdateListener(animation -> syncAnimatePercent());
        mCollapseAnimator.setInterpolator(new DecelerateInterpolator());
    }

    private float getAnimatePercent() {
        final Object value = mCollapseAnimator.getAnimatedValue();
        if (value != null) {
            return (float) value;
        }

        return mCollapsed ? 0 : 1;
    }

    private void syncAnimatePercent() {
        syncAnimatePercent(getAnimatePercent());
    }

    // percent: 0 -> 完全收起
    // percent: 1 -> 完全展开
    private void syncAnimatePercent(@FloatRange(from = 0, to = 1) float percent) {
        requestLayout();
    }

    private int calBottomSize() {
        int height = getMeasuredHeight();
        int minOffsetY = Math.min(mMaxTopHeight, height);
        int maxOffsetY = height - Math.min(mMinBottomHeight, height);
        if (maxOffsetY < minOffsetY) {
            // 越界，取平均，展开与收起时一致
            minOffsetY = maxOffsetY = (minOffsetY + maxOffsetY) / 2;
        }

        // [0 - 1] [收起 - 展开]
        float animatePercent = getAnimatePercent();

        int offsetY = (int) (minOffsetY + (maxOffsetY - minOffsetY) * (1 - animatePercent));
        int bottomSize = height - offsetY;
        Preconditions.checkArgument(bottomSize >= 0);
        return bottomSize;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        if (childCount >= 1) {
            final View child0 = getChildAt(0);
            child0.layout(0, 0, child0.getMeasuredWidth(), child0.getMeasuredHeight());
        }
        if (childCount >= 2) {
            final View child1 = getChildAt(1);
            child1.layout(0, getHeight() - child1.getMeasuredHeight(),
                    child1.getMeasuredWidth(), getHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int bottomSize = calBottomSize();

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int childWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

        final int childCount = getChildCount();
        Preconditions.checkArgument(childCount <= 2);
        if (childCount >= 1) {
            final View child0 = getChildAt(0);
            child0.measure(childWidth,
                    MeasureSpec.makeMeasureSpec(height - bottomSize, MeasureSpec.EXACTLY));
        }
        if (childCount >= 2) {
            final View child1 = getChildAt(1);
            child1.measure(childWidth,
                    MeasureSpec.makeMeasureSpec(bottomSize, MeasureSpec.EXACTLY));
        }
    }

    /**
     * 设置展开或者收起
     *
     * @param collapse true 表示收起，否则表示展开
     */
    public void setCollapse(boolean collapse) {
        this.setCollapse(collapse, true);
    }

    /**
     * @return 如果当前处于收起状态返回 true, 否则返回 false.
     */
    public boolean isCollapsed() {
        return mCollapsed;
    }

    /**
     * 设置展开或者收起
     *
     * @param collapse true 表示收起，否则表示展开
     * @param smooth   是否平滑切换
     */
    public void setCollapse(boolean collapse, boolean smooth) {
        this.setCollapseInternal(collapse, smooth);
    }

    private void setCollapseInternal(boolean collapse, boolean smooth) {
        if (mCollapsed != collapse) {
            mCollapsed = collapse;

            if (mOnCollapseChangedListener != null) {
                mOnCollapseChangedListener.onCollapseChanged(mCollapsed);
            }
        }
        mCollapseAnimator.cancel();
        if (smooth) {
            mCollapseAnimator.setFloatValues(getAnimatePercent(), mCollapsed ? 0 : 1);
            mCollapseAnimator.start();
        } else {
            mCollapseAnimator.setFloatValues(mCollapsed ? 0 : 1);
            syncAnimatePercent();
        }
    }

    public interface OnCollapseChangedListener {
        void onCollapseChanged(boolean collapse);
    }

    private OnCollapseChangedListener mOnCollapseChangedListener;

    public void setOnCollapseChangedListener(OnCollapseChangedListener listener) {
        mOnCollapseChangedListener = listener;
    }

}
