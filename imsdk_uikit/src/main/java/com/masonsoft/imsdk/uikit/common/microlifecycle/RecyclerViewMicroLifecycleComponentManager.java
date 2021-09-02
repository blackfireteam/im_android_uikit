package com.masonsoft.imsdk.uikit.common.microlifecycle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.uikit.MSIMUikitLog;

import io.github.idonans.core.thread.Threads;

public abstract class RecyclerViewMicroLifecycleComponentManager extends MicroLifecycleComponentManager {

    protected final RecyclerView mRecyclerView;

    public RecyclerViewMicroLifecycleComponentManager(@NonNull RecyclerView recyclerView, @NonNull Lifecycle lifecycle) {
        super(lifecycle);
        mRecyclerView = recyclerView;
        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (DEBUG) {
                MSIMUikitLog.v("onScrollStateChanged newState:%s", newState);
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (DEBUG) {
                MSIMUikitLog.v("onScrolled dx:%s, dy:%s", dx, dy);
            }

            requestDispatchLifecycleEventAgain(0L);

            if (dx == 0 && dy == 0 && recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                // bug fix: 删除一个 item 时，可能会触发 dx, dy 都是 0 的 onScrolled,
                // 由于 RecyclerView 中的 child view 还没有更新到最新状态(与删除动画有关)，
                // 此处做出一组延迟校验
                requestDispatchLifecycleEventAgain(50L);
                requestDispatchLifecycleEventAgain(500L);
                requestDispatchLifecycleEventAgain(1500L);
                requestDispatchLifecycleEventAgain(5000L);
            }
        }
    };

    protected void requestDispatchLifecycleEventAgain(long delay) {
        Threads.postUi(() -> {
            if (mLifecycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                dispatchLifecycleEvent(Lifecycle.Event.ON_RESUME);
            }
        }, Math.max(0, delay));
    }

    @Override
    public void close() {
        super.close();
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
    }

    @Override
    public boolean isInPerfectArea(@NonNull MicroLifecycleComponent microLifecycleComponent) {
        if (!mLifecycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            return false;
        }
        if (mRecyclerView == null) {
            return false;
        }
        if (!(microLifecycleComponent instanceof ViewHolderMicroLifecycleComponent)) {
            return false;
        }
        int width = mRecyclerView.getWidth();
        int height = mRecyclerView.getHeight();
        if (width <= 0 || height <= 0) {
            return false;
        }

        RecyclerView.ViewHolder viewHolder = ((ViewHolderMicroLifecycleComponent) microLifecycleComponent).getViewHolder();
        if (viewHolder == null) {
            return false;
        }
        return isViewHolderInPerfectArea(microLifecycleComponent, viewHolder);
    }

    protected abstract boolean isViewHolderInPerfectArea(@NonNull MicroLifecycleComponent microLifecycleComponent,
                                                         @NonNull RecyclerView.ViewHolder viewHolder);

    public static abstract class ViewHolderMicroLifecycleComponent extends MicroLifecycleComponent {

        public ViewHolderMicroLifecycleComponent(@NonNull MicroLifecycleComponentManager microLifecycleComponentManager) {
            super(microLifecycleComponentManager);
        }

        @Nullable
        public abstract RecyclerView.ViewHolder getViewHolder();

    }

}
