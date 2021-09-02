package com.masonsoft.imsdk.uikit.common.microlifecycle;

import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import java.util.Collection;

public class VisibleRecyclerViewMicroLifecycleComponentManager extends RecyclerViewMicroLifecycleComponentManager {

    public VisibleRecyclerViewMicroLifecycleComponentManager(@NonNull RecyclerView recyclerView, @NonNull Lifecycle lifecycle) {
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
        final RectF childRect = new RectF();
        final View child = viewHolder.itemView;
        final float translationX = child.getTranslationX();
        final float translationY = child.getTranslationY();
        childRect.set(child.getLeft() + translationX,
                child.getTop() + translationY,
                child.getRight() + translationX,
                child.getBottom() + translationY);
        return childRect.intersect(0, 0, mRecyclerView.getWidth(), mRecyclerView.getHeight());
    }

}
