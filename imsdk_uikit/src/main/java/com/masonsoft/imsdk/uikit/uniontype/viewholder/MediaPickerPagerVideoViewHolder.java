package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.UnionTypeMediaData;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManagerHost;
import com.masonsoft.imsdk.uikit.common.microlifecycle.RecyclerViewMicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplMediaPickerPagerVideoBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public class MediaPickerPagerVideoViewHolder extends UnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplMediaPickerPagerVideoBinding mBinding;
    private final int mGridImageResize;
    private final int mVideoControlPadding;

    @Nullable
    private LocalMicroLifecycle mLocalMicroLifecycle;

    public MediaPickerPagerVideoViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_media_picker_pager_video);
        mBinding = ImsdkUikitUnionTypeImplMediaPickerPagerVideoBinding.bind(itemView);
        mGridImageResize = ContextUtil.getContext().getResources().getDimensionPixelSize(R.dimen.imsdk_uikit_media_picker_image_grid_size);

        mVideoControlPadding = DimenUtil.dp2px(50);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MediaData.MediaInfo mediaInfo = itemObject.getObject(MediaData.MediaInfo.class);
        final UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject1(null);
        Preconditions.checkNotNull(mediaInfo);
        Preconditions.checkNotNull(unionTypeMediaData);

        mBinding.videoView.setVideoUri(mediaInfo.uri, mGridImageResize);
        mBinding.videoView.setOnControlViewVisibilityChangedListener(visibility -> {
            final UnionTypeViewHolderListeners.OnItemClickPayloadListener listener = itemObject.getExtHolderItemClickPayload1();
            if (listener != null) {
                listener.onItemClick(MediaPickerPagerVideoViewHolder.this, visibility);
            }
        });
        mBinding.videoView.updateActionCloseFeature(false);
        mBinding.videoView.updateControlViewFeature(0, mVideoControlPadding, 0, mVideoControlPadding);

        // 判断是否有第一次自动播放
        if (itemObject.getExtObjectBoolean1(false)) {
            itemObject.putExtObjectBoolean1(false);
            mBinding.videoView.setAllowResumedOnce(true);
        } else {
            mBinding.videoView.setAllowResumedOnce(false);
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
            return MediaPickerPagerVideoViewHolder.this;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            mBinding.videoView.performCreate();
        }

        @Override
        public void onStart() {
            super.onStart();
            mBinding.videoView.performStart();
        }

        @Override
        public void onResume() {
            super.onResume();
            mBinding.videoView.performResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            mBinding.videoView.performPause();
        }

        @Override
        public void onStop() {
            super.onStop();
            mBinding.videoView.performStop();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mLocalMicroLifecycle = null;
            mBinding.videoView.performDestroy();
        }
    }

}
