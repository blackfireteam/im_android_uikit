package com.masonsoft.imsdk.uikit.app.conversation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMConversationListener;
import com.masonsoft.imsdk.MSIMConversationListenerProxy;
import com.masonsoft.imsdk.MSIMConversationPageContext;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.lang.GeneralResultException;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.widget.SessionUserIdChangedViewHelper;
import com.masonsoft.imsdk.util.Objects;
import com.masonsoft.imsdk.util.TimeDiffDebugHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.idonans.core.thread.BatchQueue;
import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.page.PagePresenter;
import io.github.idonans.lang.DisposableHolder;
import io.github.idonans.uniontype.DeepDiff;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;

public class ConversationFragmentPresenter extends PagePresenter<UnionTypeItemObject, GeneralResult, ConversationFragment.ViewImpl> {

    private static final boolean DEBUG = true;

    private final SessionUserIdChangedViewHelper mSessionUserIdChangedViewHelper;
    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMConversationListener mConversationListener;
    private final int mConversationType = MSIMConstants.ConversationType.C2C;
    private final int mPageSize = 20;

    private final MSIMConversationPageContext mConversationPageContext = new MSIMConversationPageContext();
    private final DisposableHolder mDefaultRequestHolder = new DisposableHolder();

    private final BatchQueue<MSIMConversation> mBatchQueueAddOrUpdateConversation = new BatchQueue<>();

    @UiThread
    public ConversationFragmentPresenter(@NonNull ConversationFragment.ViewImpl view) {
        super(view);
        mSessionUserIdChangedViewHelper = new SessionUserIdChangedViewHelper() {
            @Override
            protected void onSessionUserIdChanged(long sessionUserId) {
                reloadWithNewSessionUserId();
            }
        };
        mConversationListener = new MSIMConversationListenerProxy(new MSIMConversationListener() {
            @Override
            public void onConversationChanged(long sessionUserId, long conversationId, int conversationType, long targetUserId) {
                addOrUpdateConversation(sessionUserId, conversationId);
            }
        });
        MSIMManager.getInstance().getConversationManager().addConversationListener(mConversationPageContext, mConversationListener);
        mBatchQueueAddOrUpdateConversation.setConsumer(this::addOrUpdateConversation);
    }

    private long getSessionUserId() {
        return mSessionUserIdChangedViewHelper.getSessionUserId();
    }

    private void reloadWithNewSessionUserId() {
        requestInit(true);
    }

    private boolean isAbort(long sessionUserId) {
        if (super.isAbort()) {
            return true;
        }
        if (getSessionUserId() != sessionUserId) {
            return true;
        }
        return getView() == null;
    }

    private void addOrUpdateConversation(long sessionUserId, long conversationId) {
        if (isAbort(sessionUserId)) {
            return;
        }

        final MSIMConversation conversation = MSIMManager.getInstance().getConversationManager().getConversation(sessionUserId, conversationId);
        if (conversation != null) {
            mBatchQueueAddOrUpdateConversation.add(conversation);
        }
    }

    @WorkerThread
    private void addOrUpdateConversation(@Nullable List<MSIMConversation> updateList) {
        if (updateList == null || updateList.isEmpty()) {
            return;
        }

        // 移除重复的会话
        final Set<String> duplicate = new HashSet<>();

        final List<UnionTypeItemObject> unionTypeItemObjectList = new ArrayList<>();
        for (MSIMConversation conversation : updateList) {
            final String key = conversation.getSessionUserId() + "_" + conversation.getConversationId();
            if (duplicate.contains(key)) {
                continue;
            }
            duplicate.add(key);

            final UnionTypeItemObject unionTypeItemObject = createDefault(conversation);
            if (unionTypeItemObject != null) {
                unionTypeItemObjectList.add(unionTypeItemObject);
            }
        }

        if (unionTypeItemObjectList.isEmpty()) {
            return;
        }

        final ConversationFragment.ViewImpl view = getView();
        if (view == null) {
            return;
        }
        final TimeDiffDebugHelper timeDiffDebugHelper = new TimeDiffDebugHelper(Objects.defaultObjectTag(this));
        timeDiffDebugHelper.mark();
        view.mergeConversationList(unionTypeItemObjectList);
        timeDiffDebugHelper.mark();
        timeDiffDebugHelper.print("mergeConversationList unionTypeItemObjectList size:" + unionTypeItemObjectList.size());
    }

    @Nullable
    public ConversationFragment.ViewImpl getView() {
        return (ConversationFragment.ViewImpl) super.getView();
    }

