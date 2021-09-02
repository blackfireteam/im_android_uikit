package com.masonsoft.imsdk.uikit.app.conversation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMConversationListener;
import com.masonsoft.imsdk.MSIMConversationListenerProxy;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.idonans.core.thread.TaskQueue;
import io.github.idonans.core.util.Preconditions;
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
    private long mFirstConversationSeq = -1;
    private long mLastConversationSeq = -1;

    private final DisposableHolder mDefaultRequestHolder = new DisposableHolder();

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

            @Override
            public void onConversationCreated(long sessionUserId, long conversationId, int conversationType, long targetUserId) {
                addOrUpdateConversation(sessionUserId, conversationId);
            }
        }) {
            @Nullable
            @Override
            protected Object getOnConversationCreatedTag(long sessionUserId, long conversationId, int conversationType, long targetUserId) {
                // merge created, changed callback
                return super.getOnConversationChangedTag(sessionUserId, conversationId, conversationType, targetUserId);
            }
        };
        MSIMManager.getInstance().getConversationManager().addConversationListener(mConversationListener);
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

    @NonNull
    private final Object mAddOrUpdateConversationMergeLock = new Object();
    @NonNull
    private Set<Pair<Long, Long>> mAddOrUpdateConversationDataSet = new HashSet<>();
    private final TaskQueue mAddOrUpdateConversationActionQueue = new TaskQueue(1);

    private void addOrUpdateConversation(long sessionUserId, long conversationId) {
        if (isAbort(sessionUserId)) {
            return;
        }

        synchronized (mAddOrUpdateConversationMergeLock) {
            mAddOrUpdateConversationDataSet.add(Pair.create(sessionUserId, conversationId));
        }

        mAddOrUpdateConversationActionQueue.skipQueue();
        mAddOrUpdateConversationActionQueue.enqueue(() -> {
            final Set<Pair<Long, Long>> dataSet;
            synchronized (mAddOrUpdateConversationMergeLock) {
                dataSet = mAddOrUpdateConversationDataSet;
                mAddOrUpdateConversationDataSet = new HashSet<>();
            }

            final long bestSessionUserId = getSessionUserId();
            final List<MSIMConversation> updateList = new ArrayList<>();
            for (Pair<Long, Long> dataPair : dataSet) {
                Preconditions.checkNotNull(dataPair.first);
                Preconditions.checkNotNull(dataPair.second);
                if (bestSessionUserId != dataPair.first) {
                    continue;
                }
                final MSIMConversation conversation = MSIMManager.getInstance().getConversationManager().getConversation(dataPair.first, dataPair.second);
                if (conversation != null) {
                    updateList.add(conversation);
                }
            }
            if (!updateList.isEmpty()) {
                Collections.sort(updateList, (o1Object, o2Object) -> {
                    final long o1ObjectSeq = o1Object.getSeq();
                    final long o2ObjectSeq = o2Object.getSeq();
                    final long diff = o1ObjectSeq - o2ObjectSeq;
                    return diff == 0 ? 0 : (diff < 0 ? 1 : -1);
                });

                if (isAbort(bestSessionUserId)) {
                    return;
                }
                addOrUpdateConversation(updateList);
            }
        });
    }

    @WorkerThread
    private void addOrUpdateConversation(@Nullable List<MSIMConversation> updateList) {
        if (updateList == null || updateList.isEmpty()) {
            return;
        }

        final List<UnionTypeItemObject> unionTypeItemObjectList = new ArrayList<>();
        for (MSIMConversation conversation : updateList) {
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
        view.mergeSortedConversationList(unionTypeItemObjectList);
        timeDiffDebugHelper.mark();
        timeDiffDebugHelper.print("mergeSortedConversationList unionTypeItemObjectList size:" + unionTypeItemObjectList.size());
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

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createInitRequest() throws Exception {
        MSIMUikitLog.v(Objects.defaultObjectTag(this) + " createInitRequest");
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " createInitRequest sessionUserId:%s, mConversationType:%s, pageSize:%s",
                    getSessionUserId(),
                    mConversationType,
                    mPageSize);
        }

        return Single.just("")
                .map(input -> MSIMManager.getInstance().getConversationManager().pageQueryConversation(
                        getSessionUserId(),
                        0,
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
        MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onInitRequestResult");
        // 记录上一页，下一页参数
        if (result.items == null || result.items.isEmpty()) {
            mFirstConversationSeq = -1;
            mLastConversationSeq = -1;
            setNextPageRequestEnable(false);
        } else {
            mFirstConversationSeq = ((MSIMConversation) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(0)).itemObject).object).getSeq();
            mLastConversationSeq = ((MSIMConversation) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(result.items.size() - 1)).itemObject).object).getSeq();
            setNextPageRequestEnable(true);
        }

        super.onInitRequestResult(view, result);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createNextPageRequest() throws Exception {
        MSIMUikitLog.v(Objects.defaultObjectTag(this) + " createNextPageRequest");
        if (DEBUG) {
            MSIMUikitLog.v(Objects.defaultObjectTag(this) + " createNextPageRequest sessionUserId:%s, mConversationType:%s, pageSize:%s, mLastConversationSeq:%s",
                    getSessionUserId(),
                    mConversationType,
                    mPageSize,
                    mLastConversationSeq);
        }

        if (mLastConversationSeq <= 0) {
            MSIMUikitLog.e(Objects.defaultObjectTag(this) + " createNextPageRequest invalid mLastConversationSeq:%s", mLastConversationSeq);
            return null;
        }

        return Single.just("")
                .map(input -> MSIMManager.getInstance().getConversationManager().pageQueryConversation(
                        getSessionUserId(),
                        mLastConversationSeq,
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
        MSIMUikitLog.v(Objects.defaultObjectTag(this) + " onNextPageRequestResult");
        // 记录上一页，下一页参数
        if (result.items != null && !result.items.isEmpty()) {
            mLastConversationSeq = ((MSIMConversation) ((DataObject) ((UnionTypeItemObject) ((List) result.items).get(result.items.size() - 1)).itemObject).object).getSeq();
        }

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
