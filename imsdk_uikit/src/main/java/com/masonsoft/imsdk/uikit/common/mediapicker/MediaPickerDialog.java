package com.masonsoft.imsdk.uikit.common.mediapicker;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.ItemClickUnionTypeAdapter;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonMediaPickerDialogBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonMediaPickerDialogBucketViewBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonMediaPickerDialogPagerViewBinding;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.widget.GridItemDecoration;

import java.util.List;

import io.github.idonans.backstack.ViewBackLayer;
import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeItemObject;

public class MediaPickerDialog implements MediaData.MediaLoaderCallback, ViewBackLayer.OnBackPressedListener {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private final Activity mActivity;
    private final LayoutInflater mInflater;
    private final ImsdkUikitCommonMediaPickerDialogBinding mBinding;
    private ViewDialog mViewDialog;
    public GridView mGridView;
    private BucketView mBucketView;
    private PagerView mPagerView;

    @Nullable
    private UnionTypeMediaData mUnionTypeMediaData;
    private MediaData.MediaLoader mMediaLoader;

    public MediaPickerDialog(Activity activity, ViewGroup parentView) {
        mActivity = activity;
        mInflater = activity.getLayoutInflater();
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_uikit_common_media_picker_dialog)
                .defaultAnimation()
                .setOnBackPressedListener(this)
                .setParentView(parentView)
                .dimBackground(true)
                .create();
        mBinding = ImsdkUikitCommonMediaPickerDialogBinding.bind(mViewDialog.getContentView());

        mGridView = new GridView(mBinding);
        mBucketView = new BucketView(mBinding);
        mPagerView = new PagerView(mBinding);

