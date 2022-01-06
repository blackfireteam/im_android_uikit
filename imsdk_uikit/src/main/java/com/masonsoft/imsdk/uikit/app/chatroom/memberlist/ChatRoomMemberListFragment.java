package com.masonsoft.imsdk.uikit.app.chatroom.memberlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.masonsoft.imsdk.MSIMChatRoomMember;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitChatRoomMemberListFragmentBinding;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.widget.GridItemDecoration;

import java.util.ArrayList;
import java.util.List;

import io.github.idonans.core.util.DimenUtil;
import io.github.idonans.dynamic.DynamicView;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;

/**
 * 聊天室在线用户列表
 */
public class ChatRoomMemberListFragment extends SystemInsetsFragment {

    public static ChatRoomMemberListFragment newInstance(long chatRoomId) {
        Bundle args = new Bundle();
        args.putLong(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, chatRoomId);
        ChatRoomMemberListFragment fragment = new ChatRoomMemberListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private long mChatRoomId;
    @Nullable
    private ImsdkUikitChatRoomMemberListFragmentBinding mBinding;

    private UnionTypeAdapter mDataAdapter;
    private ChatRoomMemberListFragmentPresenter mPresenter;
    private ViewImpl mViewImpl;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mChatRoomId = args.getLong(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, mChatRoomId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ImsdkUikitChatRoomMemberListFragmentBinding.inflate(inflater, container, false);

        ViewUtil.onClick(mBinding.topBarBack, v -> ActivityUtil.requestBackPressed(ChatRoomMemberListFragment.this));

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
                        if (groupAndPosition[0] == ViewImpl.GROUP_CONTENT) {
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
                        if (groupAndPosition[0] == ViewImpl.GROUP_CONTENT) {
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

        UnionTypeAdapter adapter = new UnionTypeAdapter();
        adapter.setHost(Host.Factory.create(this, recyclerView, adapter));
        adapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
        mDataAdapter = adapter;
        recyclerView.setAdapter(adapter);

        mViewImpl = new ViewImpl(adapter);
        clearPresenter();
        mPresenter = new ChatRoomMemberListFragmentPresenter(mViewImpl);

        return mBinding.getRoot();
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

    class ViewImpl implements DynamicView {

        @NonNull
        private final UnionTypeAdapter mAdapter;
        private static final int GROUP_CONTENT = 0;

        public ViewImpl(@NonNull UnionTypeAdapter adapter) {
            mAdapter = adapter;
        }

        public long getChatRoomId() {
            return ChatRoomMemberListFragment.this.mChatRoomId;
        }

        public void onChatRoomStateChanged(@NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext, int memberListSize) {
            final ImsdkUikitChatRoomMemberListFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            if (memberListSize > 0) {
                binding.topBarTitle.setText(I18nResources.getString(R.string.imsdk_uikit_chat_room_member_list_title_format, memberListSize));
            } else {
                binding.topBarTitle.setText(I18nResources.getString(R.string.imsdk_uikit_chat_room_member_list_title));
            }
        }

        public void replaceMemberList(@NonNull List<MSIMChatRoomMember> memberList, @NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
            final ImsdkUikitChatRoomMemberListFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            mAdapter.getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final List<UnionTypeItemObject> list = new ArrayList<>();
                        for (MSIMChatRoomMember member : memberList) {
                            final UnionTypeItemObject unionTypeItemObject = mPresenter.createDefault(member);
                            if (unionTypeItemObject != null) {
                                list.add(unionTypeItemObject);
                            } else {
                                MSIMUikitLog.e("unexpected. replaceMemberList createDefault return null. member:%s", member);
                            }
                        }
                        groupArrayList.setGroupItems(GROUP_CONTENT, list);
                    })
                    .commit();
        }
    }

}
