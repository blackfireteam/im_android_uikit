package com.masonsoft.imsdk.uikit.app.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMMessageFactory;
import com.masonsoft.imsdk.MSIMMessageListener;
import com.masonsoft.imsdk.MSIMMessageListenerProxy;
import com.masonsoft.imsdk.MSIMMessagePageContext;
import com.masonsoft.imsdk.MSIMUserInfo;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.lang.GeneralResultException;
import com.masonsoft.imsdk.lang.SafetyRunnable;
import com.masonsoft.imsdk.uikit.CustomIMMessageFactory;
import com.masonsoft.imsdk.uikit.MSIMConversationLoader;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.MSIMUserInfoLoader;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageViewHolder;
import com.masonsoft.imsdk.util.Objects;
import com.masonsoft.imsdk.util.TimeDiffDebugHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.idonans.core.thread.BatchQueue;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.IOUtil;
import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.PagePresenter;
import io.github.idonans.lang.DisposableHolder;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.github.idonans.uniontype.UnionTypeMapper;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;

public class SingleChatFragmentPresenter extends PagePresenter<UnionTypeItemObject, GeneralResult, SingleChatFragment.ViewImpl> {

    private static final boolean DEBUG = true;

    private final long mSessionUserId;
    private final int mConversationType = MSIMConstants.ConversationType.C2C;
    private final long mTargetUserId;
    private final int mPageSize = 20;

    @Nullable
    private MSIMMessage mLastMessage;
    private long mConsumedTypedLastMessageSeq;

    private final MSIMMessagePageContext mMessagePageContext = new MSIMMessagePageContext();
    private final MSIMMessageListener mMessageListener;

    private final DisposableHolder mDefaultRequestHolder = new DisposableHolder();

    private final BatchQueue<MSIMMessage> mBatchQueueUpdateOrRemoveMessage = new BatchQueue<>();

    private MSIMUserInfoLoader mTargetUserInfoLoader;
    private MSIMConversationLoader mConversationLoader;

    @UiThread
    public SingleChatFragmentPresenter(@NonNull SingleChatFragment.ViewImpl view) {
        super(view);
        mSessionUserId = MSIMManager.getInstance().getSessionUserId();
        mTargetUserId = view.getTargetUserId();
        MSIMManager.getInstance().getConversationManager().getConversationByTargetUserId(
                mSessionUserId,
                MSIMConstants.ConversationType.C2C,
                mTargetUserId
        );

        mTargetUserInfoLoader = new MSIMUserInfoLoader() {
            @Override
            protected void onUserInfoLoad(@NonNull MSIMUserInfo userInfo) {
                super.onUserInfoLoad(userInfo);

                showTargetUserInfo(userInfo);
            }
        };
        mConversationLoader = new MSIMConversationLoader() {
            @Override
            protected void onConversationLoad(@NonNull MSIMConversation conversation) {
                super.onConversationLoad(conversation);

                reloadOrRequestMoreMessage();
            }
        };

        mBatchQueueUpdateOrRemoveMessage.setConsumer(this::updateOrRemoveMessage);
        mMessageListener = new MSIMMessageListenerProxy(this::updateOrRemoveMessage);
        MSIMManager.getInstance().getMessageManager().addMessageListener(mMessagePageContext, mMessageListener);
    }

    void start() {
        mTargetUserInfoLoader.setUserInfo(MSIMUserInfo.mock(mTargetUserId), false);
        mConversationLoader.setConversation(MSIMConversation.mock(mSessionUserId, mConversationType, mTargetUserId), false);
    }

    private void showTargetUserInfo(@NonNull MSIMUserInfo userInfo) {
        final SingleChatFragment.ViewImpl view = getView();
        if (view != null) {
            view.showTargetUserInfo(userInfo);
        }
    }

    @Nullable
    public SingleChatFragment.ViewImpl getView() {
        return (SingleChatFragment.ViewImpl) super.getView();
    }

    private void updateOrRemoveMessage(@NonNull MSIMMessage message) {
        final long sessionUserId = message.getSessionUserId();
        final int conversationType = message.getConversationType();
        final long targetUserId = message.getTargetUserId();
        if (mSessionUserId == sessionUserId
                && mConversationType == conversationType
                && mTargetUserId == targetUserId) {

            mBatchQueueUpdateOrRemoveMessage.add(message);
        }
    }

