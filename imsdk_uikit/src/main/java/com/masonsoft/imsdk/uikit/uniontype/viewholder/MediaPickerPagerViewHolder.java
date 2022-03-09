package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.UnionTypeMediaData;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplMediaPickerPagerBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public class MediaPickerPagerViewHolder extends UnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplMediaPickerPagerBinding mBinding;
    private final int mGridImageResize;

    public MediaPickerPagerViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_media_picker_pager);
        mBinding = ImsdkUikitUnionTypeImplMediaPickerPagerBinding.bind(itemView);
        mGridImageResize = ContextUtil.getContext().getResources().getDimensionPixelSize(R.dimen.imsdk_uikit_media_picker_image_grid_size);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = getItemObject(DataObject.class);
        Preconditions.checkNotNull(itemObject);
        final MediaData.MediaInfo mediaInfo = itemObject.getObject(MediaData.MediaInfo.class);
        final UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject1(null);
        Preconditions.checkNotNull(mediaInfo);
        Preconditions.checkNotNull(unionTypeMediaData);

        if (mediaInfo.isVideoMimeType()) {
            ViewUtil.setVisibilityIfChanged(mBinding.videoFlag, View.VISIBLE);
            mBinding.durationText.setText(formatDuration(mediaInfo.durationMs));
        } else {
            ViewUtil.setVisibilityIfChanged(mBinding.videoFlag, View.GONE);
            mBinding.durationText.setText(null);
        }

        mBinding.image.setImageUrl(
                ImageRequestBuilder.newBuilderWithSource(mediaInfo.uri)
                        .setResizeOptions(ResizeOptions.forSquareSize(mGridImageResize))
                        .setCacheChoice(ImageRequest.CacheChoice.DEFAULT)
                        .build(),
                ImageRequestBuilder.newBuilderWithSource(mediaInfo.uri)
                        .build()
        );

        ViewUtil.onClick(mBinding.image, v -> {
            if (itemObject.getExtHolderItemClick1() != null) {
                itemObject.getExtHolderItemClick1().onItemClick(MediaPickerPagerViewHolder.this);
            }
        });
    }

    private String formatDuration(long durationMs) {
        final long durationS = (long) Math.ceil(durationMs / 1000f);
        final long min = durationS / 60;
        final long s = durationS % 60;
        return min + ":" + s;
    }

}
