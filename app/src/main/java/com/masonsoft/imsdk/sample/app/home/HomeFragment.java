package com.masonsoft.imsdk.sample.app.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.sample.R;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleHomeFragmentBinding;
import com.masonsoft.imsdk.sample.entity.Spark;
import com.masonsoft.imsdk.sample.uniontype.SampleUnionTypeMapper;
import com.masonsoft.imsdk.sample.uniontype.viewholder.HomeSparkLoadFailLargeViewHolder;
import com.masonsoft.imsdk.sample.uniontype.viewholder.HomeSparkLoadNoMoreDataLargeViewHolder;
import com.masonsoft.imsdk.sample.uniontype.viewholder.HomeSparkLoadingLargeViewHolder;
import com.masonsoft.imsdk.sample.uniontype.viewholder.HomeSparkViewHolder;
import com.masonsoft.imsdk.sample.widget.cardlayoutmanager.CardLayoutItemTouchHelper;
import com.masonsoft.imsdk.sample.widget.cardlayoutmanager.CardLayoutManager;
import com.masonsoft.imsdk.sample.widget.cardlayoutmanager.CardTouchCallbackImpl;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManagerHost;
import com.masonsoft.imsdk.uikit.common.microlifecycle.TopVisibleRecyclerViewMicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

import io.github.idonans.core.util.IOUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.dynamic.page.UnionTypeStatusPageView;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.systeminsets.SystemInsetsLayout;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;

/**
 * 首页
 */
public class HomeFragment extends SystemInsetsFragment {

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private ImsdkSampleHomeFragmentBinding mBinding;

    private MicroLifecycleComponentManager mMicroLifecycleComponentManager;
    private ViewImpl mView;
    private HomeFragmentPresenter mPresenter;
    @SuppressWarnings("FieldCanBeLocal")
    private CardLayoutItemTouchHelper mCardLayoutItemTouchHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SampleLog.v("onCreateView %s", getClass());

        mBinding = ImsdkSampleHomeFragmentBinding.inflate(inflater, container, false);
        mBinding.topSystemInsets.setOnSystemInsetsListener(new SystemInsetsLayout.OnSystemInsetsListener() {
            @Override
            public void onSystemInsets(int left, int top, int right, int bottom) {
                SampleLog.v("HomeFragment topSystemInsets onSystemInsets left:%s, top:%s, right:%s, bottom:%s", left, top, right, bottom);
            }
        });
        mBinding.bottomSystemInsets.setOnSystemInsetsListener(new SystemInsetsLayout.OnSystemInsetsListener() {
            @Override
            public void onSystemInsets(int left, int top, int right, int bottom) {
                SampleLog.v("HomeFragment bottomSystemInsets onSystemInsets left:%s, top:%s, right:%s, bottom:%s", left, top, right, bottom);
            }
        });

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Preconditions.checkNotNull(mBinding);
        final RecyclerView recyclerView = mBinding.recyclerView;

