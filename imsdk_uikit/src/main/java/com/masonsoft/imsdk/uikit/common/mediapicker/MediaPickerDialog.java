package com.masonsoft.imsdk.uikit.common.mediapicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.LocalViewPager2Helper;
import androidx.viewpager2.widget.ViewPager2;

import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.microlifecycle.CenterRecyclerViewMicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManagerHost;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonMediaPickerDialogBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonMediaPickerDialogBucketViewBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitCommonMediaPickerDialogPagerViewBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.widget.GridItemDecoration;

import java.util.List;

import io.github.idonans.backstack.ViewBackLayer;
import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.core.util.IOUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;

public class MediaPickerDialog implements MediaData.MediaLoaderCallback, ViewBackLayer.OnBackPressedListener {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private final Activity mActivity;
    private final LayoutInflater mInflater;
    @NonNull
    private final ImsdkUikitCommonMediaPickerDialogBinding mBinding;
    @NonNull
    private ViewDialog mViewDialog;

    @NonNull
    private GridView mGridView;
    @NonNull
    private BucketView mBucketView;

    @Nullable
    private PagerView mPagerView;

    @Nullable
    private UnionTypeMediaData mUnionTypeMediaData;
    private MediaData.MediaLoader mMediaLoader;

    private boolean mPendingToShowViewDialog;
    private boolean mUnionTypeMediaDataLoadFinish;

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

