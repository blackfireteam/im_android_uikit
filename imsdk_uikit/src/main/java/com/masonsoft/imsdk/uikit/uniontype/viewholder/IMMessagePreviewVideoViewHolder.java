package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManagerHost;
import com.masonsoft.imsdk.uikit.common.microlifecycle.RecyclerViewMicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.widget.IMMessagePreviewVideoView;

import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;

public class IMMessagePreviewVideoViewHolder extends IMMessageViewHolder {

    private final IMMessagePreviewVideoView mPreviewVideoView;

    @Nullable
    private LocalMicroLifecycle mLocalMicroLifecycle;

    public IMMessagePreviewVideoViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_sample_union_type_impl_im_message_preview_video);
        mPreviewVideoView = itemView.findViewById(R.id.preview_video_view);
    }

    @Override
    protected void onBindItemObject(int position, @NonNull DataObject<MSIMMessage> itemObject) {
        super.onBindItemObject(position, itemObject);

        final MSIMMessage message = itemObject.object;

        mPreviewVideoView.setMessage(message);
        mPreviewVideoView.setOnActionCloseClickListener(() -> {
            UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
            if (listener != null) {
                listener.onItemClick(this);
            }
        });

        // 判断是否有第一次自动播放
        if (itemObject.getExtObjectBoolean1(false)) {
            itemObject.putExtObjectBoolean1(false);
            mPreviewVideoView.setAllowResumedOnce(true);
        } else {
            mPreviewVideoView.setAllowResumedOnce(false);
        }

        createLocalMicroLifecycle();
    }

    private void createLocalMicroLifecycle() {
        if (mLocalMicroLifecycle == null) {
            UnionTypeAdapter adapter = host.getAdapter();
            if (adapter instanceof MicroLifecycleComponentManagerHost) {
                MicroLifecycleComponentManager microLifecycleComponentManager = ((MicroLifecycleComponentManagerHost) adapter).getMicroLifecycleComponentManager();
                if (microLifecycleComponentManager != null) {
                    mLocalMicroLifecycle = createLocalMicroLifecycle(microLifecycleComponentManager);
                }
            }
        }
    }

    @Nullable
    protected LocalMicroLifecycle createLocalMicroLifecycle(@NonNull MicroLifecycleComponentManager microLifecycleComponentManager) {
        return new LocalMicroLifecycle(microLifecycleComponentManager);
    }

    protected class LocalMicroLifecycle extends RecyclerViewMicroLifecycleComponentManager.ViewHolderMicroLifecycleComponent {

        public LocalMicroLifecycle(@NonNull MicroLifecycleComponentManager microLifecycleComponentManager) {
            super(microLifecycleComponentManager);
        }

        @Nullable
        @Override
        public RecyclerView.ViewHolder getViewHolder() {
            return IMMessagePreviewVideoViewHolder.this;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (mPreviewVideoView != null) {
                mPreviewVideoView.performCreate();
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            if (mPreviewVideoView != null) {
                mPreviewVideoView.performStart();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mPreviewVideoView != null) {
                mPreviewVideoView.performResume();
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            if (mPreviewVideoView != null) {
                mPreviewVideoView.performPause();
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            if (mPreviewVideoView != null) {
                mPreviewVideoView.performStop();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mLocalMicroLifecycle = null;
            if (mPreviewVideoView != null) {
                mPreviewVideoView.performDestroy();
            }
        }
    }

}
