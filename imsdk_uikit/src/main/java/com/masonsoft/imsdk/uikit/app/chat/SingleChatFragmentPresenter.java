package com.masonsoft.imsdk.uikit.app.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.lang.GeneralResultException;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageViewHolder;
import com.masonsoft.imsdk.uikit.widget.MSIMConversationChangedViewHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.PagePresenter;
import io.github.idonans.lang.DisposableHolder;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;

public class SingleChatFragmentPresenter extends PagePresenter<UnionTypeItemObject, GeneralResult, SingleChatFragment.ViewImpl> {

    private static final boolean DEBUG = true;

    private final long mSessionUserId;
    private final int mConversationType = MSIMConstants.ConversationType.C2C;
    private final long mTargetUserId;
    private final int mPageSize = 20;
    private long mFirstMessageSeq = -1;
    private long mLastMessageSeq = -1;

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMConversationChangedViewHelper mConversationChangedViewHelper;

    private final DisposableHolder mDefaultRequestHolder = new DisposableHolder();

    @UiThread
    public SingleChatFragmentPresenter(@NonNull SingleChatFragment.ViewImpl view) {
        super(view);
        mSessionUserId = MSIMManager.getInstance().getSessionUserId();
        mTargetUserId = view.getTargetUserId();

        mConversationChangedViewHelper = new MSIMConversationChangedViewHelper() {
            @Override
            protected void onConversationChanged(@Nullable MSIMConversation conversation, @Nullable Object customObject) {
                if (conversation == null) {
                    reloadOrRequestMoreMessage();
                    return;
                }

                final long sessionUserId = conversation.getSessionUserId();
                final int conversationType = conversation.getConversationType();
                final long targetUserId = conversation.getTargetUserId();
                if (mSessionUserId == sessionUserId
                        && mConversationType == conversationType
                        && mTargetUserId == targetUserId) {
                    reloadOrRequestMoreMessage();
                }
            }
        };
        mConversationChangedViewHelper.setConversationByTargetUserId(
                mSessionUserId,
                mConversationType,
                mTargetUserId
        );
    }

    @Nullable
    public SingleChatFragment.ViewImpl getView() {
        return (SingleChatFragment.ViewImpl) super.getView();
    }

    private void reloadOrRequestMoreMessage() {
        if (mLastMessageSeq > 0) {
            if (!getNextPageRequestStatus().isLoading()) {
                requestNextPage(true);
            }
        } else {
            if (!getInitRequestStatus().isLoading()) {
                requestInit(true);
            }
        }
    }

    private final UnionTypeViewHolderListeners.OnItemClickListener mOnHolderItemClickListener = viewHolder -> {
        SingleChatFragment.ViewImpl view = getView();
        if (view != null) {
            IMMessageViewHolder.Helper.showPreview(viewHolder, view.getTargetUserId());
        }
    };

    private final UnionTypeViewHolderListeners.OnItemLongClickListener mOnHolderItemLongClickListener = viewHolder -> {
        SingleChatFragment.ViewImpl view = getView();
        if (view != null) {
            IMMessageViewHolder.Helper.showMenu(viewHolder);
        }
    };

    @Nullable
    private UnionTypeItemObject createDefault(@Nullable MSIMMessage message) {
        if (message == null) {
            return null;
        }
        final DataObject<MSIMMessage> dataObject = new DataObject<>(message)
                .putExtHolderItemClick1(mOnHolderItemClickListener)
                .putExtHolderItemLongClick1(mOnHolderItemLongClickListener);
        return IMMessageViewHolder.Helper.createDefault(dataObject, mSessionUserId);
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
                        mSessionUserId,
                        0,
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
        // 记录上一页，下一页参数
        if (result.items == null || result.items.isEmpty()) {
            mFirstMessageSeq = -1;
            mLastMessageSeq = -1;
            setPrePageRequestEnable(false);
            setNextPageRequestEnable(false);
        } else {
            mFirstMessageSeq = ((MSIMMessage) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(0)).itemObject).object).getSeq();
            mLastMessageSeq = ((MSIMMessage) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(result.items.size() - 1)).itemObject).object).getSeq();
            setPrePageRequestEnable(true);
            setNextPageRequestEnable(true);
        }

        super.onInitRequestResult(view, result);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createPrePageRequest() throws Exception {
        MSIMUikitLog.v("createPrePageRequest");
        if (DEBUG) {
            MSIMUikitLog.v("createPrePageRequest sessionUserId:%s, mConversationType:%s, targetUserId:%s, pageSize:%s, firstMessageSeq:%s",
                    mSessionUserId,
                    mConversationType,
                    mTargetUserId,
                    mPageSize,
                    mFirstMessageSeq);
        }

        if (mFirstMessageSeq <= 0) {
            MSIMUikitLog.e("createPrePageRequest invalid firstMessageSeq:%s", mFirstMessageSeq);
            return null;
        }

        return Single.just("")
                .map(input -> MSIMManager.getInstance().getMessageManager().pageQueryHistoryMessage(
                        mSessionUserId,
                        mFirstMessageSeq,
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
        // 记录上一页，下一页参数
        if (result.items != null && !result.items.isEmpty()) {
            mFirstMessageSeq = ((MSIMMessage) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(0)).itemObject).object).getSeq();
        }
        super.onPrePageRequestResult(view, result);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createNextPageRequest() throws Exception {
        MSIMUikitLog.v("createNextPageRequest");
        if (DEBUG) {
            MSIMUikitLog.v("createNextPageRequest sessionUserId:%s, mConversationType:%s, targetUserId:%s, pageSize:%s, lastMessageSeq:%s",
                    mSessionUserId,
                    mConversationType,
                    mTargetUserId,
                    mPageSize,
                    mLastMessageSeq);
        }

        if (mLastMessageSeq <= 0) {
            MSIMUikitLog.e("createNextPageRequest invalid lastMessageSeq:%s", mLastMessageSeq);
            return null;
        }

        return Single.just("")
                .map(input -> MSIMManager.getInstance().getMessageManager().pageQueryNewMessage(
                        mSessionUserId,
                        mLastMessageSeq,
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
        // 记录上一页，下一页参数
        if (result.items != null && !result.items.isEmpty()) {
            mLastMessageSeq = ((MSIMMessage) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(result.items.size() - 1)).itemObject).object).getSeq();
        }
        super.onNextPageRequestResult(view, result);
    }

    @Override
    public void setAbort() {
        super.setAbort();
        mDefaultRequestHolder.clear();
    }

}