        mMediaLoader = new MediaData.MediaLoader(this, mInnerMediaSelector);
        mMediaLoader.start();
    }

    private final MediaSelector mInnerMediaSelector = new MediaSelector.SimpleMediaSelector() {
        @Override
        public boolean accept(@NonNull MediaData.MediaInfo info) {
            if (mOutMediaSelector != null) {
                if (!mOutMediaSelector.accept(info)) {
                    return false;
                }
            }
            return super.accept(info);
        }

        @Override
        public boolean canSelect(@NonNull List<MediaData.MediaInfo> mediaInfoListSelected, @NonNull MediaData.MediaInfo info) {
            if (mOutMediaSelector != null) {
                if (!mOutMediaSelector.canSelect(mediaInfoListSelected, info)) {
                    return false;
                }
            }
            return super.canSelect(mediaInfoListSelected, info);
        }

        @Override
        public boolean canDeselect(@NonNull List<MediaData.MediaInfo> mediaInfoListSelected, int currentSelectedIndex, @NonNull MediaData.MediaInfo info) {
            if (mOutMediaSelector != null) {
                if (!mOutMediaSelector.canDeselect(mediaInfoListSelected, currentSelectedIndex, info)) {
                    return false;
                }
            }
            return super.canDeselect(mediaInfoListSelected, currentSelectedIndex, info);
        }

        @Override
        public boolean canFinishSelect(@NonNull List<MediaData.MediaInfo> mediaInfoListSelected) {
            if (mOutMediaSelector != null) {
                if (!mOutMediaSelector.canFinishSelect(mediaInfoListSelected)) {
                    return false;
                }
            }
            return super.canFinishSelect(mediaInfoListSelected);
        }
    };

    @Nullable
    private MediaSelector mOutMediaSelector;

    public void setMediaSelector(@Nullable MediaSelector mediaSelector) {
        mOutMediaSelector = mediaSelector;
    }

    private void notifyMediaDataChanged() {
        if (mUnionTypeMediaData == null) {
            MSIMUikitLog.e("notifyMediaDataChanged mUnionTypeMediaData is null");
            return;
        }
        if (mUnionTypeMediaData.mediaData.bucketSelected != null) {
            List<UnionTypeItemObject> gridItems = mUnionTypeMediaData.unionTypeGridItemsMap.get(mUnionTypeMediaData.mediaData.bucketSelected);
            mGridView.mDataAdapter.getData()
                    .beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        // mGridView.mDataAdapter.setGroupItems(0, gridItems);
                        groupArrayList.setGroupItems(0, gridItems);
                    })
                    .commit();

            List<UnionTypeItemObject> pagerItems = mUnionTypeMediaData.unionTypePagerItemsMap.get(mUnionTypeMediaData.mediaData.bucketSelected);
            mPagerView.mDataAdapter.getData()
                    .beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        // mPagerView.mDataAdapter.setGroupItems(0, pagerItems);
                        groupArrayList.setGroupItems(0, pagerItems);
                    })
                    .commit(() -> {
                        mPagerView.mRecyclerView.getLayoutManager().scrollToPosition(mUnionTypeMediaData.pagerPendingIndex);
                    });

        }
        mBucketView.mDataAdapter.getData()
                .beginTransaction()
                .add((transaction, groupArrayList) -> {
                    //mBucketView.mDataAdapter.setGroupItems(0, mUnionTypeMediaData.unionTypeBucketItems);
                    groupArrayList.setGroupItems(0, mUnionTypeMediaData.unionTypeBucketItems);
                })
                .commit();

        String bucketSelectedName = I18nResources.getString(R.string.imsdk_uikit_custom_soft_keyboard_item_media);
        if (mUnionTypeMediaData.mediaData.bucketSelected != null
                && !mUnionTypeMediaData.mediaData.bucketSelected.allMediaInfo) {
            bucketSelectedName = mUnionTypeMediaData.mediaData.bucketSelected.bucketDisplayName;
        }
        mGridView.mGridTopBarTitle.setText(bucketSelectedName);
        mGridView.updateConfirmNextStatus();
    }

    class GridView {
        private final View mGridTopBarClose;
        private final TextView mGridTopBarTitle;
        private final RecyclerView mRecyclerView;
        private final TextView mActionSubmit;

        private final ItemClickUnionTypeAdapter mDataAdapter;

        private GridView(ImsdkUikitCommonMediaPickerDialogBinding parentBinding) {
            mGridTopBarClose = parentBinding.gridTopBarClose;
            mGridTopBarTitle = parentBinding.gridTopBarTitle;
            mRecyclerView = parentBinding.gridRecyclerView;
            mActionSubmit = parentBinding.actionSubmit;

            mRecyclerView.setLayoutManager(
                    new GridLayoutManager(mRecyclerView.getContext(), 3));
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new GridItemDecoration(3, DimenUtil.dp2px(2), false));
            mDataAdapter = new ItemClickUnionTypeAdapter();
            mDataAdapter.setHost(Host.Factory.create(mActivity, mRecyclerView, mDataAdapter));
            mDataAdapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
            mDataAdapter.setOnItemClickListener(viewHolder -> {
                Preconditions.checkNotNull(mUnionTypeMediaData);
                final int position = viewHolder.getAdapterPosition();
                mUnionTypeMediaData.pagerPendingIndex = Math.max(position, 0);
                notifyMediaDataChanged();
                mPagerView.mDataAdapter.getData().beginTransaction().add((transaction, groupArrayList) -> {
                    // ignore
                }).commit(() -> mPagerView.show());
            });

            mRecyclerView.setAdapter(mDataAdapter);

            ViewUtil.onClick(mGridTopBarClose, v -> {
                if (MediaPickerDialog.this.onBackPressed()) {
                    return;
                }
                MediaPickerDialog.this.hide();
            });
            ViewUtil.onClick(mGridTopBarTitle, v -> {
                if (mBucketView.onBackPressed()) {
                    return;
                }
                mBucketView.show();
            });
            ViewUtil.onClick(mActionSubmit, v -> {
                if (mUnionTypeMediaData == null) {
                    MSIMUikitLog.e("mUnionTypeMediaData is null");
                    return;
                }
                if (mUnionTypeMediaData.mediaData.mediaInfoListSelected.isEmpty()) {
                    MSIMUikitLog.e("mUnionTypeMediaData.mediaData.mediaInfoListSelected.isEmpty()");
                    return;
                }

                if (mOnMediaPickListener == null) {
                    MSIMUikitLog.v("ignore. mOnMediaPickListener is null.");
                    return;
                }
                if (mOnMediaPickListener.onMediaPick(mUnionTypeMediaData.mediaData.mediaInfoListSelected)) {
                    MediaPickerDialog.this.hide();
                }
            });
        }

        public void updateConfirmNextStatus() {
            boolean enable;
            int count;
            if (mUnionTypeMediaData == null) {
                MSIMUikitLog.e("mUnionTypeMediaData is null");
                count = 0;
                enable = false;
            } else if (mInnerMediaSelector.canFinishSelect(mUnionTypeMediaData.mediaData.mediaInfoListSelected)) {
                count = mUnionTypeMediaData.mediaData.mediaInfoListSelected.size();
                enable = true;
            } else {
                count = mUnionTypeMediaData.mediaData.mediaInfoListSelected.size();
                enable = false;
            }
            mActionSubmit.setText(I18nResources.getString(R.string.imsdk_uikit_custom_soft_keyboard_item_media_picker_submit_format, count));
            mActionSubmit.setEnabled(enable);
        }
    }

    private class BucketView {
        private final ViewDialog mBucketViewDialog;
        private final RecyclerView mRecyclerView;
        private final ItemClickUnionTypeAdapter mDataAdapter;

        private BucketView(ImsdkUikitCommonMediaPickerDialogBinding parentBinding) {
            final ViewGroup parentView = parentBinding.bucketOverlayContainer;
            mBucketViewDialog = new ViewDialog.Builder(mActivity)
                    .setParentView(parentView)
                    .setContentView(R.layout.imsdk_uikit_common_media_picker_dialog_bucket_view)
                    .setContentViewShowAnimation(R.anim.backstack_slide_in_from_top)
                    .setContentViewHideAnimation(R.anim.backstack_slide_out_to_top)
                    .dimBackground(true)
                    .create();
            //noinspection ConstantConditions
            final ImsdkUikitCommonMediaPickerDialogBucketViewBinding binding =
                    ImsdkUikitCommonMediaPickerDialogBucketViewBinding.bind(mBucketViewDialog.getContentView());
            mRecyclerView = binding.recyclerView;

            mRecyclerView.setLayoutManager(
                    new LinearLayoutManager(mRecyclerView.getContext()));
            mRecyclerView.setHasFixedSize(false);
            mDataAdapter = new ItemClickUnionTypeAdapter();
            mDataAdapter.setHost(Host.Factory.create(mActivity, mRecyclerView, mDataAdapter));
            mDataAdapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
            mDataAdapter.setOnItemClickListener(viewHolder -> {
                Preconditions.checkNotNull(mUnionTypeMediaData);
                int size = mUnionTypeMediaData.mediaData.allSubBuckets.size();
                final int position = viewHolder.getAdapterPosition();
                if ((position < 0 || position >= size)) {
                    MSIMUikitLog.e("BucketView onItemClick invalid position: %s, size:%s", position, size);
                    BucketView.this.hide();
                    return;
                }

                MediaData.MediaBucket mediaBucket = mUnionTypeMediaData.mediaData.allSubBuckets.get(position);
                if (ObjectsCompat.equals(mUnionTypeMediaData.mediaData.bucketSelected, mediaBucket)) {
                    if (DEBUG) {
                        MSIMUikitLog.v("BucketView onItemClick ignore. same as last bucket selected");
                    }
                    BucketView.this.hide();
                    return;
                }

                BucketView.this.hide();
                mUnionTypeMediaData.mediaData.bucketSelected = mediaBucket;
                notifyMediaDataChanged();
            });

            mRecyclerView.setAdapter(mDataAdapter);
        }

        public void show() {
            mBucketViewDialog.show();
        }

        public void hide() {
            mBucketViewDialog.hide(false);
        }

        public boolean onBackPressed() {
            if (mBucketViewDialog.isShown()) {
                mBucketViewDialog.hide(false);
                return true;
            }
            return false;
        }
    }

    private class PagerView {

        private final ViewDialog mPagerViewDialog;
        @SuppressWarnings("FieldCanBeLocal")
        private final ImsdkUikitCommonMediaPickerDialogPagerViewBinding mBinding;
        private final RecyclerView mRecyclerView;
        private final ItemClickUnionTypeAdapter mDataAdapter;

        private PagerView(ImsdkUikitCommonMediaPickerDialogBinding parentBinding) {
            final ViewGroup parentView = parentBinding.pagerOverlayContainer;
            mPagerViewDialog = new ViewDialog.Builder(mActivity)
                    .setParentView(parentView)
                    .setContentView(R.layout.imsdk_uikit_common_media_picker_dialog_pager_view)
                    .create();
            //noinspection ConstantConditions
            mBinding = ImsdkUikitCommonMediaPickerDialogPagerViewBinding.bind(mPagerViewDialog.getContentView());
            mRecyclerView = mBinding.recyclerView;
            mRecyclerView.setLayoutManager(
                    new LinearLayoutManager(mRecyclerView.getContext(), RecyclerView.HORIZONTAL, false));
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
            PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
            pagerSnapHelper.attachToRecyclerView(mRecyclerView);
            mDataAdapter = new ItemClickUnionTypeAdapter();
            mDataAdapter.setHost(Host.Factory.create(mActivity, mRecyclerView, mDataAdapter));
            mDataAdapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
            mDataAdapter.setOnItemClickListener(viewHolder -> PagerView.this.hide());
            mRecyclerView.setAdapter(mDataAdapter);
        }

        public void show() {
            mPagerViewDialog.show();
        }

        public void hide() {
            mPagerViewDialog.hide(false);
        }

        public boolean onBackPressed() {
            if (mPagerViewDialog.isShown()) {
                mPagerViewDialog.hide(false);
                return true;
            }
            return false;
        }
    }

    public void show() {
        Threads.postUi(() -> mViewDialog.show(), 220L);
    }

    public void hide() {
        mViewDialog.hide(false);
        mMediaLoader.close();
    }

    @Override
    public void onLoadFinish(@NonNull MediaData mediaData) {
        if (DEBUG) {
            MSIMUikitLog.v("onLoadFinish buckets:%s, media info list map size:%s", mediaData.allSubBuckets.size(), mediaData.allMediaInfoListMap.size());
        }
        mediaData.bucketSelected = mediaData.allMediaInfoListBucket;
        UnionTypeMediaData unionTypeMediaData = new UnionTypeMediaData(this, mediaData);
        Threads.runOnUi(() -> {
            mUnionTypeMediaData = unionTypeMediaData;
            notifyMediaDataChanged();
        });
    }

    @Override
    public boolean onBackPressed() {
        if (mBucketView.onBackPressed()) {
            return true;
        }

        if (mPagerView.onBackPressed()) {
            return true;
        }

        return false;
    }

    public interface OnMediaPickListener {
        /**
         * 关闭 MediaPicker，返回 true.
         */
        boolean onMediaPick(@NonNull List<MediaData.MediaInfo> mediaInfoList);
    }

    private OnMediaPickListener mOnMediaPickListener;

    public void setOnMediaPickListener(OnMediaPickListener listener) {
        mOnMediaPickListener = listener;
    }

}
