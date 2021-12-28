package com.masonsoft.imsdk.uikit.app.chatroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomMessage;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMSessionListener;
import com.masonsoft.imsdk.MSIMSessionListenerProxy;
import com.masonsoft.imsdk.core.IMLog;
import com.masonsoft.imsdk.lang.GeneralResult;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageViewHolder;
import com.masonsoft.imsdk.uikit.widget.MSIMChatRoomStateChangedViewHelper;

import io.github.idonans.core.thread.BatchQueue;
import io.github.idonans.dynamic.DynamicResult;
import io.github.idonans.dynamic.single.SinglePresenter;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.reactivex.rxjava3.core.SingleSource;

public class ChatRoomFragmentPresenter extends SinglePresenter<UnionTypeItemObject, GeneralResult, ChatRoomFragment.ViewImpl> {

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

    private final BatchQueue<Object> mChatRoomStateChangedBatchQueue = new BatchQueue<>(true);

    @UiThread
    public ChatRoomFragmentPresenter(@NonNull ChatRoomFragment.ViewImpl view) {
        super(view);
        MSIMManager.getInstance().addSessionListener(mSessionListener);
        mChatRoomStateChangedBatchQueue.setConsumer(objects -> ChatRoomFragmentPresenter.this.onChatRoomStateChangedInternal());

        init();
    }

    private void init() {
        if (!reInit()) {
            notifyChatRoomStateChanged();
        }
    }

    private boolean reInit() {
        final ChatRoomFragment.ViewImpl view = getView();
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

    private void onChatRoomStateChangedInternal() {
        final ChatRoomFragment.ViewImpl view = getView();
        if (view != null) {
            view.onChatRoomStateChanged(mChatRoomContext);
        }
    }

    @Nullable
    public ChatRoomFragment.ViewImpl getView() {
        return (ChatRoomFragment.ViewImpl) super.getView();
    }

    public long getChatRoomId() {
        return mChatRoomId;
    }

    @Nullable
    public GlobalChatRoomManager.StaticChatRoomContext getChatRoomContext() {
        return mChatRoomContext;
    }

    private final UnionTypeViewHolderListeners.OnItemClickListener mOnHolderItemClickListener = viewHolder -> {
        ChatRoomFragment.ViewImpl view = getView();
        if (view != null) {
            IMBaseMessageViewHolder.Helper.showPreview(viewHolder);
        }
    };

    private final UnionTypeViewHolderListeners.OnItemLongClickListener mOnHolderItemLongClickListener = viewHolder -> {
        ChatRoomFragment.ViewImpl view = getView();
        if (view != null) {
            IMBaseMessageViewHolder.Helper.showMenu(viewHolder);
        }
    };

    @Nullable
    public UnionTypeItemObject createDefault(@Nullable MSIMChatRoomMessage message) {
        if (message == null) {
            return null;
        }
        final DataObject dataObject = new DataObject(message)
                .putExtHolderItemClick1(mOnHolderItemClickListener)
                .putExtHolderItemLongClick1(mOnHolderItemLongClickListener);
        return IMBaseMessageViewHolder.Helper.createDefault(dataObject);
    }

    @Nullable
    @Override
    protected SingleSource<DynamicResult<UnionTypeItemObject, GeneralResult>> createInitRequest() throws Exception {
        return null;
    }

}