    @Nullable
    private UnionTypeItemObject createDefault(@Nullable MSIMConversation conversation) {
        if (conversation == null) {
            return null;
        }
        final DataObject<MSIMConversation> dataObject = new DeepDiffDataObject(conversation);
        return new UnionTypeItemObject(
                IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_CONVERSATION,
                dataObject
        );
    }

    @Override
    protected void onInitRequest(@NonNull ConversationFragment.ViewImpl view) {
        MSIMUikitLog.v("%s onInitRequest", Objects.defaultObjectTag(this));
        super.onInitRequest(view);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createInitRequest() throws Exception {
        MSIMUikitLog.v("%s createInitRequest", Objects.defaultObjectTag(this));
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " createInitRequest sessionUserId:%s, mConversationType:%s, pageSize:%s",
                    getSessionUserId(),
                    mConversationType,
                    mPageSize);
        }

        return Single.just("")
                .map(input -> MSIMManager.getInstance().getConversationManager().pageQueryNextConversation(
                        mConversationPageContext,
                        true,
                        getSessionUserId(),
                        mPageSize,
                        mConversationType))
                .map(page -> {
                    List<MSIMConversation> conversationList = page.items;
                    if (conversationList == null) {
                        conversationList = new ArrayList<>();
                    }
                    List<UnionTypeItemObject> target = new ArrayList<>();
                    for (MSIMConversation conversation : conversationList) {
                        UnionTypeItemObject item = createDefault(conversation);
                        if (item == null) {
                            if (DEBUG) {
                                MSIMUikitLog.e(Objects.defaultObjectTag(this) + " createInitRequest ignore null UnionTypeItemObject");
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
    protected void onInitRequestResult(@NonNull ConversationFragment.ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
        MSIMUikitLog.v("%s onInitRequestResult", Objects.defaultObjectTag(this));
        // 记录上一页，下一页参数
        if (result.items == null || result.items.isEmpty()) {
            setNextPageRequestEnable(false);
        } else {
            setNextPageRequestEnable(true);
        }

        super.onInitRequestResult(view, result);
    }

    @Override
    protected void onNextPageRequest(@NonNull ConversationFragment.ViewImpl view) {
        MSIMUikitLog.v("%s onNextPageRequest", Objects.defaultObjectTag(this));
        super.onNextPageRequest(view);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createNextPageRequest() throws Exception {
        MSIMUikitLog.v("%s createNextPageRequest", Objects.defaultObjectTag(this));
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " createNextPageRequest sessionUserId:%s, mConversationType:%s, pageSize:%s",
                    getSessionUserId(),
                    mConversationType,
                    mPageSize);
        }

        return Single.just("")
                .map(input -> MSIMManager.getInstance().getConversationManager().pageQueryNextConversation(
                        mConversationPageContext,
                        false,
                        getSessionUserId(),
                        mPageSize,
                        mConversationType))
                .map(page -> {
                    List<MSIMConversation> conversationList = page.items;
                    if (conversationList == null) {
                        conversationList = new ArrayList<>();
                    }
                    List<UnionTypeItemObject> target = new ArrayList<>();
                    for (MSIMConversation conversation : conversationList) {
                        UnionTypeItemObject item = createDefault(conversation);
                        if (item == null) {
                            if (DEBUG) {
                                MSIMUikitLog.e(Objects.defaultObjectTag(this) + " createNextPageRequest ignore null UnionTypeItemObject");
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
    protected void onNextPageRequestResult(@NonNull ConversationFragment.ViewImpl view, @NonNull DynamicResult<UnionTypeItemObject, GeneralResult> result) {
        MSIMUikitLog.v("%s onNextPageRequestResult", Objects.defaultObjectTag(this));
        super.onNextPageRequestResult(view, result);
    }

    @Override
    public void setAbort() {
        super.setAbort();
        mDefaultRequestHolder.clear();
    }

    private static class DeepDiffDataObject extends DataObject<MSIMConversation> implements DeepDiff {

        public DeepDiffDataObject(@NonNull MSIMConversation object) {
            super(object);
        }

        @Override
        public boolean isSameItem(@Nullable Object other) {
            if (other instanceof DeepDiffDataObject) {
                final DeepDiffDataObject otherDataObject = (DeepDiffDataObject) other;
                return this.object.getConversationId() == otherDataObject.object.getConversationId();
            }
            return false;
        }

        @Override
        public boolean isSameContent(@Nullable Object other) {
            if (other instanceof DeepDiffDataObject) {
                final DeepDiffDataObject otherDataObject = (DeepDiffDataObject) other;
                return this.object.getConversationId() == otherDataObject.object.getConversationId();
            }
            return false;
        }
    }

}
