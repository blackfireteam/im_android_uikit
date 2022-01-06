package com.masonsoft.imsdk.uikit.app.chatroom.memberlist;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.masonsoft.imsdk.MSIMCallback;
import com.masonsoft.imsdk.MSIMChatRoomInfo;
import com.masonsoft.imsdk.MSIMChatRoomMember;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.core.I18nResources;
import com.masonsoft.imsdk.core.IMConstants;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.common.simpledialog.SimpleBottomActionsDialog;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitChatRoomMemberListFragmentBinding;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;
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

    private MSIMCallback<GeneralResult> mCallback;

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

        public void showMenu(@NonNull MSIMChatRoomMember member, @NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
            final ImsdkUikitChatRoomMemberListFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            final Activity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
            if (activity == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
                return;
            }

            final MSIMChatRoomInfo chatRoomInfo = chatRoomContext.getChatRoomContext().getChatRoomInfo();
            if (chatRoomInfo == null) {
                TipUtil.show(MSIMUikitConstants.ErrorLog.INVALID_CHAT_ROOM_INFO);
                return;
            }

            final long sessionUserId = chatRoomContext.getSessionUserId();
            final long memberUserId = member.getUserId();
            if (sessionUserId == memberUserId) {
                MSIMUikitLog.v("ignore. showMenu member is current session user id: %s",
                        member.getUserId());
                return;
            }

            if (sessionUserId <= 0 || memberUserId <= 0) {
                MSIMUikitLog.e("unexpected. showMenu sessionUserId:%s, memberUserId:%s",
                        sessionUserId, memberUserId);
                return;
            }

            // 设置为临时管理员（10 分钟）
            final int actionIdSetTmpAdmin_10Min = 0;
            // 设置为临时管理员（30 分钟）
            final int actionIdSetTmpAdmin_30Min = 1;
            // 设置为临时管理员（1 小时）
            final int actionIdSetTmpAdmin_1Hour = 2;
            // 设置为临时管理员（24 小时）
            final int actionIdSetTmpAdmin_24Hour = 3;
            // 设置为临时管理员（1 周）
            final int actionIdSetTmpAdmin_1Week = 4;
            // 取消临时管理员
            final int actionIdClearTmpAdmin = 5;
            // 禁言（10 分钟）
            final int actionIdSetMute_10Min = 6;
            // 禁言（30 分钟）
            final int actionIdSetMute_30Min = 7;
            // 禁言（1 小时）
            final int actionIdSetMute_1Hour = 8;
            // 禁言（24 小时）
            final int actionIdSetMute_24Hour = 9;
            // 禁言（1 周）
            final int actionIdSetMute_1Week = 10;
            // 取消禁言
            final int actionIdClearMute = 11;
            final String[] actionTextArray = {
                    "设置为临时管理员（10 分钟）",
                    "设置为临时管理员（30 分钟）",
                    "设置为临时管理员（1 小时）",
                    "设置为临时管理员（24 小时）",
                    "设置为临时管理员（1 周）",
                    "取消临时管理员",
                    "禁言（10 分钟）",
                    "禁言（30 分钟）",
                    "禁言（1 小时）",
                    "禁言（24 小时）",
                    "禁言（1 周）",
                    "取消禁言"
            };

            final List<Integer> actionIdList = new ArrayList<>();
            if (chatRoomInfo.hasActionAssign()) {
                // 当前账号有权限设置临时管理员
                final int role = member.getRole();
                if (role == MSIMConstants.ChatRoomMemberRole.NORMAL) {
                    // 设置普通用户为临时管理员
                    actionIdList.add(actionIdSetTmpAdmin_10Min);
                    actionIdList.add(actionIdSetTmpAdmin_30Min);
                    actionIdList.add(actionIdSetTmpAdmin_1Hour);
                    actionIdList.add(actionIdSetTmpAdmin_24Hour);
                    actionIdList.add(actionIdSetTmpAdmin_1Week);
                } else if (role == MSIMConstants.ChatRoomMemberRole.TEMP_ADMIN) {
                    // 取消用户的临时管理员身份
                    actionIdList.add(actionIdClearTmpAdmin);
                }
            }
            if (chatRoomInfo.hasActionMute()) {
                // 当前账号有权限设置禁言目标用户
                final boolean mute = member.isMute();
                final int role = member.getRole();
                if (role == MSIMConstants.ChatRoomMemberRole.NORMAL) {
                    // 对普通用户设置禁言或者取消禁言
                    if (mute) {
                        // 取消禁言
                        actionIdList.add(actionIdClearMute);
                    } else {
                        // 禁言
                        actionIdList.add(actionIdSetMute_10Min);
                        actionIdList.add(actionIdSetMute_30Min);
                        actionIdList.add(actionIdSetMute_1Hour);
                        actionIdList.add(actionIdSetMute_24Hour);
                        actionIdList.add(actionIdSetMute_1Week);
                    }
                }
            }

            if (actionIdList.isEmpty()) {
                MSIMUikitLog.v("ignore. showMenu actionIdList is empty.");
                return;
            }

            final List<String> actions = new ArrayList<>();
            for (Integer actionId : actionIdList) {
                actions.add(actionTextArray[actionId]);
            }
            final SimpleBottomActionsDialog dialog = new SimpleBottomActionsDialog(
                    activity,
                    actions
            );
            dialog.setOnActionClickListener((index, actionText) -> {
                final int size = actionIdList.size();
                if (index >= 0 && index < size) {
                    final int actionId = actionIdList.get(index);
                    if (actionId == actionIdSetTmpAdmin_10Min) {
                        // 设置为临时管理员（10 分钟）
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserAdmin(
                                        sessionUserId,
                                        Lists.newArrayList(memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_10_MIN,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdSetTmpAdmin_30Min) {
                        // 设置为临时管理员（30 分钟）
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserAdmin(
                                        sessionUserId,
                                        Lists.newArrayList(memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_30_MIN,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdSetTmpAdmin_1Hour) {
                        // 设置为临时管理员（1 小时）
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserAdmin(
                                        sessionUserId,
                                        Lists.newArrayList(memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_1_HOUR,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdSetTmpAdmin_24Hour) {
                        // 设置为临时管理员（24 小时）
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserAdmin(
                                        sessionUserId,
                                        Lists.newArrayList(memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_24_HOUR,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdSetTmpAdmin_1Week) {
                        // 设置为临时管理员（1 周）
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserAdmin(
                                        sessionUserId,
                                        Lists.newArrayList(memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_1_WEEK,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdClearTmpAdmin) {
                        // 取消临时管理员
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserAdmin(
                                        sessionUserId,
                                        Lists.newArrayList(-memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_1_WEEK,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdSetMute_10Min) {
                        // 禁言（10 分钟）
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserMute(
                                        sessionUserId,
                                        Lists.newArrayList(memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_10_MIN,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdSetMute_30Min) {
                        // 禁言（30 分钟）
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserMute(
                                        sessionUserId,
                                        Lists.newArrayList(memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_30_MIN,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdSetMute_1Hour) {
                        // 禁言（1 小时）
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserMute(
                                        sessionUserId,
                                        Lists.newArrayList(memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_1_HOUR,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdSetMute_24Hour) {
                        // 禁言（24 小时）
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserMute(
                                        sessionUserId,
                                        Lists.newArrayList(memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_24_HOUR,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdSetMute_1Week) {
                        // 禁言（1 周）
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserMute(
                                        sessionUserId,
                                        Lists.newArrayList(memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_1_WEEK,
                                        null,
                                        resetCallback()
                                );
                    } else if (actionId == actionIdClearMute) {
                        // 取消禁言
                        chatRoomContext.getChatRoomContext().getChatRoomManager()
                                .modifyUserMute(
                                        sessionUserId,
                                        Lists.newArrayList(-memberUserId),
                                        IMConstants.ChatRoomActionDurationType.DURATION_1_WEEK,
                                        null,
                                        resetCallback()
                                );
                    } else {
                        MSIMUikitLog.e("unexpected. showMenu invalid actionId:%s", actionId);
                    }
                } else {
                    MSIMUikitLog.e("unexpected. showMenu invalid index:%s of size:%s", index, size);
                }
            });
            dialog.show();
        }
    }

    @NonNull
    private MSIMCallback<GeneralResult> resetCallback() {
        mCallback = generalResult -> {
            final GeneralResult cause = generalResult.getCause();
            if (!cause.isSuccess()) {
                TipUtil.showOrDefault(cause.message);
            }
        };
        return mCallback;
    }

}
