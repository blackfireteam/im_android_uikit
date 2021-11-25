package com.masonsoft.imsdk.uikit.uniontype.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.ItemClickUnionTypeAdapter;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.mediapicker.UnionTypeMediaData;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitUnionTypeImplMediaPickerGridBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

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
        //noinspection unchecked
        final DataObject<MediaData.MediaInfo> itemObject = (DataObject<MediaData.MediaInfo>) this.itemObject;
        Preconditions.checkNotNull(itemObject);
        MediaData.MediaInfo mediaInfo = itemObject.object;
        MediaData mediaData = itemObject.getExtObjectObject1(null);
        UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject2(null);

        if (mediaInfo.isVideoMimeType()) {
            ViewUtil.setVisibilityIfChanged(mBinding.videoFlag, View.VISIBLE);
            mBinding.durationText.setText(formatDuration(mediaInfo.durationMs));
        } else {
            ViewUtil.setVisibilityIfChanged(mBinding.videoFlag, View.GONE);
            mBinding.durationText.setText(null);
        }
        mBinding.image.setImageUrl(null, mediaInfo.uri.toString());
        int selectedIndex = mediaData.indexOfSelected(mediaInfo);
        if (selectedIndex >= 0) {
            mBinding.flagSelect.setSelected(true);
            mBinding.flagSelectText.setText(String.valueOf(selectedIndex + 1));
        } else {
            mBinding.flagSelect.setSelected(false);
            mBinding.flagSelectText.setText(null);
        }

        ViewUtil.onClick(mBinding.flagSelect, v -> {
            int currentSelectedIndex = mediaData.indexOfSelected(mediaInfo);
            if (currentSelectedIndex >= 0) {
                // 取消选中
                if (mediaData.mediaSelector.canDeselect(mediaData.mediaInfoListSelected, currentSelectedIndex, mediaInfo)) {
                    mediaData.mediaInfoListSelected.remove(mediaInfo);
                }
            } else {
                // 选中
                if (mediaData.mediaSelector.canSelect(mediaData.mediaInfoListSelected, mediaInfo)) {
                    mediaData.mediaInfoListSelected.add(mediaInfo);
                }
            }
            host.getAdapter().notifyDataSetChanged();
            if (unionTypeMediaData != null) {
                unionTypeMediaData.childClick();
            }
        });
        ViewUtil.onClick(itemView, v -> {
            if (itemObject.getExtHolderItemClick1() != null) {
                itemObject.getExtHolderItemClick1().onItemClick(MediaPickerGridViewHolder.this);
            }
            if (host.getAdapter() instanceof ItemClickUnionTypeAdapter) {
                final ItemClickUnionTypeAdapter adapter = (ItemClickUnionTypeAdapter) host.getAdapter();
                if (adapter.getOnItemClickListener() != null) {
                    adapter.getOnItemClickListener().onItemClick(MediaPickerGridViewHolder.this);
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
