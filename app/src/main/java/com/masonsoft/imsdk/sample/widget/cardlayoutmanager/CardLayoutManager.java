package com.masonsoft.imsdk.sample.widget.cardlayoutmanager;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CardLayoutManager extends RecyclerView.LayoutManager {

    private static final int LAYOUT_MAX_COUNT = 4;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public void scrollToPosition(int position) {
        // ignore
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        // ignore
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int adapterCount = state.getItemCount();
        if (adapterCount <= 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        detachAndScrapAttachedViews(recycler);

        final int start = Math.min(LAYOUT_MAX_COUNT, adapterCount);
        for (int i = start - 1; i >= 0; i--) {
            final View childView = recycler.getViewForPosition(i);
            addView(childView);
            measureChildWithMargins(childView, 0, 0);
            int childViewDecoratedWidth = getDecoratedMeasuredWidth(childView);
            int childViewDecoratedHeight = getDecoratedMeasuredHeight(childView);

            final int x = (int) (getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight() - childViewDecoratedWidth) / 2f);
            final int y = (int) (getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom() - childViewDecoratedHeight) / 2f);

            layoutDecoratedWithMargins(
                    childView,
                    x,
                    y,
                    x + childViewDecoratedWidth,
                    y + childViewDecoratedHeight
            );
        }
    }

}
