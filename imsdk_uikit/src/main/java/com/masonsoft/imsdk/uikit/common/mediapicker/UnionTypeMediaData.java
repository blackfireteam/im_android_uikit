package com.masonsoft.imsdk.uikit.common.mediapicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.MediaPickerBucketViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.MediaPickerGridViewHolder;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.MediaPickerPagerViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.idonans.uniontype.UnionTypeItemObject;

public class UnionTypeMediaData {

    public final UnionTypeMediaDataObservable unionTypeMediaDataObservable = new UnionTypeMediaDataObservable();

    @NonNull
    public final MediaData mediaData;
    @NonNull
    public final Map<MediaData.MediaBucket, List<UnionTypeItemObject>> unionTypeGridItemsMap;
    @NonNull
    public final Map<MediaData.MediaBucket, List<UnionTypeItemObject>> unionTypePagerItemsMap;
    @NonNull
    public final List<UnionTypeItemObject> unionTypeBucketItems;

    public int pagerPendingIndex;
    public final MediaPickerDialog dialog;

    UnionTypeMediaData(MediaPickerDialog dialog, @NonNull MediaData mediaData) {
        this.dialog = dialog;
        this.mediaData = mediaData;

        this.unionTypeGridItemsMap = new HashMap<>();
        this.unionTypePagerItemsMap = new HashMap<>();
        this.unionTypeBucketItems = new ArrayList<>(this.mediaData.allSubBuckets.size());

        for (MediaData.MediaBucket bucket : this.mediaData.allSubBuckets) {
            List<UnionTypeItemObject> gridItems = new ArrayList<>(bucket.mediaInfoList.size());
            List<UnionTypeItemObject> pagerItems = new ArrayList<>(bucket.mediaInfoList.size());

            for (MediaData.MediaInfo mediaInfo : bucket.mediaInfoList) {
                gridItems.add(new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_MEDIA_PICKER_GRID,
                        new DataObject(mediaInfo)
                                .putExtObjectObject1(UnionTypeMediaData.this)
                                .putExtHolderItemClick1(UnionTypeMediaData.this::onGridItemClick1)
                                .putExtHolderItemClick2(UnionTypeMediaData.this::onGridItemClick2)
                ));

                pagerItems.add(new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_MEDIA_PICKER_PAGER,
                        new DataObject(mediaInfo)
                                .putExtObjectObject1(UnionTypeMediaData.this)
                                .putExtHolderItemClick1(UnionTypeMediaData.this::onPagerItemClick1)
                                .putExtHolderItemClick2(UnionTypeMediaData.this::onPagerItemClick2)));
            }

