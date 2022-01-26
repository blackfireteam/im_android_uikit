package com.masonsoft.imsdk.uikit.app.chatroom.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMSessionListener;
import com.masonsoft.imsdk.MSIMSessionListenerProxy;
import com.masonsoft.imsdk.core.IMLog;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.widget.MSIMChatRoomStateChangedViewHelper;

import io.github.idonans.core.thread.BatchQueue;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.dynamic.DynamicPresenter;

public class ChatRoomSettingsFragmentPresenter extends DynamicPresenter<ChatRoomSettingsFragment.ViewImpl> {

    private static final boolean DEBUG = true;

    private long mSessionUserId;
    @SuppressWarnings("FieldCanBeLocal")
    private long mChatRoomId;

    @Nullable
    private GlobalChatRoomManager.StaticChatRoomContext mChatRoomContext;
    private final MSIMChatRoomStateChangedViewHelper mChatRoomStateChangedViewHelper = new MSIMChatRoomStateChangedViewHelper() {
        @Override
        protected void onChatRoomStateChanged(@Nullable MSIMChatRoomContext chatRoomContext, @Nullable Object customObject) {
            MSIMChatRoomContext currentChatRoomContext = null;
            if (mChatRoomContext != null) {
                currentChatRoomContext = mChatRoomContext.getChatRoomContext();
            }

            if (currentChatRoomContext == chatRoomContext) {
                notifyChatRoomStateChanged();
            }
        }
    };
    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMSessionListener mSessionListener = new MSIMSessionListenerProxy(new MSIMSessionListener() {
        @Override
        public void onSessionChanged() {
        }

        @Override
        public void onSessionUserIdChanged() {
            init();
        }
    }, true);
    private final GlobalChatRoomManager.StaticChatRoomContext.OnStaticChatRoomContextChangedListener mOnStaticChatRoomContextChangedListener = new GlobalChatRoomManager.StaticChatRoomContext.OnStaticChatRoomContextChangedListener() {
        @Override
        public void onStaticChatRoomContextChanged(@NonNull GlobalChatRoomManager.StaticChatRoomContext context) {
            if (mChatRoomContext == context) {
                notifyChatRoomStateChanged();
            }
        }
    };

    private final BatchQueue<Object> mChatRoomStateChangedBatchQueue = new BatchQueue<>(false);

    @UiThread
    public ChatRoomSettingsFragmentPresenter(@NonNull ChatRoomSettingsFragment.ViewImpl view) {
        super(view);
        MSIMManager.getInstance().addSessionListener(mSessionListener);
        mChatRoomStateChangedBatchQueue.setConsumer(objects -> ChatRoomSettingsFragmentPresenter.this.onChatRoomStateChangedInternal());

        init();
    }

    private void init() {
        if (reInit()) {
            notifyChatRoomStateChanged();
        }
    }

    private boolean reInit() {
        final ChatRoomSettingsFragment.ViewImpl view = getView();
        if (view == null) {
            IMLog.v("abort reInit view is null");
            return false;
        }
        mChatRoomId = view.getChatRoomId();
        if (mChatRoomId <= 0) {
            IMLog.v("abort reInit chat room id is invalid:%s", mChatRoomId);
            return false;
        }
        mSessionUserId = MSIMManager.getInstance().getSessionUserId();
        if (mSessionUserId <= 0) {
            IMLog.v("abort reInit session user id is invalid:%s", mSessionUserId);
            return false;
        }
        mChatRoomContext = GlobalChatRoomManager.getInstance().getStaticChatRoomContext(mChatRoomId);
        if (mChatRoomContext == null) {
            IMLog.v("abort reInit chat room context is null");
            return false;
        }
        mChatRoomStateChangedViewHelper.setChatRoomContext(mChatRoomContext.getChatRoomContext());
        mChatRoomContext.addOnStaticChatRoomContextChangedListener(mOnStaticChatRoomContextChangedListener);
        return true;
    }

    private void notifyChatRoomStateChanged() {
        mChatRoomStateChangedBatchQueue.add(Boolean.TRUE);
    }

    @WorkerThread
    private void onChatRoomStateChangedInternal() {
        if (mChatRoomContext == null) {
            return;
        }

        Threads.postUi(() -> {
            final ChatRoomSettingsFragment.ViewImpl view = getView();
            if (view == null) {
                return;
            }
            if (mChatRoomContext == null) {
                return;
            }

            view.onChatRoomStateChanged(mChatRoomContext);
        });
    }

    public long getChatRoomId() {
        return mChatRoomId;
    }

    @Nullable
    public GlobalChatRoomManager.StaticChatRoomContext getChatRoomContext() {
        return mChatRoomContext;
    }

}
