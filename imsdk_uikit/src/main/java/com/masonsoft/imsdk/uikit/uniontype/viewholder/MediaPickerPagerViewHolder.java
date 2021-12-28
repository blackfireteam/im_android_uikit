package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.ItemClickUnionTypeAdapter;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplMediaPickerPagerBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public class MediaPickerPagerViewHolder extends UnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplMediaPickerPagerBinding mBinding;

    public MediaPickerPagerViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_media_picker_pager);
        mBinding = ImsdkUikitUnionTypeImplMediaPickerPagerBinding.bind(itemView);
    }

    @Override
    public void onBindUpdate() {
        final DataObject itemObject = (DataObject) this.itemObject;
        Preconditions.checkNotNull(itemObject);
        final MediaData.MediaInfo mediaInfo = (MediaData.MediaInfo) itemObject.object;
        final MediaData mediaData = itemObject.getExtObjectObject1(null);

        MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onBindUpdate uri:%s", mediaInfo.uri);

        if (mediaInfo.isVideoMimeType()) {
            ViewUtil.setVisibilityIfChanged(mBinding.videoFlag, View.VISIBLE);
            mBinding.durationText.setText(formatDuration(mediaInfo.durationMs));
        } else {
            ViewUtil.setVisibilityIfChanged(mBinding.videoFlag, View.GONE);
            mBinding.durationText.setText(null);
        }

        // grid image resize 80dp
        mBinding.image.setImageUrl(
                ImageRequestBuilder.newBuilderWithSource(mediaInfo.uri)
                        .setResizeOptions(ResizeOptions.forSquareSize(DimenUtil.dp2px(80)))
                        .setCacheChoice(ImageRequest.CacheChoice.DEFAULT)
                        .build(),
                ImageRequestBuilder.newBuilderWithSource(mediaInfo.uri)
                        .build()
        );

        mBinding.image.setOnClickListener(v -> {
            if (itemObject.getExtHolderItemClick1() != null) {
                itemObject.getExtHolderItemClick1().onItemClick(MediaPickerPagerViewHolder.this);
            }

            if (host.getAdapter() instanceof ItemClickUnionTypeAdapter) {
                final ItemClickUnionTypeAdapter adapter = (ItemClickUnionTypeAdapter) host.getAdapter();
                if (adapter.getOnItemClickListener() != null) {
                    adapter.getOnItemClickListener().onItemClick(MediaPickerPagerViewHolder.this);
                }
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