    private void updateOrRemoveMessage(@Nullable List<MSIMMessage> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 移除重复的消息
        final List<MSIMMessage> removeDuplicateList = new ArrayList<>();
        for (MSIMMessage message : list) {
            removeDuplicateList.remove(message);
            removeDuplicateList.add(message);
        }

        final List<UnionTypeItemObject> unionTypeItemObjectList = new ArrayList<>();
        for (MSIMMessage message : removeDuplicateList) {
            final UnionTypeItemObject unionTypeItemObject = createDefault(message);
            if (unionTypeItemObject != null) {
                unionTypeItemObjectList.add(unionTypeItemObject);
            }
        }

        if (unionTypeItemObjectList.isEmpty()) {
            return;
        }

        final SingleChatFragment.ViewImpl view = getView();
        if (view == null) {
            return;
        }

        final TimeDiffDebugHelper timeDiffDebugHelper = new TimeDiffDebugHelper(Objects.defaultObjectTag(this));
        timeDiffDebugHelper.mark();
        view.updateOrRemoveMessageList(unionTypeItemObjectList);
        timeDiffDebugHelper.mark();
        timeDiffDebugHelper.print("updateOrRemoveMessage unionTypeItemObjectList size:" + unionTypeItemObjectList.size());

    }

    private void reloadOrRequestMoreMessage() {
        if (getView() == null) {
            MSIMUikitLog.v("reloadOrRequestMoreMessage ignore. view is null.");
            return;
        }

        MSIMUikitLog.v("reloadOrRequestMoreMessage");
        if (getInitRequestStatus().isLoading()) {
            MSIMUikitLog.v("reloadOrRequestMoreMessage abort getInitRequestStatus().isLoading()");
            return;
        }
        if (getNextPageRequestStatus().isLoading()) {
            MSIMUikitLog.v("reloadOrRequestMoreMessage abort getNextPageRequestStatus().isLoading()");
            return;
        }
        if (isNextPageRequestEnable()) {
            MSIMUikitLog.v("reloadOrRequestMoreMessage start requestNextPage(true)");
            requestNextPage(true);
        } else {
            MSIMUikitLog.v("reloadOrRequestMoreMessage start requestInit(true)");
            requestInit(true);
        }
    }

    private final UnionTypeViewHolderListeners.OnItemClickListener mOnHolderItemClickListener = viewHolder -> {
        SingleChatFragment.ViewImpl view = getView();
        if (view != null) {
            IMBaseMessageViewHolder.Helper.showPreview(viewHolder);
        }
    };

    private final UnionTypeViewHolderListeners.OnItemLongClickListener mOnHolderItemLongClickListener = viewHolder -> {
        SingleChatFragment.ViewImpl view = getView();
        if (view != null) {
            IMBaseMessageViewHolder.Helper.showMenu(viewHolder);
        }
    };

    @Nullable
    private UnionTypeItemObject createDefault(@Nullable MSIMMessage message) {
        if (message == null) {
            return null;
        }
        final DataObject dataObject = new DataObject(message)
                .putExtHolderItemClick1(mOnHolderItemClickListener)
                .putExtHolderItemLongClick1(mOnHolderItemLongClickListener);
        final int unionType = IMBaseMessageViewHolder.Helper.getDefaultUnionType(dataObject);
        if (unionType != UnionTypeMapper.UNION_TYPE_NULL) {
            return new UnionTypeItemObject(unionType, dataObject);
        }
        return null;
    }

