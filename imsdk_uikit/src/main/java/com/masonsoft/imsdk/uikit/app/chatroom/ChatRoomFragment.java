package com.masonsoft.imsdk.uikit.app.chatroom;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.masonsoft.imsdk.MSIMCallback;
import com.masonsoft.imsdk.MSIMChatRoomMessage;
import com.masonsoft.imsdk.MSIMChatRoomMessageFactory;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMWeakCallback;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.app.chatroom.settings.ChatRoomSettingsActivity;
import com.masonsoft.imsdk.uikit.common.app.CustomInputFragment;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationInfo;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManagerHost;
import com.masonsoft.imsdk.uikit.common.microlifecycle.VisibleRecyclerViewMicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.simpledialog.SimpleBottomActionsDialog;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitChatRoomFragmentContentBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitChatRoomFragmentTopBarBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.idonans.core.AbortSignal;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.dynamic.DynamicView;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;

/**
 * 聊天室页面
 */
public class ChatRoomFragment extends CustomInputFragment {

    public static ChatRoomFragment newInstance(long chatRoomId) {
        Bundle args = new Bundle();
        args.putLong(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, chatRoomId);
        ChatRoomFragment fragment = new ChatRoomFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private long mChatRoomId;
    @Nullable
    private ImsdkUikitChatRoomFragmentTopBarBinding mTopBarBinding;
    @Nullable
    private ImsdkUikitChatRoomFragmentContentBinding mContentBinding;
    private LocalEnqueueCallback mEnqueueCallback;

    private UnionTypeAdapter mDataAdapter;
    private ChatRoomFragmentPresenter mPresenter;
    private ViewImpl mViewImpl;
    private MicroLifecycleComponentManager mMicroLifecycleComponentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mChatRoomId = args.getLong(MSIMUikitConstants.ExtrasKey.KEY_ROOM_ID, mChatRoomId);
        }
    }

    private static void smoothScrollToPosition(RecyclerView recyclerView, int position) {
        MSIMUikitLog.v("smoothScrollToPosition recyclerView:%s position:%s", recyclerView, position);
        recyclerView.smoothScrollToPosition(position);
    }

    private static void scrollToPosition(RecyclerView recyclerView, int position) {
        MSIMUikitLog.v("scrollToPosition recyclerView:%s position:%s", recyclerView, position);
        recyclerView.scrollToPosition(position);
    }

    @Override
    protected void onSoftKeyboardLayoutShownAdapter(boolean customSoftKeyboard, boolean systemSoftKeyboard) {
        super.onSoftKeyboardLayoutShownAdapter(customSoftKeyboard, systemSoftKeyboard);

        Threads.postUi(() -> {
            int count = mDataAdapter.getItemCount();
            if (count > 0) {
                //noinspection ConstantConditions
                final int firstPosition = ((LinearLayoutManager) mContentBinding.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                final int lastPosition = ((LinearLayoutManager) mContentBinding.recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                final int archPosition = Math.max(0, count - 3);

                boolean scrollWithAnimation = false;
                if (archPosition >= firstPosition && archPosition <= lastPosition) {
                    scrollWithAnimation = true;
                }

                MSIMUikitLog.v("onSoftKeyboardLayoutShown scrollWithAnimation:%s, firstPosition:%s, count:%s",
                        scrollWithAnimation, firstPosition, count);
                if (scrollWithAnimation) {
                    smoothScrollToPosition(mContentBinding.recyclerView, count - 1);
                } else {
                    mContentBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            mContentBinding.recyclerView.removeOnScrollListener(this);
                            MSIMUikitLog.v("onSoftKeyboardLayoutShown scrollWithAnimation:false addOnScrollListener onScrolled");
                            smoothScrollToPosition(mContentBinding.recyclerView, mDataAdapter.getItemCount() - 1);
                        }
                    });
                    scrollToPosition(mContentBinding.recyclerView, archPosition);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = super.onCreateView(inflater, container, savedInstanceState);

        //noinspection ConstantConditions
        mTopBarBinding = ImsdkUikitChatRoomFragmentTopBarBinding.inflate(inflater, getCustomBinding().customTopBarContainer, true);
        mContentBinding = ImsdkUikitChatRoomFragmentContentBinding.inflate(inflater, getCustomBinding().customContentContainer, true);

        getCustomBinding().customSoftKeyboard.getCustomConfig().setShowSnapchat(false);
        getCustomBinding().customSoftKeyboard.getCustomConfig().setShowRtc(false);
        getCustomBinding().customSoftKeyboard.getCustomConfig().setShowLocation(false);

        ViewUtil.onClick(mTopBarBinding.topBarBack, v -> ActivityUtil.requestBackPressed(ChatRoomFragment.this));
        mTopBarBinding.topBarTitle.setChatRoomContext(null);

        ViewUtil.onClick(mTopBarBinding.topBarMore, v -> showBottomActions());

        final RecyclerView recyclerView = mContentBinding.recyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                recyclerView.getContext(),
                RecyclerView.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    if (mDataAdapter != null && lastPosition >= 0) {
                        if (lastPosition == mDataAdapter.getItemCount() - 1) {
                            // 滚动到最底部
                            hideNewMessagesTipView();
                        }
                    }
                }
            }
        });
        mMicroLifecycleComponentManager = new VisibleRecyclerViewMicroLifecycleComponentManager(recyclerView, getLifecycle());

        UnionTypeAdapter adapter = new UnionTypeAdapterImpl();
        adapter.setHost(Host.Factory.create(this, recyclerView, adapter));
        adapter.setUnionTypeMapper(new IMUikitUnionTypeMapper());
        mDataAdapter = adapter;
        recyclerView.setAdapter(adapter);

        mViewImpl = new ViewImpl(adapter);
        clearPresenter();
        mPresenter = new ChatRoomFragmentPresenter(mViewImpl);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void showBottomActions() {
        final Activity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final List<String> actions = Lists.newArrayList("设置", "模拟并发消息");
        final SimpleBottomActionsDialog dialog = new SimpleBottomActionsDialog(
                activity,
                actions
        );
        dialog.setOnActionClickListener((index, actionText) -> {
            if (index == 0) {
                // 设置
                if (mChatRoomId <= 0) {
                    MSIMUikitLog.e("unexpected. chat room id:" + mChatRoomId);
                    return;
                }
                ChatRoomSettingsActivity.start(activity, mChatRoomId);
            } else if (index == 1) {
                // 模拟并发消息
                if (mPresenter == null) {
                    MSIMUikitLog.e("unexpected. presenter is null");
                    return;
                }
                mockMultiMessages(mPresenter.getChatRoomContext());
            }
        });
        dialog.show();
    }

    private static void mockMultiMessages(final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
        if (chatRoomContext == null) {
            MSIMUikitLog.e("unexpected. chat room context is null");
            return;
        }
        final String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        final int size = 10;
        for (int i = 1; i <= size; i++) {
            final int index = i;
            Threads.postBackground(() -> {
                final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
                final MSIMChatRoomMessage message = MSIMChatRoomMessageFactory.createTextMessage(
                        "[" + time + "] mock concurrent message [" + index + "/" + size + "]"
                );
                chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                        sessionUserId,
                        message
                );
            });
        }
    }

    @Override
    protected void submitTextMessage(boolean snapchat, String text) {
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            MSIMUikitLog.e("chat room context is null");
            return;
        }

        mEnqueueCallback = new LocalEnqueueCallback(true);
        final MSIMChatRoomMessage message = MSIMChatRoomMessageFactory.createTextMessage(
                text
        );
        chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                chatRoomContext.getSessionUserId(),
                message,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    @Override
    protected void submitLottieMessage(boolean snapchat, String lottieId) {
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            MSIMUikitLog.e("chat room context is null");
            return;
        }

        mEnqueueCallback = new LocalEnqueueCallback(true);
        final MSIMChatRoomMessage message = MSIMChatRoomMessageFactory.createCustomEmotionMessage(
                lottieId
        );
        chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                chatRoomContext.getSessionUserId(),
                message,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    @Override
    protected void submitMediaMessage(boolean snapchat, @NonNull List<MediaData.MediaInfo> mediaInfoList) {
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            MSIMUikitLog.e("chat room context is null");
            return;
        }

        for (MediaData.MediaInfo mediaInfo : mediaInfoList) {
            mEnqueueCallback = new LocalEnqueueCallback(false);
            final MSIMChatRoomMessage message;
            if (mediaInfo.isVideoMimeType()) {
                message = MSIMChatRoomMessageFactory.createVideoMessage(
                        mediaInfo.uri
                );
            } else {
                message = MSIMChatRoomMessageFactory.createImageMessage(
                        mediaInfo.uri
                );
            }
            chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                    chatRoomContext.getSessionUserId(),
                    message,
                    new MSIMWeakCallback<>(mEnqueueCallback)
            );
        }
    }

    @Override
    protected void submitAudioMessage(boolean snapchat, final String audioFilePath) {
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            MSIMUikitLog.e("chat room context is null");
            return;
        }

        mEnqueueCallback = new LocalEnqueueCallback(true);
        final MSIMChatRoomMessage message = MSIMChatRoomMessageFactory.createAudioMessage(
                audioFilePath
        );
        chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                chatRoomContext.getSessionUserId(),
                message,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    @Override
    protected void submitLocationMessage(boolean snapchat, @NonNull LocationInfo locationInfo, long zoom) {
        final GlobalChatRoomManager.StaticChatRoomContext chatRoomContext = mPresenter.getChatRoomContext();
        if (chatRoomContext == null) {
            MSIMUikitLog.e("chat room context is null");
            return;
        }

        mEnqueueCallback = new LocalEnqueueCallback(false);
        final MSIMChatRoomMessage message = MSIMChatRoomMessageFactory.createLocationMessage(
                locationInfo.title,
                locationInfo.subTitle,
                locationInfo.lat,
                locationInfo.lng,
                zoom
        );
        chatRoomContext.getChatRoomContext().getChatRoomManager().sendChatRoomMessage(
                chatRoomContext.getSessionUserId(),
                message,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    @Override
    protected void submitClickRtcAudio(boolean snapchat) {
        MSIMUikitLog.e("unexpected. not impl. submitClickRtcAudio");
    }

    @Override
    protected void submitClickRtcVideo(boolean snapchat) {
        MSIMUikitLog.e("unexpected. not impl. submitClickRtcVideo");
    }

    private void showNewMessagesTipView() {
        // TODO
        // ViewUtil.setVisibilityIfChanged(mActionNewMessages, View.VISIBLE);
    }

    private void hideNewMessagesTipView() {
        // TODO
        // ViewUtil.setVisibilityIfChanged(mActionNewMessages, View.GONE);
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
        mTopBarBinding = null;
        mContentBinding = null;
        mViewImpl = null;
    }

    private class UnionTypeAdapterImpl extends UnionTypeAdapter implements MicroLifecycleComponentManagerHost {
        @Override
        public MicroLifecycleComponentManager getMicroLifecycleComponentManager() {
            return mMicroLifecycleComponentManager;
        }
    }

    class ViewImpl implements DynamicView {

        @NonNull
        private final UnionTypeAdapter mAdapter;
        private final int GROUP_CONTENT = 0;

        public ViewImpl(@NonNull UnionTypeAdapter adapter) {
            mAdapter = adapter;
        }

        public long getChatRoomId() {
            return ChatRoomFragment.this.mChatRoomId;
        }

        public void onChatRoomStateChanged(@NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
            final ImsdkUikitChatRoomFragmentTopBarBinding binding = mTopBarBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            binding.topBarTitle.setChatRoomContext(chatRoomContext.getChatRoomContext());
        }

        public void onAppendMessages(@NonNull List<MSIMChatRoomMessage> messageList, @NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
            final ImsdkUikitChatRoomFragmentContentBinding binding = mContentBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            final ObjectWrapper autoScrollToEnd = new ObjectWrapper(null);
            mAdapter.getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final List<UnionTypeItemObject> contentList = new ArrayList<>();
                        for (MSIMChatRoomMessage message : messageList) {
                            final UnionTypeItemObject unionTypeItemObject = mPresenter.createDefault(message);
                            if (unionTypeItemObject != null) {
                                contentList.add(unionTypeItemObject);
                            } else {
                                MSIMUikitLog.e("unexpected. onAppendMessages create UnionTypeItemObject is null. MSIMChatRoomMessage:%s", message);
                            }
                        }
                        groupArrayList.appendGroupItems(GROUP_CONTENT, contentList);
                    })
                    .commit(() -> {
                        final int count = mAdapter.getItemCount();
                        if (count <= 0) {
                            autoScrollToEnd.setObject(Boolean.TRUE);
                        } else {
                            //noinspection ConstantConditions
                            int lastPosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                            if (lastPosition < 0) {
                                autoScrollToEnd.setObject(Boolean.TRUE);
                            } else {
                                if (lastPosition >= count - 1) {
                                    autoScrollToEnd.setObject(Boolean.TRUE);
                                }
                            }
                        }
                    }, () -> {
                        if (autoScrollToEnd.getObject() == Boolean.TRUE && isResumed()) {
                            scrollToPosition(binding.recyclerView, mAdapter.getItemCount() - 1);
                        } else {
                            // 有新消息，显示向下的箭头
                            showNewMessagesTipView();
                        }
                    });
        }

        public void onUpdateMessages(@NonNull List<MSIMChatRoomMessage> messageList, @NonNull GlobalChatRoomManager.StaticChatRoomContext chatRoomContext) {
            final ImsdkUikitChatRoomFragmentContentBinding binding = mContentBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            mAdapter.getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        for (MSIMChatRoomMessage message : messageList) {
                            final UnionTypeItemObject unionTypeItemObject = mPresenter.createDefault(message);
                            final List<UnionTypeItemObject> list = groupArrayList.getGroupItems(GROUP_CONTENT);
                            if (list != null) {
                                for (int i = list.size() - 1; i >= 0; i--) {
                                    final UnionTypeItemObject object = list.get(i);
                                    final DataObject dataObject = object.getItemObject(DataObject.class);
                                    if (dataObject != null) {
                                        final MSIMChatRoomMessage m = dataObject.getObject(MSIMChatRoomMessage.class);
                                        if (m != null) {
                                            if (m.equals(message)) {
                                                if (unionTypeItemObject == null) {
                                                    // 该消息不可见
                                                    list.remove(i);
                                                } else {
                                                    // 该消息可见但是内容可能发生了变化
                                                    list.set(i, unionTypeItemObject);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    })
                    .commit();
        }

        public void onReceivedTipMessageList(@NonNull List<CharSequence> tipMessageList) {
            final ImsdkUikitChatRoomFragmentContentBinding binding = mContentBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            final ObjectWrapper autoScrollToEnd = new ObjectWrapper(null);
            mAdapter.getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final List<UnionTypeItemObject> contentList = new ArrayList<>();
                        for (CharSequence tipMessage : tipMessageList) {
                            final UnionTypeItemObject unionTypeItemObject = mPresenter.createTipMessageDefault(tipMessage);
                            if (unionTypeItemObject != null) {
                                contentList.add(unionTypeItemObject);
                            } else {
                                MSIMUikitLog.e("unexpected. onReceivedTipMessageList create UnionTypeItemObject is null. tipMessage:%s", tipMessage);
                            }
                        }
                        groupArrayList.appendGroupItems(GROUP_CONTENT, contentList);
                    })
                    .commit(() -> {
                        final int count = mAdapter.getItemCount();
                        if (count <= 0) {
                            autoScrollToEnd.setObject(Boolean.TRUE);
                        } else {
                            //noinspection ConstantConditions
                            int lastPosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                            if (lastPosition < 0) {
                                autoScrollToEnd.setObject(Boolean.TRUE);
                            } else {
                                if (lastPosition >= count - 1) {
                                    autoScrollToEnd.setObject(Boolean.TRUE);
                                }
                            }
                        }
                    }, () -> {
                        if (autoScrollToEnd.getObject() == Boolean.TRUE && isResumed()) {
                            scrollToPosition(binding.recyclerView, mAdapter.getItemCount() - 1);
                        } else {
                            // 有新消息，显示向下的箭头
                            showNewMessagesTipView();
                        }
                    });
        }
    }

    private class LocalEnqueueCallback implements MSIMCallback<GeneralResult>, AbortSignal {

        private final boolean mClearEditTextWhenSuccess;

        private LocalEnqueueCallback(boolean clearEditTextWhenSuccess) {
            this.mClearEditTextWhenSuccess = clearEditTextWhenSuccess;
        }

        @Override
        public void onCallback(@NonNull GeneralResult result) {
            if (isAbort()) {
                return;
            }
            Threads.postUi(() -> {
                if (isAbort()) {
                    return;
                }

                final ImsdkUikitChatRoomFragmentContentBinding binding = mContentBinding;
                if (binding == null) {
                    MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                    return;
                }
                MSIMUikitLog.v("onCallback %s", result);

                if (result.isSuccess()) {
                    if (mClearEditTextWhenSuccess) {
                        // 消息发送成功之后，清空输入框
                        clearInputText();
                    }
                } else {
                    TipUtil.showOrDefault(result.message);
                }
            });
        }

        @Override
        public boolean isAbort() {
            return mEnqueueCallback != this;
        }
    }

}
