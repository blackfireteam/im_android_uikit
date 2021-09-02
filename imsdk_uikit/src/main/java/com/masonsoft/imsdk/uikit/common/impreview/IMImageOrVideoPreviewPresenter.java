package com.masonsoft.imsdk.uikit.common.impreview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.google.common.collect.Lists;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.lang.GeneralResultException;
import com.masonsoft.imsdk.uikit.MSIMUikitConstants;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMMessageViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.PagePresenter;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;

public class IMImageOrVideoPreviewPresenter extends PagePresenter<UnionTypeItemObject, Object, IMImageOrVideoPreviewDialog.ViewImpl> {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private final long mSessionUserId;
    private final int mConversationType;
    private final long mTargetUserId;
    private final int mPageSize = 20;
    private long mFirstMessageSeq = -1;
    private long mLastMessageSeq = -1;

    @UiThread
    public IMImageOrVideoPreviewPresenter(@NonNull IMImageOrVideoPreviewDialog.ViewImpl view, long targetUserId, long initMessageSeq) {
        super(view);
        setPrePageRequestEnable(initMessageSeq >= 0);
        setNextPageRequestEnable(initMessageSeq >= 0);
        mSessionUserId = MSIMManager.getInstance().getSessionUserId();
        mConversationType = MSIMConstants.ConversationType.C2C;
        mTargetUserId = targetUserId;
        mFirstMessageSeq = initMessageSeq;
        mLastMessageSeq = initMessageSeq;
    }

    void showInitMessage(MSIMMessage initMessage) {
        IMImageOrVideoPreviewDialog.ViewImpl view = getView();
        if (view == null) {
            return;
        }

        view.onInitRequestResult(
                new DynamicResult<UnionTypeItemObject, Object>()
                        .setItems(Lists.newArrayList(create(initMessage, true)))
        );
    }

    private final UnionTypeViewHolderListeners.OnItemClickListener mOnHolderItemClickListener = viewHolder -> {
        IMImageOrVideoPreviewDialog.ViewImpl view = (IMImageOrVideoPreviewDialog.ViewImpl) getView();
        if (view != null) {
            view.hide();
        }
    };

    @Nullable
    private UnionTypeItemObject create(MSIMMessage message) {
        return create(message, false);
    }

    @Nullable
    private UnionTypeItemObject create(MSIMMessage message, boolean autoPlay) {
        if (message == null) {
            return null;
        }

        return IMMessageViewHolder.Helper.createPreviewDefault(
                new DataObject<>(message)
                        .putExtObjectBoolean1(autoPlay)
                        .putExtHolderItemClick1(mOnHolderItemClickListener),
                mSessionUserId
        );
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, Object>> createInitRequest() throws Exception {
        return null;
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, Object>> createPrePageRequest() throws Exception {
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
                        UnionTypeItemObject item = create(message);
                        if (item == null) {
                            if (DEBUG) {
                                MSIMUikitLog.e("createPrePageRequest ignore null UnionTypeItemObject");
                            }
                            continue;
                        }
                        target.add(item);
                    }

                    return new DynamicResult<UnionTypeItemObject, Object>()
                            .setItems(target)
                            .setPayload(page.generalResult)
                            .setError(GeneralResultException.createOrNull(page.generalResult));
                });
    }

    @Override
    protected void onPrePageRequestResult(@NonNull IMImageOrVideoPreviewDialog.ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, Object> result) {
        MSIMUikitLog.v("onPrePageRequestResult");

        // 记录上一页，下一页参数
        if (result.items != null && !result.items.isEmpty()) {
            mFirstMessageSeq = ((MSIMMessage) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(0)).itemObject).object).getSeq();
        }
        super.onPrePageRequestResult(view, result);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, Object>> createNextPageRequest() throws Exception {
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
                        UnionTypeItemObject item = create(message);
                        if (item == null) {
                            if (DEBUG) {
                                MSIMUikitLog.e("createNextPageRequest ignore null UnionTypeItemObject");
                            }
                            continue;
                        }
                        target.add(item);
                    }

                    return new DynamicResult<UnionTypeItemObject, Object>()
                            .setItems(target)
                            .setPayload(page.generalResult)
                            .setError(GeneralResultException.createOrNull(page.generalResult));
                });
    }

    @Override
    protected void onNextPageRequestResult(@NonNull IMImageOrVideoPreviewDialog.ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, Object> result) {
        MSIMUikitLog.v("onNextPageRequestResult");

        // 记录上一页，下一页参数
        if (result.items != null && !result.items.isEmpty()) {
            mLastMessageSeq = ((MSIMMessage) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(result.items.size() - 1)).itemObject).object).getSeq();
        }
        super.onNextPageRequestResult(view, result);
    }

}