    @Override
    protected void onInitRequest(@NonNull SingleChatFragment.ViewImpl view) {
        MSIMUikitLog.v("onInitRequest");
        super.onInitRequest(view);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createInitRequest() throws Exception {
        MSIMUikitLog.v("createInitRequest");
        if (DEBUG) {
            MSIMUikitLog.v("createInitRequest sessionUserId:%s, mConversationType:%s, targetUserId:%s, pageSize:%s",
                    mSessionUserId,
                    mConversationType,
                    mTargetUserId,
                    mPageSize);
        }

        return Single.just("")
                .map(input -> MSIMManager.getInstance().getMessageManager().pageQueryHistoryMessage(
                        mMessagePageContext,
                        true,
                        mSessionUserId,
                        mPageSize,
                        mConversationType,
                        mTargetUserId))
                .map(page -> {
                    List<MSIMMessage> messageList = page.items;
                    if (messageList == null) {
                        messageList = new ArrayList<>();
                    }
                    Collections.reverse(messageList);
                    final List<UnionTypeItemObject> target = new ArrayList<>();
                    for (MSIMMessage message : messageList) {
                        UnionTypeItemObject item = createDefault(message);
                        if (item == null) {
                            if (DEBUG) {
                                MSIMUikitLog.e("createInitRequest ignore null UnionTypeItemObject");
                            }
                            continue;
                        }
                        target.add(item);
                    }

                    return new DynamicResult<UnionTypeItemObject, GeneralResult>()
                            .setItems(target)
                            .setPayload(page.generalResult)
                            .setError(GeneralResultException.createOrNull(page.generalResult));
                });
    }

    @Override
    protected void onInitRequestResult(@NonNull SingleChatFragment.ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
        MSIMUikitLog.v("onInitRequestResult");
        if (result.items == null || result.items.isEmpty()) {
            mLastMessage = null;
            setPrePageRequestEnable(false);
            setNextPageRequestEnable(false);
        } else {
            mLastMessage = (MSIMMessage) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(result.items.size() - 1)).itemObject).object;
            setPrePageRequestEnable(true);
            setNextPageRequestEnable(true);
        }

        super.onInitRequestResult(view, result);

