package com.masonsoft.imsdk.uikit.app.chat;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMCallback;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMMessageFactory;
import com.masonsoft.imsdk.MSIMWeakCallback;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.uikit.MSIMRtcMessageManager;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.common.app.CustomInputFragment;
import com.masonsoft.imsdk.uikit.common.locationpicker.LocationInfo;
import com.masonsoft.imsdk.uikit.common.mediapicker.MediaData;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.microlifecycle.MicroLifecycleComponentManagerHost;
import com.masonsoft.imsdk.uikit.common.microlifecycle.VisibleRecyclerViewMicroLifecycleComponentManager;
import com.masonsoft.imsdk.uikit.common.simpledialog.SimpleBottomActionsDialog;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitSingleChatFragmentContentBinding;
import com.masonsoft.imsdk.uikit.databinding.ImsdkUikitSingleChatFragmentTopBarBinding;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.util.ActivityUtil;
import com.masonsoft.imsdk.uikit.util.TipUtil;
import com.masonsoft.imsdk.util.Objects;
import com.masonsoft.imsdk.util.TimeDiffDebugHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.idonans.core.AbortSignal;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.UnionTypeStatusPageView;
import io.github.idonans.dynamic.uniontype.loadingstatus.UnionTypeLoadingStatus;
import io.github.idonans.lang.util.ViewUtil;
import io.github.idonans.uniontype.Host;
import io.github.idonans.uniontype.UnionTypeAdapter;
import io.github.idonans.uniontype.UnionTypeItemObject;

/**
 * 单聊页面
 *
 * @see com.masonsoft.imsdk.MSIMConstants.ConversationType#C2C
 */
public class SingleChatFragment extends CustomInputFragment {

