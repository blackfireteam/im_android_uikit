package com.masonsoft.imsdk.sample.widget.cardlayoutmanager;

import android.graphics.Canvas;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.RecyclerView;

public class CardTouchCallbackImpl extends CardLayoutItemTouchHelper.Callback {

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final int childCount = recyclerView.getChildCount();
        final boolean isFirstChild = childCount > 0 && recyclerView.getChildAt(0) == viewHolder.itemView;

        // 第一个 child view 不能滑动
        final int swipeFlags;
        if (childCount <= 0 || isFirstChild) {
            swipeFlags = 0;
        } else {
            swipeFlags = CardLayoutItemTouchHelper.LEFT | CardLayoutItemTouchHelper.RIGHT;
        }

        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction, Object payload) {
    }

    private final Interpolator SWIPING_INTERPOLATOR = new DecelerateInterpolator(2.0f);

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        float progress = Math.abs(dX) / recyclerView.getWidth();
        progress = MathUtils.clamp(progress, 0f, 1f);
        progress = SWIPING_INTERPOLATOR.getInterpolation(progress);
        float dXProgress = Math.signum(dX) * progress;
        onSwiping(recyclerView, viewHolder, dX, dY, dXProgress);
    }

    /**
     * @param dXProgress 水平滚动进度，范围 [-1, 1], 复数表示向左滑动进度，正数表示向右滑动进度
     */
    public void onSwiping(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          float dX, float dY, float dXProgress) {
    }

}