        mMediaLoader = new MediaData.MediaLoader(this, mInnerMediaSelector);
        mMediaLoader.start();
    }

    private final MediaSelector mInnerMediaSelector = new MediaSelector.SimpleMediaSelector() {
        @Override
        public boolean accept(@NonNull MediaData.MediaInfo info) {
            if (!super.accept(info)) {
                return false;
            }

            if (mOutMediaSelector != null) {
                //noinspection RedundantIfStatement
                if (!mOutMediaSelector.accept(info)) {
                    return false;
                }
            }

            return true;
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

    private void onUnionTypeMediaDataLoadFinish() {
        mUnionTypeMediaDataLoadFinish = true;
        Preconditions.checkNotNull(mUnionTypeMediaData);
        Preconditions.checkNotNull(mUnionTypeMediaData.mediaData.bucketSelected);
        mUnionTypeMediaData.unionTypeMediaDataObservable.registerObserver(mUnionTypeMediaDataObserver);

        mGridView.syncTitleBar();
        mGridView.syncContent();

        mBucketView.syncContent();

        if (mPendingToShowViewDialog) {
            mViewDialog.show();
        }
    }

    class GridView {
        private final View mGridTopBarClose;
        private final View mGridTopBarTitle;
        private final TextView mGridTopBarTitleText;
        private final View mGridTopBarTitleArrow;
        private final TextView mGridTopBarSubmit;
        private final RecyclerView mGridRecyclerView;

        private final UnionTypeAdapter mGridDataAdapter;

        private GridView(ImsdkUikitCommonMediaPickerDialogBinding parentBinding) {
            mGridTopBarClose = parentBinding.gridTopBarClose;
            mGridTopBarTitle = parentBinding.gridTopBarTitle;
            mGridTopBarTitleText = parentBinding.gridTopBarTitleText;
            mGridTopBarTitleArrow = parentBinding.gridTopBarTitleArrow;
            mGridTopBarSubmit = parentBinding.gridTopBarSubmit;
            mGridRecyclerView = parentBinding.gridRecyclerView;

            mGridRecyclerView.setItemAnimator(null);
            mGridRecyclerView.setLayoutManager(
                    new GridLayoutManager(mGridRecyclerView.getContext(), 4));
            mGridRecyclerView.setHasFixedSize(true);
            mGridRecyclerView.addItemDecoration(new GridItemDecoration(4, DimenUtil.dp2px(2), false));
            mGridDataAdapter = new UnionTypeAdapter();
            mGridDataAdapter.setHost(Host.Factory.create(mActivity, mGridRecyclerView, mGridDataAdapter));
            mGridDataAdapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
            mGridRecyclerView.setAdapter(mGridDataAdapter);

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
            ViewUtil.onClick(mGridTopBarSubmit, v -> MediaPickerDialog.this.submit());
        }

        public void syncTitleBar() {
            Preconditions.checkNotNull(mUnionTypeMediaData);
            Preconditions.checkNotNull(mUnionTypeMediaData.mediaData.bucketSelected);
            String bucketSelectedName = I18nResources.getString(R.string.imsdk_uikit_custom_soft_keyboard_item_media_bucket_all);
            if (mUnionTypeMediaData.mediaData.bucketSelected != null
                    && !mUnionTypeMediaData.mediaData.bucketSelected.allMediaInfo) {
                bucketSelectedName = mUnionTypeMediaData.mediaData.bucketSelected.bucketDisplayName;
            }
            mGridTopBarTitleText.setText(bucketSelectedName);

            syncSharedSubmitButton(mGridTopBarSubmit);
        }

        public void syncContent() {
            Preconditions.checkNotNull(mUnionTypeMediaData);
            Preconditions.checkNotNull(mUnionTypeMediaData.mediaData.bucketSelected);
            final List<UnionTypeItemObject> gridItems = mUnionTypeMediaData.unionTypeGridItemsMap.get(mUnionTypeMediaData.mediaData.bucketSelected);
            mGridDataAdapter.getData()
                    .beginTransaction()
                    .add((transaction, groupArrayList) -> groupArrayList.setGroupItems(0, gridItems))
                    .commit();
        }
    }

    private void syncSharedSubmitButton(TextView submitButton) {
        if (mUnionTypeMediaData == null) {
            return;
        }

        boolean enable;
        int count;
        if (mInnerMediaSelector.canFinishSelect(mUnionTypeMediaData.mediaData.mediaInfoListSelected)) {
            count = mUnionTypeMediaData.mediaData.mediaInfoListSelected.size();
            enable = true;
        } else {
            count = mUnionTypeMediaData.mediaData.mediaInfoListSelected.size();
            enable = false;
        }

        if (count > 0) {
            submitButton.setText(I18nResources.getString(R.string.imsdk_uikit_custom_soft_keyboard_item_media_picker_submit_format, count));
        } else {
            submitButton.setText(I18nResources.getString(R.string.imsdk_uikit_custom_soft_keyboard_item_media_picker_submit_0));
        }
        submitButton.setEnabled(enable);
    }

    private void submit() {
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
    }

    private class BucketView {
        private final ViewDialog mBucketViewDialog;
        private final RecyclerView mRecyclerView;
        private final UnionTypeAdapter mDataAdapter;

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
            mDataAdapter = new UnionTypeAdapter();
            mDataAdapter.setHost(Host.Factory.create(mActivity, mRecyclerView, mDataAdapter));
            mDataAdapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
            mRecyclerView.setAdapter(mDataAdapter);
        }

        public void syncContent() {
            Preconditions.checkNotNull(mUnionTypeMediaData);
            Preconditions.checkNotNull(mUnionTypeMediaData.mediaData.bucketSelected);
            mDataAdapter.getData()
                    .beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        groupArrayList.setGroupItems(0, mUnionTypeMediaData.unionTypeBucketItems);
                    })
                    .commit();
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

    public void showBucketView() {
        mBucketView.show();
    }

    public void hideBucketView() {
        mBucketView.hide();
    }

    public void showPagerView(int position) {
        if (mUnionTypeMediaData == null) {
            return;
        }

        if (mPagerView == null) {
            mPagerView = new PagerView(mBinding);
        }

        List<UnionTypeItemObject> pagerItems = mUnionTypeMediaData.unionTypePagerItemsMap.get(mUnionTypeMediaData.mediaData.bucketSelected);
        final PagerView finalPagerView = mPagerView;
        finalPagerView.mDataAdapter.getData()
                .beginTransaction()
                .add((transaction, groupArrayList) -> groupArrayList.setGroupItems(0, pagerItems))
                .commit(() -> {
                    if (mPagerView == finalPagerView) {
                        finalPagerView.mViewPager.setCurrentItem(position, false);
                        finalPagerView.show();
                    }
                });
    }

    public void hidePagerView() {
        if (mPagerView != null) {
            mPagerView.hide();
        }
        mPagerView = null;
    }

    public void togglePagerViewActionBar() {
        if (mPagerView == null) {
            return;
        }
        mPagerView.toggleActionBar();
    }

    public void setPagerViewActionBarVisibility(int visibility) {
        if (mPagerView == null) {
            return;
        }
        mPagerView.setActionBarVisibility(visibility);
    }

    private class PagerView implements ViewBackLayer.OnHideListener {

        private final ViewDialog mPagerViewDialog;
        @SuppressWarnings("FieldCanBeLocal")
        private final ImsdkUikitCommonMediaPickerDialogPagerViewBinding mBinding;
        private final ViewPager2 mViewPager;
        private final UnionTypeAdapter mDataAdapter;
        private boolean mShowActionBar = true;
        private final int mDiffY = DimenUtil.dp2px(100);
        private MicroLifecycleComponentManager mMicroLifecycleComponentManager;

        private PagerView(ImsdkUikitCommonMediaPickerDialogBinding parentBinding) {
            final ViewGroup parentView = parentBinding.pagerOverlayContainer;
            mPagerViewDialog = new ViewDialog.Builder(mActivity)
                    .setParentView(parentView)
                    .setContentView(R.layout.imsdk_uikit_common_media_picker_dialog_pager_view)
                    .setOnHideListener(this)
                    .create();
            //noinspection ConstantConditions
            mBinding = ImsdkUikitCommonMediaPickerDialogPagerViewBinding.bind(mPagerViewDialog.getContentView());
            mViewPager = mBinding.viewPager;
            mMicroLifecycleComponentManager = new CenterRecyclerViewMicroLifecycleComponentManager(LocalViewPager2Helper.getRecyclerView(mViewPager), ((FragmentActivity) mActivity).getLifecycle());
            mDataAdapter = new UnionTypeAdapterImpl();
            mDataAdapter.setHost(Host.Factory.create(mActivity, LocalViewPager2Helper.getRecyclerView(mViewPager), mDataAdapter));
            mDataAdapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
            mViewPager.setAdapter(mDataAdapter);
            syncActionBar();

            mViewPager.registerOnPageChangeCallback(mOnPageChangeCallback);

            ViewUtil.onClick(mBinding.pagerTopBarClose, v -> PagerView.this.hide());
            ViewUtil.onClick(mBinding.pagerTopBarSubmit, v -> MediaPickerDialog.this.submit());
            ViewUtil.onClick(mBinding.pagerBottomBarFlagSelectContainer, v -> PagerView.this.toggleSelected());
            syncActionBarContent();
        }

        private class UnionTypeAdapterImpl extends UnionTypeAdapter implements MicroLifecycleComponentManagerHost {
            @Override
            public MicroLifecycleComponentManager getMicroLifecycleComponentManager() {
                return mMicroLifecycleComponentManager;
            }
        }

        @SuppressWarnings("FieldCanBeLocal")
        private final ViewPager2.OnPageChangeCallback mOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                Threads.postUi(PagerView.this::syncActionBarContent);
            }
        };

        public void setActionBarVisibility(int visibility) {
            mShowActionBar = visibility == View.VISIBLE;
            syncActionBar();
        }

        public void toggleActionBar() {
            mShowActionBar = !mShowActionBar;
            syncActionBar();
        }

        private void syncActionBar() {
            if (mShowActionBar) {
                mBinding.pagerTopBar.animate().translationY(0).start();
                mBinding.pagerBottomBar.animate().translationY(0).start();
            } else {
                mBinding.pagerTopBar.animate().translationY(-mDiffY).start();
                mBinding.pagerBottomBar.animate().translationY(mDiffY).start();
            }
        }

        @SuppressLint("SetTextI18n")
        private void syncActionBarContent() {
            syncSharedSubmitButton(mBinding.pagerTopBarSubmit);

            final int currentItem = mViewPager.getCurrentItem();
            if (currentItem >= 0) {
                final UnionTypeItemObject unionTypeItemObject = mDataAdapter.getItem(currentItem);
                if (unionTypeItemObject != null) {
                    final DataObject itemObject = unionTypeItemObject.getItemObject(DataObject.class);
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

                    if (currentUnionTypeMediaData != mUnionTypeMediaData) {
                        return;
                    }

                    int selectedIndex = currentUnionTypeMediaData.mediaData.indexOfSelected(mediaInfo);
                    if (selectedIndex >= 0) {
                        mBinding.pagerBottomBarFlagSelectText.setSelected(true);
                        mBinding.pagerBottomBarFlagSelectText.setText(String.valueOf(selectedIndex + 1));
                    } else {
                        mBinding.pagerBottomBarFlagSelectText.setSelected(false);
                        mBinding.pagerBottomBarFlagSelectText.setText(null);
                    }
                    mBinding.pagerBottomBarFlagSelectContainer.setEnabled(true);

                    mBinding.pagerTopBarTitleText.setText((currentItem + 1) + "/" + mDataAdapter.getItemCount());
                    return;
                }
            }

            mBinding.pagerBottomBarFlagSelectText.setSelected(false);
            mBinding.pagerBottomBarFlagSelectText.setText(null);
            mBinding.pagerTopBarTitleText.setText(null);
            mBinding.pagerBottomBarFlagSelectContainer.setEnabled(false);
        }

        private void toggleSelected() {
            final int currentItem = mViewPager.getCurrentItem();
            if (currentItem < 0) {
                return;
            }
            final UnionTypeItemObject unionTypeItemObject = mDataAdapter.getItem(currentItem);
            if (unionTypeItemObject == null) {
                return;
            }
            final DataObject itemObject = unionTypeItemObject.getItemObject(DataObject.class);
            if (itemObject == null) {
                return;
            }

            final MediaData.MediaInfo mediaInfo = itemObject.getObject(MediaData.MediaInfo.class);
            final UnionTypeMediaData unionTypeMediaData = itemObject.getExtObjectObject1(null);
            if (mediaInfo == null) {
                return;
            }
            if (unionTypeMediaData != mUnionTypeMediaData) {
                return;
            }

            // 切换选中状态
            boolean notifyChanged = false;
            int currentSelectedIndex = unionTypeMediaData.mediaData.indexOfSelected(mediaInfo);
            if (currentSelectedIndex >= 0) {
                // 取消选中
                if (unionTypeMediaData.mediaData.mediaSelector.canDeselect(unionTypeMediaData.mediaData.mediaInfoListSelected, currentSelectedIndex, mediaInfo)) {
                    unionTypeMediaData.mediaData.mediaInfoListSelected.remove(mediaInfo);
                    notifyChanged = true;
                }
            } else {
                // 选中
                if (unionTypeMediaData.mediaData.mediaSelector.canSelect(unionTypeMediaData.mediaData.mediaInfoListSelected, mediaInfo)) {
                    unionTypeMediaData.mediaData.mediaInfoListSelected.add(mediaInfo);
                    notifyChanged = true;
                }
            }

            if (notifyChanged) {
                unionTypeMediaData.unionTypeMediaDataObservable.notifyMediaInfoSelectedChanged(unionTypeMediaData);
            }
        }

        public void show() {
            mPagerViewDialog.show();
        }

        public void hide() {
            mPagerViewDialog.hide(false);
        }

        @Override
        public void onHide(boolean cancel) {
            if (mPagerView == this) {
                mPagerView = null;
            }

            IOUtil.closeQuietly(mMicroLifecycleComponentManager);
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
        mPendingToShowViewDialog = true;
        if (mUnionTypeMediaDataLoadFinish) {
            mViewDialog.show();
        }
    }

    public void hide() {
        mPendingToShowViewDialog = false;
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
            onUnionTypeMediaDataLoadFinish();
        });
    }

    @Override
    public boolean onBackPressed() {
        if (mPagerView != null && mPagerView.onBackPressed()) {
            return true;
        }

        if (mBucketView.onBackPressed()) {
            return true;
        }

        return false;
    }

    public interface OnMediaPickListener {
        /**
         * 关闭 MediaPicker，返回 true.
         *
         * @param mediaInfoList 当前选择的多媒体信息
         * @return 关闭 MediaPicker，返回 true.
         */
        boolean onMediaPick(@NonNull List<MediaData.MediaInfo> mediaInfoList);
    }

    private OnMediaPickListener mOnMediaPickListener;

    public void setOnMediaPickListener(OnMediaPickListener listener) {
        mOnMediaPickListener = listener;
    }

    private final UnionTypeMediaDataObservable.UnionTypeMediaDataObserver mUnionTypeMediaDataObserver = new UnionTypeMediaDataObservable.UnionTypeMediaDataObserver() {
        @Override
        public void onBucketSelectedChanged(UnionTypeMediaData unionTypeMediaData) {
            if (mUnionTypeMediaData != unionTypeMediaData) {
                return;
            }

            mGridView.syncTitleBar();
            mGridView.syncContent();
        }

        @Override
        public void onMediaInfoSelectedChanged(UnionTypeMediaData unionTypeMediaData) {
            mGridView.syncTitleBar();

            if (mPagerView != null) {
                mPagerView.syncActionBarContent();
            }
        }
    };

}