        recyclerView.setItemAnimator(null);
        CardLayoutManager cardLayoutManager = new CardLayoutManager();
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                // TODO FIXME showLikeAndDislikeButton();
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                // TODO FIXME showLikeAndDislikeButton();
            }
        });
        recyclerView.setLayoutManager(cardLayoutManager);
        recyclerView.setHasFixedSize(true);
        mMicroLifecycleComponentManager = new TopVisibleRecyclerViewMicroLifecycleComponentManager(recyclerView, getLifecycle());
        final UnionTypeAdapter adapter = new UnionTypeAdapterImpl();
        adapter.setHost(Host.Factory.create(this, recyclerView, adapter));
        adapter.setUnionTypeMapper(new SampleUnionTypeMapper() {
            {
                put(UNION_TYPE_LOADING_STATUS_LOADING_LARGE, HomeSparkLoadingLargeViewHolder::new);
                put(UNION_TYPE_LOADING_STATUS_LOADING_SMALL, HomeSparkLoadingLargeViewHolder::new);
                put(UNION_TYPE_LOADING_STATUS_LOAD_FAIL_LARGE, HomeSparkLoadFailLargeViewHolder::new);
                put(UNION_TYPE_LOADING_STATUS_LOAD_FAIL_SMALL, HomeSparkLoadFailLargeViewHolder::new);
                put(UNION_TYPE_LOADING_STATUS_NO_MORE_DATA, LocalHomeSparkLoadNoMoreDataLargeViewHolder::new);
                put(UNION_TYPE_LOADING_STATUS_EMPTY_DATA, LocalHomeSparkLoadNoMoreDataLargeViewHolder::new);
                put(UNION_TYPE_LOADING_STATUS_MANUAL_TO_LOAD_MORE, LocalHomeSparkLoadNoMoreDataLargeViewHolder::new);
            }
        });

        mView = new ViewImpl(adapter);
        clearPresenter();
        mPresenter = new HomeFragmentPresenter(mView);
        mView.setPresenter(mPresenter);
        final CardTouchCallbackImpl cardTouchCallback = new CardTouchCallbackImpl() {
            @Override
            public void onSwiping(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, float dXProgress) {
                if (viewHolder instanceof HomeSparkViewHolder) {
                    final HomeSparkViewHolder homeSparkViewHolder = (HomeSparkViewHolder) viewHolder;
                    homeSparkViewHolder.updateLikeAndDislike(dXProgress);
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDir, Object payload) {
                final int position = viewHolder.getAdapterPosition();
                if (position < 0) {
                    SampleLog.e(MSIMUikitConstants.ErrorLog.INVALID_POSITION);
                    return;
                }
                final UnionTypeItemObject originObject = adapter.getItem(position);
                if (originObject == null) {
                    SampleLog.e(MSIMUikitConstants.ErrorLog.INVALID_TARGET);
                    return;
                }
                final DataObject itemObject = (DataObject) originObject.itemObject;
                //noinspection ConstantConditions
                final Spark spark = (Spark) itemObject.object;

                adapter.getData().beginTransaction()
                        .add((transaction, groupArrayList) -> groupArrayList.removeItem(position))
                        .commit();

                if (payload instanceof Bundle) {
                    Bundle args = (Bundle) payload;
                    boolean swipeOnly = args.getBoolean(MSIMUikitConstants.ExtrasKey.KEY_BOOLEAN, false);
                    if (swipeOnly) {
                        // swipe only, this is invoked by #swipeTopVisibleViewHolderOnly
                        SampleLog.v("ignore. onSwiped is swipe only");
                        return;
                    }
                }

                if (spark == null || spark.profile.getUid() <= 0) {
                    SampleLog.e(MSIMUikitConstants.ErrorLog.INVALID_TARGET);
                    return;
                }

                if (swipeDir == CardLayoutItemTouchHelper.LEFT) {
                    // TODO FIXME dislikeSubmit(userInfo);
                } else if (swipeDir == CardLayoutItemTouchHelper.RIGHT) {
                    // TODO FIXME likeSubmit(userInfo);
                } else {
                    SampleLog.e("unexpected swipeDir:%s", swipeDir);
                }
            }
        };
        mCardLayoutItemTouchHelper = new CardLayoutItemTouchHelper(cardTouchCallback);
        mCardLayoutItemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
        mPresenter.requestInit();
    }

    private class LocalHomeSparkLoadNoMoreDataLargeViewHolder extends HomeSparkLoadNoMoreDataLargeViewHolder {

        public LocalHomeSparkLoadNoMoreDataLargeViewHolder(@NonNull Host host) {
            super(host);
        }

        @Override
        public void onBindUpdate() {
            View retry = itemView.findViewById(R.id.retry);
            if (retry != null) {
                ViewUtil.onClick(retry, v -> {
                    // 总是加载第一页
                    refresh();
                });
            }
        }

    }

    public void refresh() {
        if (mPresenter != null) {
            mPresenter.requestInit(true);
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearPresenter();
        mBinding = null;
        mView = null;
        IOUtil.closeQuietly(mMicroLifecycleComponentManager);
        mMicroLifecycleComponentManager = null;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    class ViewImpl extends UnionTypeStatusPageView<Object> {

        public ViewImpl(@NonNull UnionTypeAdapter adapter) {
            super(adapter);
            setClearContentWhenRequestInit(true);
        }

    }

}
