package com.masonsoft.imsdk.uikit.app.chatroom.edittod;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMChatRoomInfo;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.R;
import com.masonsoft.imsdk.uikit.app.SystemInsetsFragment;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitChatRoomEditTodFragmentBinding;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;

import java.util.concurrent.atomic.AtomicBoolean;

import io.github.idonans.dynamic.DynamicView;
import io.github.idonans.lang.util.ViewUtil;

/**
 * 编辑聊天室公告
 */
public class ChatRoomEditTodFragment extends SystemInsetsFragment {

    public static ChatRoomEditTodFragment newInstance(long chatRoomId) {
        Bundle args = new Bundle();
        args.putLong(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, chatRoomId);
        ChatRoomEditTodFragment fragment = new ChatRoomEditTodFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private long mChatRoomId;
    @Nullable
    private ImsdkUikitChatRoomEditTodFragmentBinding mBinding;

    private ChatRoomEditTodFragmentPresenter mPresenter;
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
        mBinding = ImsdkUikitChatRoomEditTodFragmentBinding.inflate(inflater, container, false);

        ViewUtil.onClick(mBinding.topBarBack, v -> ActivityUtil.requestBackPressed(ChatRoomEditTodFragment.this));

        ViewUtil.onClick(mBinding.submitTod, v -> onSubmitClick());

        mBinding.submitTod.setEnabled(false);
        mBinding.editText.setEnabled(false);

        mViewImpl = new ViewImpl();
        clearPresenter();
        mPresenter = new ChatRoomEditTodFragmentPresenter(mViewImpl);

        return mBinding.getRoot();
    }

    private void onSubmitClick() {
        final Activity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }
        if (mBinding == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
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

        //noinspection ConstantConditions
        final String tod = mBinding.editText.getText().toString().trim();
        final long sessionUserId = chatRoomContext.getSessionUserId();
        chatRoomContext.getChatRoomContext().getChatRoomManager().modifyTod(
                sessionUserId,
                tod
        );

        activity.finish();
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

        private AtomicBoolean mSetTodOnce = new AtomicBoolean(false);

        public ViewImpl() {
        }

        public long getChatRoomId() {
            return ChatRoomEditTodFragment.this.mChatRoomId;
        }

        public void onChatRoomStateChanged(@NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
            final ImsdkUikitChatRoomEditTodFragmentBinding binding = mBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            boolean allowEditTod = false;
            final MSIMChatRoomInfo chatRoomInfo = chatRoomContext.getChatRoomContext().getChatRoomInfo();
            if (chatRoomInfo != null) {
                allowEditTod = chatRoomInfo.hasActionTod();

                if (mSetTodOnce.compareAndSet(false, true)) {
                    binding.editText.setText(chatRoomContext.getChatRoomContext().getTipsOfDay());
                }
            }
            binding.editText.setEnabled(allowEditTod);
            binding.submitTod.setEnabled(allowEditTod);
        }
    }

}