        reloadAndCheckUpdate(result.items);
    }

    @Override
    protected void onPrePageRequest(@NonNull SingleChatFragment.ViewImpl view) {
        MSIMUikitLog.v("onPrePageRequest");
        super.onPrePageRequest(view);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createPrePageRequest() throws Exception {
        MSIMUikitLog.v("createPrePageRequest");
        if (DEBUG) {
            MSIMUikitLog.v("createPrePageRequest sessionUserId:%s, mConversationType:%s, targetUserId:%s, pageSize:%s",
                    mSessionUserId,
                    mConversationType,
                    mTargetUserId,
                    mPageSize);
        }

        return Single.just("")
                .map(input -> MSIMManager.getInstance().getMessageManager().pageQueryHistoryMessage(
                        mMessagePageContext,
                        false,
                        mSessionUserId,
                        mPageSize,
                        mConversationType,
                        mTargetUserId))
                .map(page -> {
                    List<MSIMMessage> messageList = page.items;
                    if (messageList == null) {
                        messageList = new ArrayList<>();
                    }
                    Collections.reverse(messageList);
                    List<UnionTypeItemObject> target = new ArrayList<>();
                    for (MSIMMessage message : messageList) {
                        UnionTypeItemObject item = createDefault(message);
                        if (item == null) {
                            if (DEBUG) {
                                MSIMUikitLog.e("createPrePageRequest ignore null UnionTypeItemObject");
                            }
                            continue;
                        }
                        target.add(item);
                    }

                    return new DynamicResult<UnionTypeItemObject, GeneralResult>()
                            .setItems(target)
                            .setPayload(page.generalResult)
                            .setError(GeneralResultException.createOrNull(page.generalResult));
                });
    }

    @Override
    protected void onPrePageRequestResult(@NonNull SingleChatFragment.ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
        MSIMUikitLog.v("onPrePageRequestResult");
        super.onPrePageRequestResult(view, result);

        reloadAndCheckUpdate(result.items);
    }

    @Override
    protected void onNextPageRequest(@NonNull SingleChatFragment.ViewImpl view) {
        MSIMUikitLog.v("onNextPageRequest");
        super.onNextPageRequest(view);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createNextPageRequest() throws Exception {
        MSIMUikitLog.v("createNextPageRequest");
        if (DEBUG) {
            MSIMUikitLog.v("createNextPageRequest sessionUserId:%s, mConversationType:%s, targetUserId:%s, pageSize:%s",
                    mSessionUserId,
                    mConversationType,
                    mTargetUserId,
                    mPageSize);
        }

        return Single.just("")
                .map(input -> MSIMManager.getInstance().getMessageManager().pageQueryNewMessage(
                        mMessagePageContext,
                        false,
                        mSessionUserId,
                        mPageSize,
                        mConversationType,
                        mTargetUserId))
                .map(page -> {
                    List<MSIMMessage> messageList = page.items;
                    if (messageList == null) {
                        messageList = new ArrayList<>();
                    }
                    List<UnionTypeItemObject> target = new ArrayList<>();
                    for (MSIMMessage message : messageList) {
                        UnionTypeItemObject item = createDefault(message);
                        if (item == null) {
                            if (DEBUG) {
                                MSIMUikitLog.e("createNextPageRequest ignore null UnionTypeItemObject");
                            }
                            continue;
                        }
                        target.add(item);
                    }

                    return new DynamicResult<UnionTypeItemObject, GeneralResult>()
                            .setItems(target)
                            .setPayload(page.generalResult)
                            .setError(GeneralResultException.createOrNull(page.generalResult));
                });
    }

    @Override
    protected void onNextPageRequestResult(@NonNull SingleChatFragment.ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
        MSIMUikitLog.v("onNextPageRequestResult");

        if (result.items != null && !result.items.isEmpty()) {
            mLastMessage = ((MSIMMessage) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(result.items.size() - 1)).itemObject).object);
        }

        super.onNextPageRequestResult(view, result);

        reloadAndCheckUpdate(result.items);
    }

    @Override
    public void setAbort() {
        super.setAbort();
        mDefaultRequestHolder.clear();
        IOUtil.closeQuietly(mMessagePageContext);
    }

    /**
     * 当前用户正在输入
     */
    public void setBeingTyped() {
        final MSIMMessage lastMessage = mLastMessage;
        if (lastMessage == null) {
            return;
        }
        final long seq = lastMessage.getSeq();
        if (mConsumedTypedLastMessageSeq == seq) {
            return;
        }

        final boolean received = lastMessage.isReceived();
        final long duration = System.currentTimeMillis() - lastMessage.getTimeMs();
        if (received && duration <= TimeUnit.SECONDS.toMillis(5)) {
            // 最后一条消息是对方刚刚发送的
            mConsumedTypedLastMessageSeq = seq;
            final String body = CustomIMMessageFactory.createCustomSignalingTyped();
            final MSIMMessage message = MSIMMessageFactory.createCustomSignalingMessage(body);
            MSIMManager.getInstance().getMessageManager().sendCustomSignaling(
                    lastMessage.getSessionUserId(),
                    lastMessage.getFromUserId(),
                    message
            );
        }
    }

    private void reloadAndCheckUpdate(final @Nullable Collection<UnionTypeItemObject> targetList) {
        if (targetList == null || targetList.isEmpty()) {
            return;
        }

        final List<MSIMMessage> messageList = new ArrayList<>();
        for (UnionTypeItemObject unionTypeItemObject : targetList) {
            final DataObject dataObject = unionTypeItemObject.getItemObject(DataObject.class);
            if (dataObject == null) {
                continue;
            }
            final MSIMMessage message = dataObject.getObject(MSIMMessage.class);
            if (message == null) {
                continue;
            }
            messageList.add(message);
        }
        reloadAndCheckUpdate(messageList);
    }

    private void reloadAndCheckUpdate(final @Nullable List<MSIMMessage> messageList) {
        if (messageList == null || messageList.isEmpty()) {
            return;
        }
        Threads.postBackground(new SafetyRunnable(() -> {
            for (MSIMMessage message : messageList) {
                if (SingleChatFragmentPresenter.this.isAbort()) {
                    break;
                }
                if (message == null) {
                    continue;
                }

                MSIMMessage reloadMessage = MSIMManager.getInstance().getMessageManager().getMessage(
                        message.getSessionUserId(),
                        message.getConversationType(),
                        message.getTargetUserId(),
                        message.getMessageId()
                );

                if (reloadMessage == null) {
                    continue;
                }
                if (message.getLastModify() >= reloadMessage.getLastModify()) {
                    continue;
                }

                updateOrRemoveMessage(reloadMessage);
            }
        }));
    }

}
