package com.masonsoft.imsdk.uikit.common.impreview;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.common.microlifecycle.CenterRecyclerViewMicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManagerHost;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.widget.PagerRecyclerView;

import java.util.List;

import io.github.idonans.backstack.ViewBackLayer;
import io.github.idonans.backstack.dialog.ViewDialog;
import io.github.idonans.core.util.IOUtil;
import io.github.idonans.dynamic.page.UnionTypeStatusPageView;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;

public class IMBaseMessageImageOrVideoPreviewDialog implements ViewBackLayer.OnBackPressedListener {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;
    private final ViewDialog mViewDialog;

    private final PagerRecyclerView mRecyclerView;
    private final ViewImpl mViewImpl;
    private final MicroLifecycleComponentManager mMicroLifecycleComponentManager;

    private IMBaseMessageImageOrVideoPreviewPresenter mPresenter;

    public IMBaseMessageImageOrVideoPreviewDialog(Lifecycle lifecycle,
                                                  Activity activity,
                                                  ViewGroup parentView,
                                                  List<MSIMBaseMessage> messageList,
                                                  int index) {
        mViewDialog = new ViewDialog.Builder(activity)
                .setContentView(R.layout.imsdk_uikit_common_im_base_message_image_or_video_preview)
                .setParentView(parentView)
                .setOnBackPressedListener(this)
                .dimBackground(true)
                .setCancelable(false)
                .create();
        //noinspection ConstantConditions
        mRecyclerView = mViewDialog.getContentView().findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                mRecyclerView.getContext(),
                RecyclerView.HORIZONTAL,
                false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        mMicroLifecycleComponentManager = new CenterRecyclerViewMicroLifecycleComponentManager(mRecyclerView, lifecycle);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mRecyclerView);

        UnionTypeAdapter adapter = new UnionTypeAdapterImpl();
        adapter.setHost(Host.Factory.create(activity, mRecyclerView, adapter));
        adapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
        mViewImpl = new ViewImpl(adapter);
        clearPresenter();

        mPresenter = new IMBaseMessageImageOrVideoPreviewPresenter(mViewImpl, messageList, index);
        mViewImpl.setPresenter(mPresenter);
        mPresenter.showInitMessage();
        mRecyclerView.setAdapter(adapter);
    }

    public void show() {
        mViewDialog.show();
    }

    public void hide() {
        clearPresenter();
        IOUtil.closeQuietly(mMicroLifecycleComponentManager);
        mViewDialog.hide(false);
    }

    @Override
    public boolean onBackPressed() {
        hide();
        return true;
    }

    private class UnionTypeAdapterImpl extends UnionTypeAdapter implements MicroLifecycleComponentManagerHost {

        @Override
        public MicroLifecycleComponentManager getMicroLifecycleComponentManager() {
            return mMicroLifecycleComponentManager;
        }

    }

    private void clearPresenter() {
        if (mPresenter != null) {
            mPresenter.setAbort();
            mPresenter = null;
        }
    }

    class ViewImpl extends UnionTypeStatusPageView<Object> {
        public ViewImpl(@NonNull UnionTypeAdapter adapter) {
            super(adapter);
            setAlwaysHideNoMoreData(true);
        }

        public void hide() {
            IMBaseMessageImageOrVideoPreviewDialog.this.hide();
        }
    }

}