    public static SingleChatFragment newInstance(long targetUserId) {
        Bundle args = new Bundle();
        args.putLong(MSIMUikitConstants.ExtrasKey.TARGET_USER_ID, targetUserId);
        SingleChatFragment fragment = new SingleChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private long mTargetUserId;
    @Nullable
    private ImsdkUikitSingleChatFragmentTopBarBinding mTopBarBinding;
    @Nullable
    private ImsdkUikitSingleChatFragmentContentBinding mContentBinding;
    private LocalEnqueueCallback mEnqueueCallback;

    private UnionTypeAdapter mDataAdapter;
    private SingleChatFragmentPresenter mPresenter;
    private ViewImpl mViewImpl;
    private MicroLifecycleComponentManager mMicroLifecycleComponentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mTargetUserId = args.getLong(MSIMUikitConstants.ExtrasKey.TARGET_USER_ID, mTargetUserId);
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
        mTopBarBinding = ImsdkUikitSingleChatFragmentTopBarBinding.inflate(inflater, getCustomBinding().customTopBarContainer, true);
        mContentBinding = ImsdkUikitSingleChatFragmentContentBinding.inflate(inflater, getCustomBinding().customContentContainer, true);

        getCustomBinding().customSoftKeyboard.getCustomConfig().setShowSnapchat(false);

        ViewUtil.onClick(mTopBarBinding.topBarBack, v -> ActivityUtil.requestBackPressed(SingleChatFragment.this));
        mTopBarBinding.topBarTitle.setUserInfo(mTargetUserId, null);
        mTopBarBinding.beingTypedView.setTarget(MSIMManager.getInstance().getSessionUserId(), mTargetUserId);

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
                            sendMarkAsRead();
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
        mViewImpl = new ViewImpl(adapter);
        clearPresenter();
        mPresenter = new SingleChatFragmentPresenter(mViewImpl);
        mViewImpl.setPresenter(mPresenter);
        recyclerView.setAdapter(adapter);

        getCustomBinding().keyboardEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mPresenter != null) {
                    mPresenter.setBeingTyped();
                }
            }
        });

        mPresenter.requestInit();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sendMarkAsRead();
    }

    private void showBottomActions() {
        final Activity activity = ActivityUtil.getActiveAppCompatActivity(getContext());
        if (activity == null) {
            MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.ACTIVITY_IS_NULL);
            return;
        }

        final List<String> actions = Lists.newArrayList("模拟并发消息");
        final SimpleBottomActionsDialog dialog = new SimpleBottomActionsDialog(
                activity,
                actions
        );
        dialog.setOnActionClickListener((index, actionText) -> {
            if (index == 0) {
                // 模拟并发消息
                final long targetUserId = mTargetUserId;
                mockMultiMessages(targetUserId);
            }
        });
        dialog.show();
    }

    private static void mockMultiMessages(final long targetUserId) {
        final String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        final int size = 10;
        for (int i = 1; i <= size; i++) {
            final int index = i;
            Threads.postBackground(() -> {
                final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
                final MSIMMessage message = MSIMMessageFactory.createTextMessage("[" + time + "] mock concurrent message [" + index + "/" + size + "]");
                MSIMManager.getInstance().getMessageManager().sendMessage(
                        sessionUserId,
                        message,
                        targetUserId
                );
            });
        }
    }

    @Override
    protected void submitTextMessage(boolean snapchat, String text) {
        mEnqueueCallback = new LocalEnqueueCallback(true);
        final MSIMMessage message = MSIMMessageFactory.createTextMessage(text);
        // MSIMMessageFactory.setSnapchat(message, snapchat);
        MSIMManager.getInstance().getMessageManager().sendMessage(
                MSIMManager.getInstance().getSessionUserId(),
                message,
                mTargetUserId,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    @Override
    protected void submitMediaMessage(boolean snapchat, @NonNull List<MediaData.MediaInfo> mediaInfoList) {
        for (MediaData.MediaInfo mediaInfo : mediaInfoList) {
            mEnqueueCallback = new LocalEnqueueCallback(false);
            final MSIMMessage message;
            if (mediaInfo.isVideoMimeType()) {
                message = MSIMMessageFactory.createVideoMessage(mediaInfo.uri);
            } else {
                message = MSIMMessageFactory.createImageMessage(mediaInfo.uri);
            }
            // MSIMMessageFactory.setSnapchat(message, snapchat);
            MSIMManager.getInstance().getMessageManager().sendMessage(
                    MSIMManager.getInstance().getSessionUserId(),
                    message,
                    mTargetUserId,
                    new MSIMWeakCallback<>(mEnqueueCallback)
            );
        }
    }

    @Override
    protected void submitFlashImageMessage(boolean snapchat, @NonNull List<MediaData.MediaInfo> mediaInfoList) {
        for (MediaData.MediaInfo mediaInfo : mediaInfoList) {
            mEnqueueCallback = new LocalEnqueueCallback(false);
            final MSIMMessage message = MSIMMessageFactory.createFlashImageMessage(mediaInfo.uri);
            // MSIMMessageFactory.setSnapchat(message, snapchat);
            MSIMManager.getInstance().getMessageManager().sendMessage(
                    MSIMManager.getInstance().getSessionUserId(),
                    message,
                    mTargetUserId,
                    new MSIMWeakCallback<>(mEnqueueCallback)
            );
        }
    }

    @Override
    protected void submitAudioMessage(boolean snapchat, final String audioFilePath) {
        mEnqueueCallback = new LocalEnqueueCallback(true);
        final MSIMMessage message = MSIMMessageFactory.createAudioMessage(audioFilePath);
        // MSIMMessageFactory.setSnapchat(message, snapchat);
        MSIMManager.getInstance().getMessageManager().sendMessage(
                MSIMManager.getInstance().getSessionUserId(),
                message,
                mTargetUserId,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    @Override
    protected void submitLocationMessage(boolean snapchat, @NonNull LocationInfo locationInfo, long zoom) {
        mEnqueueCallback = new LocalEnqueueCallback(false);
        final MSIMMessage message = MSIMMessageFactory.createLocationMessage(
                locationInfo.title,
                locationInfo.subTitle,
                locationInfo.lat,
                locationInfo.lng,
                zoom
        );
        // MSIMMessageFactory.setSnapchat(message, snapchat);
        MSIMManager.getInstance().getMessageManager().sendMessage(
                MSIMManager.getInstance().getSessionUserId(),
                message,
                mTargetUserId,
                new MSIMWeakCallback<>(mEnqueueCallback)
        );
    }

    @Override
    protected void submitClickRtcAudio(boolean snapchat) {
        MSIMRtcMessageManager.getInstance().startRtcMessage(mTargetUserId, null, false);
    }

    @Override
    protected void submitClickRtcVideo(boolean snapchat) {
        MSIMRtcMessageManager.getInstance().startRtcMessage(mTargetUserId, null, true);
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

    private void sendMarkAsRead() {
        MSIMUikitLog.v(Objects.defaultObjectTag(this) + " sendMarkAsRead targetUserId:%s", mTargetUserId);
        MSIMManager.getInstance().getMessageManager().markAsRead(
                MSIMManager.getInstance().getSessionUserId(),
                mTargetUserId
        );
    }

    class ViewImpl extends UnionTypeStatusPageView<GeneralResult> {

        public ViewImpl(@NonNull UnionTypeAdapter adapter) {
            super(adapter);
            setAlwaysHideNoMoreData(true);
        }

        /**
         * @param unionTypeItemObjectList 更新内容或者删除条目（不存在追加的情况）
         */
        @WorkerThread
        void updateOrRemoveMessageList(@NonNull final List<UnionTypeItemObject> unionTypeItemObjectList) {
            final String tag = Objects.defaultObjectTag(this) + "[updateOrRemoveMessageList][" + System.currentTimeMillis() + "][size:]" + unionTypeItemObjectList.size();
            MSIMUikitLog.v(tag);

            getAdapter().getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final TimeDiffDebugHelper innerMergeTimeDiffDebugHelper = new TimeDiffDebugHelper("innerMergeTimeDiffDebugHelper[" + tag + "]");
                        int removedCount = 0;
                        int updateCount = 0;

                        final List<UnionTypeItemObject> currentList = groupArrayList.getGroupItems(getGroupContent());
                        if (currentList == null || currentList.isEmpty()) {
                            return;
                        }

                        // 更新或者删除
                        for (UnionTypeItemObject updateOrRemoveUnionTypeItemObject : unionTypeItemObjectList) {
                            final DataObject updateOrRemoveDataObject = updateOrRemoveUnionTypeItemObject.getItemObject(DataObject.class);
                            if (updateOrRemoveDataObject == null) {
                                continue;
                            }
                            final MSIMBaseMessage updateOrRemoveBaseMessage = updateOrRemoveDataObject.getObject(MSIMBaseMessage.class);
                            if (updateOrRemoveBaseMessage == null) {
                                continue;
                            }

                            // 从后向前遍历当前列表
                            final int currentListSize = currentList.size();
                            for (int i = currentListSize - 1; i >= 0; i--) {
                                final UnionTypeItemObject currentUnionTypeItemObject = currentList.get(i);
                                final DataObject currentDataObject = currentUnionTypeItemObject.getItemObject(DataObject.class);
                                if (currentDataObject == null) {
                                    continue;
                                }
                                final MSIMBaseMessage currentBaseMessage = currentDataObject.getObject(MSIMBaseMessage.class);
                                if (currentBaseMessage == null) {
                                    continue;
                                }

                                if (updateOrRemoveBaseMessage.equals(currentBaseMessage)) {
                                    // 待变更的 updateOrRemoveBaseMessage 匹配到当前列表的 currentBaseMessage
                                    final boolean remove = MSIMConstants.MessageType.DELETED == updateOrRemoveBaseMessage.getMessageType();
                                    if (remove) {
                                        // 移除 index i
                                        currentList.remove(i);
                                        removedCount++;
                                    } else {
                                        // 更新 index i
                                        currentList.set(i, updateOrRemoveUnionTypeItemObject);
                                        updateCount++;
                                    }
                                }
                            }
                        }

                        innerMergeTimeDiffDebugHelper.print("removedCount:" + removedCount + ", updateCount:" + updateCount);
                    })
                    .commit();
        }

        public long getTargetUserId() {
            return mTargetUserId;
        }

        private void showInitRequest(boolean delay) {
            getAdapter().getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final SingleChatFragmentPresenter presenter = (SingleChatFragmentPresenter) getPresenter();
                        if (presenter == null) {
                            return;
                        }
                        if (!presenter.getInitRequestStatus().isLoading()) {
                            return;
                        }

                        if (delay) {
                            Threads.postUi(() -> {
                                if (presenter.getInitRequestStatus().isLoading()) {
                                    showInitRequest(false);
                                }
                            }, 150L);
                            return;
                        }

                        // 清除当前页面内容
                        groupArrayList.removeAll();
                        // 显示一个全屏的 loading
                        groupArrayList.setGroupItems(
                                getGroupHeader(),
                                Lists.newArrayList(
                                        new UnionTypeItemObject(UnionTypeLoadingStatus.UNION_TYPE_LOADING_STATUS_LOADING_LARGE, new Object())
                                )
                        );
                    })
                    .commit();
        }

        @Override
        public void onInitRequest() {
            showInitRequest(true);
        }

        @Override
        public void onInitRequestResult(@NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
            super.onInitRequestResult(result);

            final ImsdkUikitSingleChatFragmentContentBinding binding = mContentBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            if (result.items != null && !result.items.isEmpty()) {
                getAdapter().getData().beginTransaction()
                        .commit(() -> {
                            final int count = getAdapter().getItemCount();
                            if (count > 0 && isResumed()) {
                                scrollToPosition(binding.recyclerView, count - 1);
                                sendMarkAsRead();
                            }
                        });
            }
        }

        private void showPrePageRequest(boolean delay) {
            getAdapter().getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final SingleChatFragmentPresenter presenter = (SingleChatFragmentPresenter) getPresenter();
                        if (presenter == null) {
                            return;
                        }
                        if (!presenter.getPrePageRequestStatus().isLoading()) {
                            return;
                        }

                        if (delay) {
                            Threads.postUi(() -> {
                                if (presenter.getPrePageRequestStatus().isLoading()) {
                                    showPrePageRequest(false);
                                }
                            }, 150L);
                            return;
                        }

                        // 使用小的 loading
                        groupArrayList.setGroupItems(
                                getGroupHeader(),
                                Lists.newArrayList(
                                        new UnionTypeItemObject(UnionTypeLoadingStatus.UNION_TYPE_LOADING_STATUS_LOADING_SMALL, new Object())
                                )
                        );
                    })
                    .commit();
        }

        @Override
        public void onPrePageRequest() {
            showPrePageRequest(true);
        }

        private void showNextPageRequest(boolean delay) {
            getAdapter().getData().beginTransaction()
                    .add((transaction, groupArrayList) -> {
                        final SingleChatFragmentPresenter presenter = (SingleChatFragmentPresenter) getPresenter();
                        if (presenter == null) {
                            return;
                        }
                        if (!presenter.getNextPageRequestStatus().isLoading()) {
                            return;
                        }

                        if (delay) {
                            Threads.postUi(() -> {
                                if (presenter.getNextPageRequestStatus().isLoading()) {
                                    showNextPageRequest(false);
                                }
                            }, 150L);
                            return;
                        }

                        // 使用小的 loading
                        groupArrayList.setGroupItems(
                                getGroupFooter(),
                                Lists.newArrayList(
                                        new UnionTypeItemObject(UnionTypeLoadingStatus.UNION_TYPE_LOADING_STATUS_LOADING_SMALL, new Object())
                                )
                        );
                    })
                    .commit();
        }

        @Override
        public void onNextPageRequest() {
            showNextPageRequest(true);
        }

        @Override
        public void onNextPageRequestResult(@NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
            super.onNextPageRequestResult(result);

            final ImsdkUikitSingleChatFragmentContentBinding binding = mContentBinding;
            if (binding == null) {
                MSIMUikitLog.e(MSIMUikitConstants.ErrorLog.BINDING_IS_NULL);
                return;
            }

            if (result.items != null && !result.items.isEmpty()) {
                getAdapter().getData().beginTransaction()
                        .commit(() -> {
                            final int count = getAdapter().getItemCount();
                            final int footerCount = getAdapter().getGroupItemsSize(getGroupFooter());
                            if (count > 0) {
                                boolean autoScroll = false;
                                //noinspection ConstantConditions
                                int lastPosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                                if (lastPosition >= count - (footerCount + 1) - result.items.size()) {
                                    // 当前滚动到最后
                                    autoScroll = true;
                                }
                                if (autoScroll && isResumed()) {
                                    scrollToPosition(binding.recyclerView, count - 1);
                                    sendMarkAsRead();
                                } else {
                                    // 显示向下的箭头
                                    showNewMessagesTipView();
                                }
                            }
                        });
            }
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

                final ImsdkUikitSingleChatFragmentContentBinding binding = mContentBinding;
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
