package com.masonsoft.imsdk.sample.app.discover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.masonsoft.imsdk.sample.SampleLog;
import com.masonsoft.imsdk.sample.databinding.ImsdkSampleDiscoverFragmentBinding;
import com.masonsoft.imsdk.sample.uniontype.SampleUnionTypeMapper;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.widget.GridItemDecoration;
import com.masonsoft.imsdk.util.Objects;

import java.util.List;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.dynamic.page.UnionTypeStatusPageView;
import io.github.idonans.systeminsets.SystemInsetsLayout;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;

/**
 * 发现
 */
public class DiscoverFragment extends SystemInsetsFragment {

    public static DiscoverFragment newInstance() {
        Bundle args = new Bundle();
        DiscoverFragment fragment = new DiscoverFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private ImsdkSampleDiscoverFragmentBinding mBinding;
    private ViewImpl mView;
    private DiscoverFragmentPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SampleLog.v("onCreateView %s", getClass());

        mBinding = ImsdkSampleDiscoverFragmentBinding.inflate(inflater, container, false);
        //noinspection Convert2Lambda
        mBinding.topSystemInsets.setOnSystemInsetsListener(new SystemInsetsLayout.OnSystemInsetsListener() {
            @Override
            public void onSystemInsets(int left, int top, int right, int bottom) {
                SampleLog.v("DiscoverFragment topSystemInsets onSystemInsets left:%s, top:%s, right:%s, bottom:%s", left, top, right, bottom);
            }
        });
        //noinspection Convert2Lambda
        mBinding.bottomSystemInsets.setOnSystemInsetsListener(new SystemInsetsLayout.OnSystemInsetsListener() {
            @Override
            public void onSystemInsets(int left, int top, int right, int bottom) {
                SampleLog.v("DiscoverFragment bottomSystemInsets onSystemInsets left:%s, top:%s, right:%s, bottom:%s", left, top, right, bottom);
            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Preconditions.checkNotNull(mBinding);
        final RecyclerView recyclerView = mBinding.recyclerView;

        final int spanCount = 2;
        final GridLayoutManager layoutManager = new GridLayoutManager(recyclerView.getContext(), spanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                final UnionTypeAdapter adapter = (UnionTypeAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    final int[] groupAndPosition = adapter.getGroupAndPosition(position);
                    if (groupAndPosition != null) {
                        if (groupAndPosition[0] == mView.getGroupContent()) {
                            return 1;
                        }
                    }
                }

                return spanCount;
            }

            @Override
            public int getSpanIndex(int position, int spanCount) {
                final UnionTypeAdapter adapter = (UnionTypeAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    final int[] groupAndPosition = adapter.getGroupAndPosition(position);
                    if (groupAndPosition != null) {
                        if (groupAndPosition[0] == mView.getGroupContent()) {
                            return groupAndPosition[1] % spanCount;
                        }
                    }
                }

                return 0;
            }
        });

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridItemDecoration(spanCount, DimenUtil.dp2px(15), true));
        recyclerView.setHasFixedSize(true);
        final UnionTypeAdapter adapter = new UnionTypeAdapter();
        adapter.setHost(Host.Factory.create(this, recyclerView, adapter));
        adapter.setUnionTypeMapper(new SampleUnionTypeMapper());

        mView = new ViewImpl(adapter);
        clearPresenter();
        mPresenter = new DiscoverFragmentPresenter(mView);
        mView.setPresenter(mPresenter);
        recyclerView.setAdapter(adapter);
        mPresenter.requestInit();
    }

    class ViewImpl extends UnionTypeStatusPageView<Object> {

        public ViewImpl(@NonNull UnionTypeAdapter adapter) {
            super(adapter);
            setClearContentWhenRequestInit(true);
        }

        @WorkerThread
        public void removeUser(@NonNull final UnionTypeItemObject unionTypeItemObject) {
            Preconditions.checkArgument(!Threads.isUi());
            getAdapter().getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final List<UnionTypeItemObject> groupDefaultList = groupArrayList.getGroupItems(getGroupContent());
                        int removedPosition = -1;
                        if (groupDefaultList != null) {
                            final int size = groupDefaultList.size();
                            for (int i = 0; i < size; i++) {
                                final UnionTypeItemObject existsOne = groupDefaultList.get(i);
                                if (existsOne.isSameItem(unionTypeItemObject)) {
                                    removedPosition = i;
                                    break;
                                }
                            }
                        }
                        if (removedPosition >= 0) {
                            groupArrayList.removeGroupItem(getGroupContent(), removedPosition);
                        }
                    })
                    .commit();


        }

        @WorkerThread
        public void replaceUser(@NonNull final UnionTypeItemObject unionTypeItemObject) {
            Preconditions.checkArgument(!Threads.isUi());
            getAdapter().getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        if (groupArrayList.getGroupItemsSize(getGroupContent()) == 0) {
                            // request init
                            Threads.postUi(() -> {
                                if (mPresenter != null) {
                                    SampleLog.v(Objects.defaultObjectTag(this) + " page content is empty, use requestInit instead of replace");
                                    if (!mPresenter.getInitRequestStatus().isLoading()) {
                                        mPresenter.requestInit(true);
                                    }
                                }
                            });
                            return;
                        }

                        final List<UnionTypeItemObject> groupDefaultList = groupArrayList.getGroupItems(getGroupContent());
                        int removedPosition = -1;
                        if (groupDefaultList != null) {
                            final int size = groupDefaultList.size();
                            for (int i = 0; i < size; i++) {
                                final UnionTypeItemObject existsOne = groupDefaultList.get(i);
                                if (existsOne.isSameItem(unionTypeItemObject)) {
                                    removedPosition = i;
                                    break;
                                }
                            }
                        }
                        if (removedPosition >= 0) {
                            groupArrayList.removeGroupItem(getGroupContent(), removedPosition);
                        }

                        if (unionTypeItemObject.itemObject instanceof DataObject) {
                            try {
                                final DataObject uidObject = (DataObject) unionTypeItemObject.itemObject;
                                final Long uid = (Long) uidObject.object;
                                if (uid != null && uid > 10000000000L) {
                                    // 新上线的手机号码展示在最前面
                                    groupArrayList.insertGroupItems(getGroupContent(), 0, Lists.newArrayList(unionTypeItemObject));
                                    return;
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }

                        // 默认新上线的展示在结尾
                        groupArrayList.appendGroupItems(getGroupContent(), Lists.newArrayList(unionTypeItemObject));
                    })
                    .commit();
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
    }

}
