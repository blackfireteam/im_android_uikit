package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.UnionTypeMediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.UnionTypeMediaDataObservable;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplMediaPickerBucketBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;

import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public class MediaPickerBucketViewHolder extends UnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplMediaPickerBucketBinding mBinding;

    public MediaPickerBucketViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_media_picker_bucket);
        mBinding = ImsdkUikitUnionTypeImplMediaPickerBucketBinding.bind(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindUpdate() {
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MediaData.MediaBucket mediaBucket = itemObject.getObject(MediaData.MediaBucket.class);
        final UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject1(null);
        Preconditions.checkNotNull(mediaBucket);
        Preconditions.checkNotNull(unionTypeMediaData);
        unionTypeMediaData.unionTypeMediaDataObservable.registerObserver(mUnionTypeMediaDataObserver);

        String url = null;
        if (mediaBucket.cover != null) {
            url = mediaBucket.cover.uri.toString();
        }
        mBinding.image.setImageUrl(null, url);
        mBinding.count.setText("(" + mediaBucket.mediaInfoList.size() + ")");
        if (mediaBucket.allMediaInfo) {
            mBinding.title.setText(R.string.imsdk_uikit_custom_soft_keyboard_item_media_bucket_all);
        } else {
            mBinding.title.setText(mediaBucket.bucketDisplayName);
        }

        final boolean select = mediaBucket == unionTypeMediaData.mediaData.bucketSelected;
        ViewUtil.setVisibilityIfChanged(mBinding.flagSelectOk, select ? View.VISIBLE : View.GONE);

        ViewUtil.onClick(itemView, v -> {
            final UnionTypeViewHolderListeners.OnItemClickListener listener = itemObject.getExtHolderItemClick1();
            if (listener != null) {
                listener.onItemClick(MediaPickerBucketViewHolder.this);
            }
        });
    }

    private final UnionTypeMediaDataObservable.UnionTypeMediaDataObserver mUnionTypeMediaDataObserver = new UnionTypeMediaDataObservable.UnionTypeMediaDataObserver() {
        @Override
        public void onBucketSelectedChanged(UnionTypeMediaData unionTypeMediaData) {
            final DataObject itemObject = getItemObject(DataObject.class);
            if (itemObject == null) {
                return;
            }
            final MediaData.MediaBucket mediaBucket = itemObject.getObject(MediaData.MediaBucket.class);
            if (mediaBucket == null) {
                return;
            }

            final UnionTypeMediaData currentUnionTypeMediaData = itemObject.getExtObjectObject1(null);
            if (currentUnionTypeMediaData == null) {
                return;
            }

            if (currentUnionTypeMediaData != unionTypeMediaData) {
                return;
            }

            final boolean select = mediaBucket == unionTypeMediaData.mediaData.bucketSelected;
            ViewUtil.setVisibilityIfChanged(mBinding.flagSelectOk, select ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onMediaInfoSelectedChanged(UnionTypeMediaData unionTypeMediaData) {
            // ignore
        }
    };

}
