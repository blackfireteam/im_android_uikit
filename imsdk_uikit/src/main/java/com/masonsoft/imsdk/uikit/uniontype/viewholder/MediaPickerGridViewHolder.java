package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.UnionTypeMediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.UnionTypeMediaDataObservable;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplMediaPickerGridBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public class MediaPickerGridViewHolder extends UnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplMediaPickerGridBinding mBinding;

    public MediaPickerGridViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_media_picker_grid);
        mBinding = ImsdkUikitUnionTypeImplMediaPickerGridBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        MediaData.MediaInfo mediaInfo = itemObject.getObject(MediaData.MediaInfo.class);
        UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject1(null);
        Preconditions.checkNotNull(mediaInfo);
        Preconditions.checkNotNull(unionTypeMediaData);
        unionTypeMediaData.unionTypeMediaDataObservable.registerObserver(mUnionTypeMediaDataObserver);

        if (mediaInfo.isVideoMimeType()) {
            ViewUtil.setVisibilityIfChanged(mBinding.videoFlag, View.VISIBLE);
            mBinding.durationText.setText(formatDuration(mediaInfo.durationMs));
        } else {
            ViewUtil.setVisibilityIfChanged(mBinding.videoFlag, View.GONE);
            mBinding.durationText.setText(null);
        }
        mBinding.image.setImageUrl(null, mediaInfo.uri.toString());
        int selectedIndex = unionTypeMediaData.mediaData.indexOfSelected(mediaInfo);
        if (selectedIndex >= 0) {
            showSelectedHover(false);
            mBinding.flagSelectText.setSelected(true);
            mBinding.flagSelectText.setText(String.valueOf(selectedIndex + 1));
        } else {
            hideSelectedHover(false);
            mBinding.flagSelectText.setSelected(false);
            mBinding.flagSelectText.setText(null);
        }

        ViewUtil.onClick(itemView, v -> {
            final UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
            if (listener != null) {
                listener.onItemClick(MediaPickerGridViewHolder.this);
            }
        });

        ViewUtil.onClick(mBinding.flagSelectContainer, v -> {
            final UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick2();
            if (listener != null) {
                listener.onItemClick(MediaPickerGridViewHolder.this);
            }
        });
    }

    private void showSelectedHover(boolean animate) {
        if (mBinding.selectedHover.getAlpha() > 0.9f) {
            mBinding.selectedHover.animate().cancel();
            mBinding.selectedHover.setAlpha(1f);
            return;
        }

        if (animate) {
            mBinding.selectedHover.animate().alpha(1f).start();
        } else {
            mBinding.selectedHover.animate().cancel();
            mBinding.selectedHover.setAlpha(1f);
        }
    }

    private void hideSelectedHover(boolean animate) {
        if (mBinding.selectedHover.getAlpha() < 0.1f) {
            mBinding.selectedHover.animate().cancel();
            mBinding.selectedHover.setAlpha(0f);
            return;
        }

        if (animate) {
            mBinding.selectedHover.animate().alpha(0f).start();
        } else {
            mBinding.selectedHover.animate().cancel();
            mBinding.selectedHover.setAlpha(0f);
        }
    }

    private String formatDuration(long durationMs) {
        final long durationS = (long) Math.ceil(durationMs / 1000f);
        final long min = durationS / 60;
        final long s = durationS % 60;
        return min + ":" + s;
    }

    private final UnionTypeMediaDataObservable.UnionTypeMediaDataObserver mUnionTypeMediaDataObserver = new UnionTypeMediaDataObservable.UnionTypeMediaDataObserver() {
        @Override
        public void onBucketSelectedChanged(UnionTypeMediaData unionTypeMediaData) {
            // ignore
        }

        @Override
        public void onMediaInfoSelectedChanged(UnionTypeMediaData unionTypeMediaData) {
            final DataObject itemObject = getItemObject(DataObject.class);
            if (itemObject == null) {
                return;
            }
            final MediaData.MediaInfo mediaInfo = itemObject.getObject(MediaData.MediaInfo.class);
            if (mediaInfo == null) {
                return;
            }

            final UnionTypeMediaData currentUnionTypeMediaData = itemObject.getExtObjectObject1(null);
            if (currentUnionTypeMediaData == null) {
                return;
            }

            if (currentUnionTypeMediaData != unionTypeMediaData) {
                return;
            }

            int selectedIndex = unionTypeMediaData.mediaData.indexOfSelected(mediaInfo);
            if (selectedIndex >= 0) {
                showSelectedHover(true);
                mBinding.flagSelectText.setSelected(true);
                mBinding.flagSelectText.setText(String.valueOf(selectedIndex + 1));
            } else {
                hideSelectedHover(true);
                mBinding.flagSelectText.setSelected(false);
                mBinding.flagSelectText.setText(null);
            }
        }
    };

}
