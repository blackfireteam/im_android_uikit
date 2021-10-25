package com.masonsoft.imsdk.uikit.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMConversationListener;
import com.masonsoft.imsdk.MSIMConversationListenerProxy;
import com.masonsoft.imsdk.MSIMConversationPageContext;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.DisposableHolder;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class MSIMConversationChangedViewHelper {

    private final DisposableHolder mRequestHolder = new DisposableHolder();

    // 是否使用 mSessionUserId + mConversationId 的形式来确认一个 conversation
    // 否则使用 mSessionUserId + mConversationType + mTargetUserId 的形式来确认一个 conversation
    private boolean mByConversationId = true;

    private long mSessionUserId = Long.MIN_VALUE / 2;
    private long mConversationId = Long.MIN_VALUE / 2;
    private int mConversationType = Integer.MIN_VALUE / 2;
    private long mTargetUserId = Long.MIN_VALUE / 2;

    public MSIMConversationChangedViewHelper(@NonNull MSIMConversationPageContext pageContext) {
        MSIMManager.getInstance().getConversationManager().addConversationListener(pageContext, mConversationListener);
    }

    public void setConversation(long sessionUserId, long conversationId) {
        if (mSessionUserId != sessionUserId
                || mConversationId != conversationId
                || !mByConversationId) {
            mByConversationId = true;
            mSessionUserId = sessionUserId;
            mConversationId = conversationId;
            requestLoadData(true);
        }
    }

    public void setConversationByTargetUserId(long sessionUserId, int conversationType, long targetUserId) {
        if (mSessionUserId != sessionUserId
                || mConversationType != conversationType
                || mTargetUserId != targetUserId
                || mByConversationId) {
            mByConversationId = false;
            mSessionUserId = sessionUserId;
            mConversationType = conversationType;
            mTargetUserId = targetUserId;
            requestLoadData(true);
        }
    }

    public String getDebugString() {
        //noinspection StringBufferReplaceableByString
        final StringBuilder builder = new StringBuilder();
        builder.append(Objects.defaultObjectTag(this));
        builder.append(" sessionUserId:").append(this.mSessionUserId);
        builder.append(" byConversationId:").append(this.mByConversationId);
        builder.append(" conversationId:").append(this.mConversationId);
        builder.append(" conversationType:").append(this.mConversationType);
        builder.append(" targetUserId:").append(this.mTargetUserId);
        return builder.toString();
    }

    public long getSessionUserId() {
        return mSessionUserId;
    }

    public long getConversationId() {
        return mConversationId;
    }

    public boolean isByConversationId() {
        return mByConversationId;
    }

    public int getConversationType() {
        return mConversationType;
    }

    public long getTargetUserId() {
        return mTargetUserId;
    }

    @UiThread
    public void requestLoadData(boolean reset) {
        MSIMUikitLog.v("[%s][requestLoadData][onUi] ============= onConversationChangedInternal sessionUserId:%s, conversationId:%s",
                Objects.defaultObjectTag(MSIMConversationChangedViewHelper.this),
                mSessionUserId,
                mConversationId
        );

        Preconditions.checkArgument(Threads.isUi());

        // abort last
        mRequestHolder.set(null);

        if (reset) {
            onConversationChanged(null, null);
        }
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    MSIMUikitLog.v("[%s][requestLoadData][onIO] ============= onConversationChangedInternal sessionUserId:%s, conversationId:%s",
                            Objects.defaultObjectTag(MSIMConversationChangedViewHelper.this),
                            mSessionUserId,
                            mConversationId
                    );

                    final MSIMConversation conversation;
                    if (mByConversationId) {
                        conversation = MSIMManager.getInstance().getConversationManager().getConversation(
                                mSessionUserId,
                                mConversationId
                        );
                    } else {
                        conversation = MSIMManager.getInstance().getConversationManager().getConversationByTargetUserId(
                                mSessionUserId,
                                mConversationType,
                                mTargetUserId
                        );
                    }
                    return new ObjectWrapper(conversation);
                })
                .map(input -> {
                    final Object customObject = loadCustomObject();
                    return Pair.create(input, customObject);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Preconditions.checkNotNull(pair.first);
                    onConversationChanged((MSIMConversation) pair.first.getObject(), pair.second);
                }, MSIMUikitLog::e));
    }

    @Nullable
    @WorkerThread
    protected Object loadCustomObject() {
        return null;
    }

    protected abstract void onConversationChanged(@Nullable MSIMConversation conversation, @Nullable Object customObject);

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMConversationListener mConversationListener = new MSIMConversationListenerProxy(new MSIMConversationListener() {
        private boolean notMatch(long sessionUserId, long conversationId, int conversationType, long targetUserId) {
            if (mByConversationId) {
                return !MSIMConstants.isIdMatch(mSessionUserId, sessionUserId)
                        || !MSIMConstants.isIdMatch(mConversationId, conversationId);
            } else {
                return !MSIMConstants.isIdMatch(mSessionUserId, sessionUserId)
                        || !MSIMConstants.isIdMatch(mConversationType, conversationType)
                        || !MSIMConstants.isIdMatch(mTargetUserId, targetUserId);
            }
        }

        @Override
        public void onConversationChanged(long sessionUserId, long conversationId, int conversationType, long targetUserId) {
            onConversationChangedInternal(sessionUserId, conversationId, conversationType, targetUserId);
        }

        private void onConversationChangedInternal(long sessionUserId, long conversationId, int conversationType, long targetUserId) {
            if (notMatch(sessionUserId, conversationId, conversationType, targetUserId)) {
                return;
            }

            MSIMUikitLog.v("[%s] ============= onConversationChangedInternal sessionUserId:%s, conversationId:%s",
                    Objects.defaultObjectTag(MSIMConversationChangedViewHelper.this),
                    sessionUserId,
                    conversationId
            );

            requestLoadData(false);
        }
    }, true);

}
