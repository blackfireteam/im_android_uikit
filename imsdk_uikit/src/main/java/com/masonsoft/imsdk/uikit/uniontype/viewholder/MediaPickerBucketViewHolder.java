package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.ItemClickUnionTypeAdapter;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplMediaPickerBucketBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeViewHolder;

public class MediaPickerBucketViewHolder extends UnionTypeViewHolder {

    private final ImsdkUikitUnionTypeImplMediaPickerBucketBinding mBinding;

    public MediaPickerBucketViewHolder(@NonNull Host host) {
        super(host, R.layout.imsdk_uikit_union_type_impl_media_picker_bucket);
        mBinding = ImsdkUikitUnionTypeImplMediaPickerBucketBinding.bind(itemView);
    }

    @Override
    public void onBind(int position, @NonNull Object originObject) {
        //noinspection unchecked
        final DataObject<MediaData.MediaBucket> itemObject = (DataObject<MediaData.MediaBucket>) originObject;
        //
        final MediaData.MediaBucket mediaBucket = itemObject.object;
        final MediaData mediaData = itemObject.getExtObjectObject1(null);
        String url = null;
        if (mediaBucket.cover != null) {
            url = mediaBucket.cover.uri.toString();
        }
        mBinding.image.setImageUrl(null, url);
        mBinding.count.setText(String.valueOf(mediaBucket.mediaInfoList.size()));
        if (mediaBucket.allMediaInfo) {
            mBinding.title.setText(R.string.imsdk_sample_custom_soft_keyboard_item_media_bucket_all);
        } else {
            mBinding.title.setText(mediaBucket.bucketDisplayName);
        }
        ViewUtil.onClick(itemView, v -> {
            if (itemObject.getExtHolderItemClick1() != null) {
                itemObject.getExtHolderItemClick1().onItemClick(MediaPickerBucketViewHolder.this);
            }

            if (host.getAdapter() instanceof ItemClickUnionTypeAdapter) {
                ItemClickUnionTypeAdapter adapter = (ItemClickUnionTypeAdapter) host.getAdapter();
                if (adapter.getOnItemClickListener() != null) {
                    adapter.getOnItemClickListener().onItemClick(MediaPickerBucketViewHolder.this);
                }
            }
        });
    }

}