            this.unionTypeGridItemsMap.put(bucket, gridItems);
            this.unionTypePagerItemsMap.put(bucket, pagerItems);
            unionTypeBucketItems.add(new UnionTypeItemObject(
                    IMUikitUnionTypeMapper.UNION_TYPE_IMPL_MEDIA_PICKER_BUCKET,
                    new DataObject(bucket)
                            .putExtObjectObject1(UnionTypeMediaData.this)
                            .putExtHolderItemClick1(UnionTypeMediaData.this::onBucketItemClick)
            ));
        }
    }

    @Deprecated
    public void childClick() {
        // dialog.mGridView.updateConfirmSubmitStatus();
    }

    private void onBucketItemClick(RecyclerView.ViewHolder _holder) {
        if (!(_holder instanceof MediaPickerBucketViewHolder)) {
            return;
        }

        final MediaPickerBucketViewHolder holder = (MediaPickerBucketViewHolder) _holder;
        final DataObject itemObject = holder.getItemObject(DataObject.class);
        if (itemObject == null) {
            return;
        }

        final MediaData.MediaBucket mediaBucket = itemObject.getObject(MediaData.MediaBucket.class);
        final UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject1(null);
        if (mediaBucket == null) {
            return;
        }
        if (unionTypeMediaData != this) {
            return;
        }

        if (this.mediaData.bucketSelected != mediaBucket) {
            this.mediaData.bucketSelected = mediaBucket;
            this.unionTypeMediaDataObservable.notifyBucketSelectedChanged(this);
            this.dialog.hideBucketView();
        }
    }

    private void onGridItemClick1(RecyclerView.ViewHolder _holder) {
        if (!(_holder instanceof MediaPickerGridViewHolder)) {
            return;
        }

        final MediaPickerGridViewHolder holder = (MediaPickerGridViewHolder) _holder;
        final DataObject itemObject = holder.getItemObject(DataObject.class);
        if (itemObject == null) {
            return;
        }

        final MediaData.MediaInfo mediaInfo = itemObject.getObject(MediaData.MediaInfo.class);
        final UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject1(null);
        if (mediaInfo == null) {
            return;
        }
        if (unionTypeMediaData != this) {
            return;
        }

        // 看大图
        final int position = holder.getBindingAdapterPosition();
        if (position >= 0) {
            this.dialog.showPagerView(position);
        }
    }

    private void onGridItemClick2(RecyclerView.ViewHolder _holder) {
        if (!(_holder instanceof MediaPickerGridViewHolder)) {
            return;
        }

        final MediaPickerGridViewHolder holder = (MediaPickerGridViewHolder) _holder;
        final DataObject itemObject = holder.getItemObject(DataObject.class);
        if (itemObject == null) {
            return;
        }

        final MediaData.MediaInfo mediaInfo = itemObject.getObject(MediaData.MediaInfo.class);
        final UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject1(null);
        if (mediaInfo == null) {
            return;
        }
        if (unionTypeMediaData != this) {
            return;
        }

        // 切换选中状态
        boolean notifyChanged = false;
        int currentSelectedIndex = this.mediaData.indexOfSelected(mediaInfo);
        if (currentSelectedIndex >= 0) {
            // 取消选中
            if (this.mediaData.mediaSelector.canDeselect(this.mediaData.mediaInfoListSelected, currentSelectedIndex, mediaInfo)) {
                this.mediaData.mediaInfoListSelected.remove(mediaInfo);
                notifyChanged = true;
            }
        } else {
            // 选中
            if (this.mediaData.mediaSelector.canSelect(this.mediaData.mediaInfoListSelected, mediaInfo)) {
                this.mediaData.mediaInfoListSelected.add(mediaInfo);
                notifyChanged = true;
            }
        }

        if (notifyChanged) {
            this.unionTypeMediaDataObservable.notifyMediaInfoSelectedChanged(this);
        }
    }

    private void onPagerItemClick1(RecyclerView.ViewHolder _holder) {
        if (!(_holder instanceof MediaPickerPagerViewHolder)) {
            return;
        }

        final MediaPickerPagerViewHolder holder = (MediaPickerPagerViewHolder) _holder;
        final DataObject itemObject = holder.getItemObject(DataObject.class);
        if (itemObject == null) {
            return;
        }

        final MediaData.MediaInfo mediaInfo = itemObject.getObject(MediaData.MediaInfo.class);
        final UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject1(null);
        if (mediaInfo == null) {
            return;
        }
        if (unionTypeMediaData != this) {
            return;
        }

        // 展开或者收起操作栏
        unionTypeMediaData.dialog.togglePagerViewActionBar();
    }

    private void onPagerItemClick2(RecyclerView.ViewHolder _holder) {
        if (!(_holder instanceof MediaPickerPagerViewHolder)) {
            return;
        }

        final MediaPickerPagerViewHolder holder = (MediaPickerPagerViewHolder) _holder;
        final DataObject itemObject = holder.getItemObject(DataObject.class);
        if (itemObject == null) {
            return;
        }

        final MediaData.MediaInfo mediaInfo = itemObject.getObject(MediaData.MediaInfo.class);
        final UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject1(null);
        if (mediaInfo == null) {
            return;
        }
        if (unionTypeMediaData != this) {
            return;
        }

        // 关闭 Pager 视图
        unionTypeMediaData.dialog.hidePagerView();
    }

}
