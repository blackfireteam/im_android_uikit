package com.masonsoft.imsdk.uikit.app.conversation;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.app.chatroom.ChatRoomActivity;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitConversationFragmentBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.widget.DividerItemDecoration;
import com.masonsoft.imsdk.util.Objects;
import com.masonsoft.imsdk.util.TimeDiffDebugHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.UnionTypeStatusPageView;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;

/**
 * 会话
 */
public class ConversationFragment extends SystemInsetsFragment {

    public static ConversationFragment newInstance() {
        Bundle args = new Bundle();
        ConversationFragment fragment = new ConversationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private ImsdkUikitConversationFragmentBinding mBinding;

    private UnionTypeAdapter mDataAdapter;
    private ConversationFragmentPresenter mPresenter;
    private ViewImpl mViewImpl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MSIMUikitLog.v("onCreateView %s", getClass());
        mBinding = ImsdkUikitConversationFragmentBinding.inflate(inflater, container, false);

        final RecyclerView recyclerView = mBinding.recyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                recyclerView.getContext(),
                RecyclerView.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(true);

        recyclerView.addItemDecoration(new DividerItemDecoration(
                DividerItemDecoration.VERTICAL,
                DividerItemDecoration.SHOW_DIVIDER_MIDDLE,
                0xFFe1e1e1,
                DimenUtil.dp2px(1),
                DimenUtil.dp2px(1)
        ));

        UnionTypeAdapter adapter = new UnionTypeAdapter();
        adapter.setHost(Host.Factory.create(this, recyclerView, adapter));
        adapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
        mDataAdapter = adapter;
        mViewImpl = new ViewImpl(adapter);
        clearPresenter();
        mPresenter = new ConversationFragmentPresenter(mViewImpl);
        mViewImpl.setPresenter(mPresenter);
        recyclerView.setAdapter(adapter);

        mPresenter.requestInit();

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        MSIMManager.getInstance().syncManual();
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
        mViewImpl = null;
    }

    private static void smoothScrollToPosition(RecyclerView recyclerView, int position) {
        MSIMUikitLog.v("smoothScrollToPosition recyclerView:%s position:%s", recyclerView, position);
        recyclerView.smoothScrollToPosition(position);
    }

    private static void scrollToPosition(RecyclerView recyclerView, int position) {
        MSIMUikitLog.v("scrollToPosition recyclerView:%s position:%s", recyclerView, position);
        recyclerView.scrollToPosition(position);
    }

    class ViewImpl extends UnionTypeStatusPageView<GeneralResult> {

        public ViewImpl(@NonNull UnionTypeAdapter adapter) {
            super(adapter);
            setAlwaysHideNoMoreData(true);
        }

        public void onSessionUserIdChanged(long sessionUserId) {
            Preconditions.checkArgument(Threads.mustUi());
            if (mBinding == null) {
                return;
            }
            final GlobalChatRoomManager.StaticChatRoomContext context = GlobalChatRoomManager.getInstance().getStaticChatRoomContext();
            MSIMChatRoomContext chatRoomContext = null;
            if (context != null) {
                chatRoomContext = context.getChatRoomContext();
            }

            mBinding.chatRoomPreview.setChatRoomContext(chatRoomContext);
            ViewUtil.onClick(mBinding.chatRoomPreview, v -> {
                final Activity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
                if (activity == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                    return;
                }
                ChatRoomActivity.start(activity);
            });
        }

        @Override
        public void onInitRequestResult(@NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
            if (result.items != null) {
                mergeConversationList(new ArrayList<>(result.items));
            }
        }

        @Override
        public void onPrePageRequestResult(@NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
            if (result.items != null) {
                mergeConversationList(new ArrayList<>(result.items));
            }
        }

        @Override
        public void onNextPageRequestResult(@NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
            if (result.items != null) {
                mergeConversationList(new ArrayList<>(result.items));
            }
        }

        @WorkerThread
        void mergeConversationList(@NonNull final List<UnionTypeItemObject> unionTypeItemObjectList) {
            final String tag = Objects.defaultObjectTag(this) + "[mergeConversationList][" + System.currentTimeMillis() + "][size:]" + unionTypeItemObjectList.size();
            MSIMUikitLog.v(tag);
            final boolean[] autoScrollToTop = {false};
            getAdapter().getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final TimeDiffDebugHelper innerMergeTimeDiffDebugHelper = new TimeDiffDebugHelper("innerMergeTimeDiffDebugHelper[" + tag + "]");

                        if (groupArrayList.getGroupItemsSize(getGroupContent()) == 0) {
                            groupArrayList.setGroupItems(getGroupContent(), new ArrayList<>());
                        }
                        final List<UnionTypeItemObject> currentList = groupArrayList.getGroupItems(getGroupContent());
                        Preconditions.checkNotNull(currentList);

                        // merge
                        // 第一步，从原来的列表中删除重复元素
                        {
                            innerMergeTimeDiffDebugHelper.mark();
                            for (UnionTypeItemObject unionTypeItemObject : unionTypeItemObjectList) {
                                for (int i = 0; i < currentList.size(); i++) {
                                    if (currentList.get(i).isSameItem(unionTypeItemObject)) {
                                        currentList.remove(i);
                                        break;
                                    }
                                }
                            }
                            innerMergeTimeDiffDebugHelper.print("rm duplicate");
                        }

                        // 第二步，去除 unionTypeItemObjectList 中已删除的元素
                        innerMergeTimeDiffDebugHelper.mark();
                        for (int i = unionTypeItemObjectList.size() - 1; i >= 0; i--) {
                            final MSIMConversation updateConversation = (MSIMConversation) ((DataObject) unionTypeItemObjectList.get(i).itemObject).object;
                            if (updateConversation.isHidden()) {
                                unionTypeItemObjectList.remove(i);
                            }
                        }
                        innerMergeTimeDiffDebugHelper.print("rm delete");

                        // 第三步，将 unionTypeItemObjectList 与 currentList 合并(这两个不包含重复元素)
                        innerMergeTimeDiffDebugHelper.mark();
                        currentList.addAll(0, unionTypeItemObjectList);
                        innerMergeTimeDiffDebugHelper.mark();
                        innerMergeTimeDiffDebugHelper.print("add all");
                        Collections.sort(currentList, (o1, o2) -> {
                            final MSIMConversation o1Object = (MSIMConversation) ((DataObject) o1.itemObject).object;
                            final MSIMConversation o2Object = (MSIMConversation) ((DataObject) o2.itemObject).object;
                            final long o1ObjectSeq = o1Object.getSeq();
                            final long o2ObjectSeq = o2Object.getSeq();
                            final long diff = o1ObjectSeq - o2ObjectSeq;
                            return diff == 0 ? 0 : (diff < 0 ? 1 : -1);
                        });
                        innerMergeTimeDiffDebugHelper.mark();
                        innerMergeTimeDiffDebugHelper.print("sort current list size:" + currentList.size());

                        groupArrayList.removeGroup(getGroupHeader());
                        groupArrayList.removeGroup(getGroupFooter());
                    })
                    .commit(() -> {
                        final ImsdkUikitConversationFragmentBinding binding = mBinding;
                        if (binding == null) {
                            return;
                        }
                        //noinspection ConstantConditions
                        final int position = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                        if (position <= 0) {
                            autoScrollToTop[0] = true;
                        }
                    }, () -> {
                        final ImsdkUikitConversationFragmentBinding binding = mBinding;
                        if (binding == null) {
                            return;
                        }
                        if (autoScrollToTop[0]) {
                            scrollToPosition(binding.recyclerView, 0);
                        }
                    });
        }
    }

}
