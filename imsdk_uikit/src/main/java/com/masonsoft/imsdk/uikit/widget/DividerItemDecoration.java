package com.masonsoft.imsdk.uikit.widget;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL = RecyclerView.HORIZONTAL;
    public static final int VERTICAL = RecyclerView.VERTICAL;

    private final int mOrientation;

    public static final int SHOW_DIVIDER_START = 0x001;
    public static final int SHOW_DIVIDER_MIDDLE = 0x002;
    public static final int SHOW_DIVIDER_END = 0x004;

    private final int mShowDividerFlag;

    private final Drawable mDivider;

    private final Rect mBounds = new Rect();

    private static Drawable createDividerDrawable(int dividerColor,
                                                  int dividerWidth,
                                                  int dividerHeight) {
        return new ColorDrawable(dividerColor) {
            @Override
            public int getIntrinsicWidth() {
                return dividerWidth;
            }

            @Override
            public int getIntrinsicHeight() {
                return dividerHeight;
            }
        };
    }

    public DividerItemDecoration(
            int orientation,
            int showDividerFlag,
            int dividerColor,
            int dividerWidth,
            int dividerHeight) {
        this(orientation, showDividerFlag, createDividerDrawable(dividerColor, dividerWidth, dividerHeight));
    }

    public DividerItemDecoration(
            int orientation,
            int showDividerFlag,
            Drawable divider) {
        mDivider = divider;

        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException("invalid orientation, must be either HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;

        mShowDividerFlag = showDividerFlag;
    }

    @Override
    public void getItemOffsets(Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        outRect.set(0, 0, 0, 0);

        if (mDivider == null) {
            return;
        }

        final RecyclerView.ViewHolder viewHolder = parent.getChildViewHolder(view);
        int position = viewHolder.getAdapterPosition();
        if (position < 0) {
            return;
        }

        final RecyclerView.Adapter<?> adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }
        final int itemCount = adapter.getItemCount();
        if (itemCount <= 0) {
            return;
        }
        final boolean isFirstItem = position == 0;
        final boolean isLastItem = position == itemCount - 1;

        final int dividerWidth = mDivider.getIntrinsicWidth();
        final int dividerHeight = mDivider.getIntrinsicHeight();

        if (isFirstItem && ((mShowDividerFlag & SHOW_DIVIDER_START) != 0)) {
            // 第一个之前需要显示 divider
            if (mOrientation == HORIZONTAL) {
                outRect.left = dividerWidth;
            } else {
                outRect.top = dividerHeight;
            }
        }
        if (isLastItem && ((mShowDividerFlag & SHOW_DIVIDER_END) != 0)) {
            // 最后一个之后需要显示 divider
            if (mOrientation == HORIZONTAL) {
                outRect.right = dividerWidth;
            } else {
                outRect.bottom = dividerHeight;
            }
        }

        if (!isLastItem && ((mShowDividerFlag & SHOW_DIVIDER_MIDDLE) != 0)) {
            // 除最后一个外，每一个 item 之后都需要显示 divider
            if (mOrientation == HORIZONTAL) {
                outRect.right = dividerWidth;
            } else {
                outRect.bottom = dividerHeight;
            }
        }
    }

    private void getChildBoundsWithMargins(View view, Rect outBounds) {
        final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
        outBounds.set((int) (view.getLeft() - lp.leftMargin + view.getTranslationX()),
                (int) (view.getTop() - lp.topMargin + view.getTranslationY()),
                (int) (view.getRight() + lp.rightMargin + view.getTranslationX()),
                (int) (view.getBottom() + lp.bottomMargin + view.getTranslationY()));
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getLayoutManager() == null || mDivider == null) {
            return;
        }

        final int baseLeft = 0;
        final int baseRight = parent.getWidth();
        final int baseTop = 0;
        final int baseBottom = parent.getHeight();

        final RecyclerView.Adapter<?> adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }

        final int itemCount = parent.getAdapter().getItemCount();
        if (itemCount <= 0) {
            return;
        }

        final int dividerWidth = mDivider.getIntrinsicWidth();
        final int dividerHeight = mDivider.getIntrinsicHeight();

        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.ViewHolder childViewHolder = parent.getChildViewHolder(child);
            final int position = childViewHolder.getAdapterPosition();
            if (position < 0) {
                continue;
            }
            final boolean isFirstItem = position == 0;
            final boolean isLastItem = position == itemCount - 1;

            getChildBoundsWithMargins(child, mBounds);

            if (isFirstItem && ((mShowDividerFlag & SHOW_DIVIDER_START) != 0)) {
                // 第一个之前需要显示 divider

                final int left;
                final int top;
                final int right;
                final int bottom;

                if (mOrientation == HORIZONTAL) {
                    left = mBounds.left - dividerWidth;
                    top = baseTop;
                    right = left + dividerWidth;
                    bottom = baseBottom;
                } else {
                    left = baseLeft;
                    top = mBounds.top - dividerHeight;
                    right = baseRight;
                    bottom = top + dividerHeight;
                }

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (isLastItem && ((mShowDividerFlag & SHOW_DIVIDER_END) != 0)) {
                // 最后一个之后需要显示 divider

                final int left;
                final int top;
                final int right;
                final int bottom;

                if (mOrientation == HORIZONTAL) {
                    left = mBounds.right;
                    top = baseTop;
                    right = left + dividerWidth;
                    bottom = baseBottom;
                } else {
                    left = baseLeft;
                    top = mBounds.bottom;
                    right = baseRight;
                    bottom = top + dividerHeight;
                }
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }

            if (!isLastItem && ((mShowDividerFlag & SHOW_DIVIDER_MIDDLE) != 0)) {
                // 除最后一个外，每一个 item 之后都需要显示 divider

                final int left;
                final int top;
                final int right;
                final int bottom;
                if (mOrientation == HORIZONTAL) {
                    left = mBounds.right;
                    top = baseTop;
                    right = left + dividerWidth;
                    bottom = baseBottom;
                } else {
                    left = baseLeft;
                    top = mBounds.bottom;
                    right = baseRight;
                    bottom = top + dividerHeight;
                }
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }

        }
    }

}