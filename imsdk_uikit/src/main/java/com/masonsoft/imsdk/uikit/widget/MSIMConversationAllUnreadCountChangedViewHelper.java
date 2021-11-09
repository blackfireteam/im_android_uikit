package com.masonsoft.imsdk.uikit.widget;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.masonsoft.imsdk.MSIMConversationAllUnreadCountListener;
import com.masonsoft.imsdk.MSIMConversationAllUnreadCountListenerProxy;
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

public abstract class MSIMConversationAllUnreadCountChangedViewHelper {

    private final DisposableHolder mRequestHolder = new DisposableHolder();

    private long mSessionUserId = Long.MIN_VALUE / 2;

    public MSIMConversationAllUnreadCountChangedViewHelper() {
        MSIMManager.getInstance().getConversationManager().addConversationAllUnreadCountListener(mConversationAllUnreadCountListener);
    }

    public void setSessionUserId(long sessionUserId) {
        if (mSessionUserId != sessionUserId) {
            mSessionUserId = sessionUserId;
            requestLoadData(true);
        }
    }

    public String getDebugString() {
        //noinspection StringBufferReplaceableByString
        final StringBuilder builder = new StringBuilder();
        builder.append(Objects.defaultObjectTag(this));
        builder.append(" sessionUserId:").append(this.mSessionUserId);
        return builder.toString();
    }

    public long getSessionUserId() {
        return mSessionUserId;
    }

    @UiThread
    public void requestLoadData(boolean reset) {
        MSIMUikitLog.v("[%s][requestLoadData][onUi] ============= sessionUserId:%s",
                Objects.defaultObjectTag(MSIMConversationAllUnreadCountChangedViewHelper.this),
                mSessionUserId
        );

        Preconditions.checkArgument(Threads.isUi());

        // abort last
        mRequestHolder.set(null);

        if (reset) {
            onConversationAllUnreadCountChanged(null, null);
        }
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    MSIMUikitLog.v("[%s][requestLoadData][onIO] ============= sessionUserId:%s",
                            Objects.defaultObjectTag(MSIMConversationAllUnreadCountChangedViewHelper.this),
                            mSessionUserId
                    );

                    final int allUnreadCount = MSIMManager.getInstance().getConversationManager().getAllUnreadCount(mSessionUserId);
                    return new ObjectWrapper(allUnreadCount);
                })
                .map(input -> {
                    final Object customObject = loadCustomObject();
                    return Pair.create(input, customObject);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Preconditions.checkNotNull(pair.first);
                    onConversationAllUnreadCountChanged((Integer) pair.first.getObject(), pair.second);
                }, MSIMUikitLog::e));
    }

    @Nullable
    @WorkerThread
    protected Object loadCustomObject() {
        return null;
    }

    protected abstract void onConversationAllUnreadCountChanged(@Nullable Integer allUnreadCount, @Nullable Object customObject);

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMConversationAllUnreadCountListener mConversationAllUnreadCountListener = new MSIMConversationAllUnreadCountListenerProxy(new MSIMConversationAllUnreadCountListener() {
        @Override
        public void onConversationAllUnreadCountChanged() {
            Threads.postUi(() -> {
                MSIMUikitLog.v("[%s] ============= onConversationAllUnreadCountChanged",
                        Objects.defaultObjectTag(MSIMConversationAllUnreadCountChangedViewHelper.this)
                );

                requestLoadData(false);
            });
        }
    });

}
