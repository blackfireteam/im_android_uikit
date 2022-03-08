package com.masonsoft.imsdk.uikit.common.mediapicker;

import androidx.annotation.NonNull;

import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.idonans.uniontype.UnionTypeItemObject;

public class UnionTypeMediaData {

    @NonNull
    final MediaData mediaData;
    @NonNull
    final Map<MediaData.MediaBucket, List<UnionTypeItemObject>> unionTypeGridItemsMap;
    @NonNull
    final Map<MediaData.MediaBucket, List<UnionTypeItemObject>> unionTypePagerItemsMap;
    @NonNull
    final List<UnionTypeItemObject> unionTypeBucketItems;

    public int pagerPendingIndex;
    MediaPickerDialog dialog;

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
                                .putExtObjectObject1(this.mediaData)
                                .putExtObjectObject2(UnionTypeMediaData.this)));

                pagerItems.add(new UnionTypeItemObject(
                        IMUikitUnionTypeMapper.UNION_TYPE_IMPL_MEDIA_PICKER_PAGER,
                        new DataObject(mediaInfo)
                                .putExtObjectObject1(this.mediaData)
                                .putExtObjectObject2(UnionTypeMediaData.this)));
            }

            this.unionTypeGridItemsMap.put(bucket, gridItems);
            this.unionTypePagerItemsMap.put(bucket, pagerItems);
            unionTypeBucketItems.add(new UnionTypeItemObject(
                    IMUikitUnionTypeMapper.UNION_TYPE_IMPL_MEDIA_PICKER_BUCKET,
                    new DataObject(bucket)
                            .putExtObjectObject1(this.mediaData)
                            .putExtObjectObject2(UnionTypeMediaData.this)));
        }
    }

    public void childClick() {
        dialog.mGridView.updateConfirmSubmitStatus();
    }
}
