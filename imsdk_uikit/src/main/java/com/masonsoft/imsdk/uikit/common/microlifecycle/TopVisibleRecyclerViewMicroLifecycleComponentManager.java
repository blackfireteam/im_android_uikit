package com.masonsoft.imsdk.uikit.common.microlifecycle;

import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import java.util.Collection;

public class TopVisibleRecyclerViewMicroLifecycleComponentManager extends RecyclerViewMicroLifecycleComponentManager {

    public TopVisibleRecyclerViewMicroLifecycleComponentManager(@NonNull RecyclerView recyclerView, @NonNull Lifecycle lifecycle) {
        super(recyclerView, lifecycle);
        mRecyclerView.setRecyclerListener(holder -> {
            if (DEBUG) {
                MSIMUikitLog.v("setRecyclerListener callback");
            }
            Collection<MicroLifecycleComponent> microLifecycleComponents = copyComponents();
            for (MicroLifecycleComponent microLifecycleComponent : microLifecycleComponents) {
                if (microLifecycleComponent instanceof ViewHolderMicroLifecycleComponent) {
                    if (((ViewHolderMicroLifecycleComponent) microLifecycleComponent).getViewHolder() == holder) {
                        microLifecycleComponent.getLifecycleRegistry().handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void close() {
        super.close();
        mRecyclerView.setRecyclerListener(null);
    }

    @Override
    protected boolean isViewHolderInPerfectArea(@NonNull MicroLifecycleComponent microLifecycleComponent, @NonNull RecyclerView.ViewHolder viewHolder) {
        View topVisibleChildView = findTopVisibleChildView();
        return topVisibleChildView == viewHolder.itemView;
    }

    @Nullable
    private View findTopVisibleChildView() {
        final int count = mRecyclerView.getChildCount();
        final RectF childRect = new RectF();
        for (int i = count - 1; i >= 0; i--) {
            final View child = mRecyclerView.getChildAt(i);
            final float translationX = child.getTranslationX();
            final float translationY = child.getTranslationY();
            childRect.set(child.getLeft() + translationX,
                    child.getTop() + translationY,
                    child.getRight() + translationX,
                    child.getBottom() + translationY);
            if (childRect.intersect(0, 0, mRecyclerView.getWidth(), mRecyclerView.getHeight())) {
                return child;
            }
        }
        return null;
    }

}
