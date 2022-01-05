package com.masonsoft.imsdk.uikit.app.chatroom.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitChatRoomSettingsFragmentBinding;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;

import io.github.idonans.dynamic.DynamicView;
import io.github.idonans.lang.util.ViewUtil;

/**
 * 聊天室设置页面
 */
public class ChatRoomSettingsFragment extends SystemInsetsFragment {

    public static ChatRoomSettingsFragment newInstance(long chatRoomId) {
        Bundle args = new Bundle();
        args.putLong(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, chatRoomId);
        ChatRoomSettingsFragment fragment = new ChatRoomSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private long mChatRoomId;
    @Nullable
    private ImsdkUikitChatRoomSettingsFragmentBinding mBinding;

    private ChatRoomSettingsFragmentPresenter mPresenter;
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
        mBinding = ImsdkUikitChatRoomSettingsFragmentBinding.inflate(inflater, container, false);

        ViewUtil.onClick(mBinding.topBarBack, v -> ActivityUtil.requestBackPressed(ChatRoomSettingsFragment.this));

        ViewUtil.onClick(mBinding.memberList, v -> onMemberListClick());
        ViewUtil.onClick(mBinding.chatRoomName, v -> onChatRoomNameClick());
        ViewUtil.onClick(mBinding.chatRoomTod, v -> onChatRoomTodClick());

        mViewImpl = new ViewImpl();
        clearPresenter();
        mPresenter = new ChatRoomSettingsFragmentPresenter(mViewImpl);

        return mBinding.getRoot();
    }

    private void onMemberListClick() {
        final Activity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }
        if (mPresenter == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
            return;
        }
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            TipUtil.show(R.string.imsdk_uikit_tip_chat_room_context_is_null);
            return;
        }
        // TODO
    }

    private void onChatRoomNameClick() {
        final Activity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }
        if (mPresenter == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
            return;
        }
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            TipUtil.show(R.string.imsdk_uikit_tip_chat_room_context_is_null);
            return;
        }

        // TODO
    }

    private void onChatRoomTodClick() {
        final Activity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }
        if (mPresenter == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.PRESENTER_IS_NULL);
            return;
        }
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            TipUtil.show(R.string.imsdk_uikit_tip_chat_room_context_is_null);
            return;
        }

        // TODO
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

        public ViewImpl() {
        }

        public long getChatRoomId() {
            return ChatRoomSettingsFragment.this.mChatRoomId;
        }

        public void onChatRoomStateChanged(@NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
            final ImsdkUikitChatRoomSettingsFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            // TODO
        }
    }

}
