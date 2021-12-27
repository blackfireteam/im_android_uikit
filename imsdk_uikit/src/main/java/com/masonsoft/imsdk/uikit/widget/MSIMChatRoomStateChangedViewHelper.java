package com.masonsoft.imsdk.uikit.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomManager;
import com.masonsoft.imsdk.MSIMChatRoomStateListener;
import com.masonsoft.imsdk.MSIMChatRoomStateListenerProxy;
import com.masonsoft.imsdk.lang.ObjectWrapper;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.core.thread.Threads;
import io.github.idonans.core.util.Preconditions;
import io.github.idonans.lang.DisposableHolder;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class MSIMChatRoomStateChangedViewHelper {

    private final DisposableHolder mRequestHolder = new DisposableHolder();

    private MSIMChatRoomContext mChatRoomContext;

    public MSIMChatRoomStateChangedViewHelper(@NonNull MSIMChatRoomContext chatRoomContext) {
        chatRoomContext.getChatRoomManager().addChatRoomStateListener(mChatRoomStateListener);
    }

    public void setChatRoomContext(MSIMChatRoomContext chatRoomContext) {
        if (mChatRoomContext != chatRoomContext) {
            mChatRoomContext = chatRoomContext;
            if (mChatRoomContext != null) {
                mChatRoomContext.getChatRoomManager().addChatRoomStateListener(mChatRoomStateListener);
            }

            requestLoadData(true);
        }
    }

    @Nullable
    public MSIMChatRoomContext getChatRoomContext() {
        return mChatRoomContext;
    }

    public long getChatRoomId() {
        if (mChatRoomContext != null) {
            return mChatRoomContext.getChatRoomId();
        }
        return 0L;
    }

    public String getDebugString() {
        //noinspection StringBufferReplaceableByString
        final StringBuilder builder = new StringBuilder();
        builder.append(Objects.defaultObjectTag(this));
        builder.append(" chatRoomId:").append(this.getChatRoomId());
        return builder.toString();
    }

    @UiThread
    public void requestLoadData(boolean reset) {
        Preconditions.checkArgument(Threads.isUi());

        // abort last
        mRequestHolder.set(null);

        if (reset) {
            onChatRoomStateChanged(getChatRoomContext(), null);
        }
        mRequestHolder.set(Single.just("")
                .map(input -> {
                    return new ObjectWrapper(getChatRoomContext());
                })
                .map(input -> {
                    final Object customObject = loadCustomObject();
                    return Pair.create(input, customObject);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Preconditions.checkNotNull(pair.first);
                    onChatRoomStateChanged((MSIMChatRoomContext) pair.first.getObject(), pair.second);
                }, MSIMUikitLog::e));
    }

    @Nullable
    @WorkerThread
    protected Object loadCustomObject() {
        return null;
    }

    protected abstract void onChatRoomStateChanged(@Nullable MSIMChatRoomContext chatRoomContext, @Nullable Object customObject);

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMChatRoomStateListener mChatRoomStateListener = new MSIMChatRoomStateListenerProxy(new MSIMChatRoomStateListener() {
        private boolean notMatch(MSIMChatRoomManager msimChatRoomManager) {
            final long chatRoomId = getChatRoomId();
            if (msimChatRoomManager == null) {
                return true;
            }
            return msimChatRoomManager.getChatRoomContext().getChatRoomId() != chatRoomId;
        }

        @Override
        public void onChatRoomStateChanged(MSIMChatRoomManager msimChatRoomManager) {
            onChatRoomStateChangedInternal(msimChatRoomManager);
        }

        private void onChatRoomStateChangedInternal(MSIMChatRoomManager msimChatRoomManager) {
            if (notMatch(msimChatRoomManager)) {
                return;
            }
            Threads.postUi(() -> {
                if (notMatch(msimChatRoomManager)) {
                    return;
                }
                requestLoadData(false);
            });
        }
    });

}
