package com.masonsoft.imsdk.uikit.common.microlifecycle;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import java.util.Collection;

public class CenterRecyclerViewMicroLifecycleComponentManager extends RecyclerViewMicroLifecycleComponentManager {

    public CenterRecyclerViewMicroLifecycleComponentManager(@NonNull RecyclerView recyclerView, @NonNull Lifecycle lifecycle) {
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

            // bug fix: 删除一个 item 时, 刷新页面的 resume 状态。
            // 由于 RecyclerView 中的 child view 还没有更新到最新状态(与删除动画有关)，
            // 此处做出一组延迟校验
            requestDispatchLifecycleEventAgain(50L);
            requestDispatchLifecycleEventAgain(500L);
            requestDispatchLifecycleEventAgain(1500L);
            requestDispatchLifecycleEventAgain(5000L);
        });
    }

    @Override
    public void close() {
        super.close();
        mRecyclerView.setRecyclerListener(null);
    }

    @Override
    protected boolean isViewHolderInPerfectArea(@NonNull MicroLifecycleComponent microLifecycleComponent, @NonNull RecyclerView.ViewHolder viewHolder) {
        int width = mRecyclerView.getWidth();
        int height = mRecyclerView.getHeight();

        View centerChildView = mRecyclerView.findChildViewUnder(width / 2f, height / 2f);
        return centerChildView == viewHolder.itemView;
    }

}
