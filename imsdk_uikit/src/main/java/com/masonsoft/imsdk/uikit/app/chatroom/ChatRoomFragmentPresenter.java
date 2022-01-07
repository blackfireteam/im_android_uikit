package com.masonsoft.imsdk.uikit.app.chatroom;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomMessage;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMSessionListener;
import com.masonsoft.imsdk.MSIMSessionListenerProxy;
import com.masonsoft.imsdk.core.IMLog;
import com.masonsoft.imsdk.uikit.GlobalChatRoomManager;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;
import com.masonsoft.imsdk.uikit.uniontype.IMUikitUnionTypeMapper;
import com.masonsoft.imsdk.uikit.uniontype.UnionTypeViewHolderListeners;
import com.masonsoft.imsdk.uikit.uniontype.viewholder.IMBaseMessageViewHolder;
import com.masonsoft.imsdk.uikit.widget.MSIMChatRoomStateChangedViewHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.idonans.core.thread.BatchQueue;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.dynamic.DynamicPresenter;
import io.github.idonans.uniontype.UnionTypeItemObject;
import io.github.idonans.uniontype.UnionTypeMapper;

public class ChatRoomFragmentPresenter extends DynamicPresenter<ChatRoomFragment.ViewImpl> {

    private static final boolean DEBUG = true;

    private long mSessionUserId;
    @SuppressWarnings("FieldCanBeLocal")
    private long mChatRoomId;

    private long mMaxLocalMessageId = 0L;

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
    private final GlobalChatRoomManager.StaticChatRoomContext.OnStaticChatRoomReceivedTipMessageListener mOnStaticChatRoomReceivedTipMessageListener = new GlobalChatRoomManager.StaticChatRoomContext.OnStaticChatRoomReceivedTipMessageListener() {
        @Override
        public void onStaticChatRoomReceivedTipMessage(@NonNull List<CharSequence> tipMessageList) {
            if (!tipMessageList.isEmpty()) {
                onReceivedTipMessageListInternal(tipMessageList);
            }
        }
    };

    private final BatchQueue<Object> mChatRoomStateChangedBatchQueue = new BatchQueue<>(false);

    @UiThread
    public ChatRoomFragmentPresenter(@NonNull ChatRoomFragment.ViewImpl view) {
        super(view);
        MSIMManager.getInstance().addSessionListener(mSessionListener);
        mChatRoomStateChangedBatchQueue.setConsumer(objects -> ChatRoomFragmentPresenter.this.onChatRoomStateChangedInternal());

        init();
    }

    private void init() {
        if (reInit()) {
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
        mChatRoomContext.addOnStaticChatRoomReceivedTipMessageListener(mOnStaticChatRoomReceivedTipMessageListener);
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

        final List<MSIMChatRoomMessage> messageList = mChatRoomContext.getMessageList();
        final List<MSIMChatRoomMessage> visibleMessageList = new ArrayList<>();
        for (MSIMChatRoomMessage message : messageList) {
            if (MSIMConstants.MessageType.isVisibleMessage(message.getMessageType())) {
                visibleMessageList.add(message);
            }
        }
        Collections.sort(visibleMessageList, (o1, o2) -> Long.compare(o1.getMessageId(), o2.getMessageId()));

        // 计算是否有可见的新消息
        final List<MSIMChatRoomMessage> newMessageList = new ArrayList<>();
        for (MSIMChatRoomMessage message : visibleMessageList) {
            if (message.getMessageId() > mMaxLocalMessageId) {
                newMessageList.add(message);
                mMaxLocalMessageId = message.getMessageId();
            }
        }

        Threads.postUi(() -> {
            final ChatRoomFragment.ViewImpl view = getView();
            if (view == null) {
                return;
            }
            if (mChatRoomContext == null) {
                return;
            }

            if (!newMessageList.isEmpty()) {
                view.onAppendMessages(messageList, mChatRoomContext);
            }

            view.onChatRoomStateChanged(mChatRoomContext);
        });
    }

    private void onReceivedTipMessageListInternal(@NonNull List<CharSequence> tipMessageList) {
        Threads.postUi(() -> {
            final ChatRoomFragment.ViewImpl view = getView();
            if (view == null) {
                return;
            }
            if (mChatRoomContext == null) {
                return;
            }
            view.onReceivedTipMessageList(tipMessageList);
        });
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

        final int unionType = IMBaseMessageViewHolder.Helper.getDefaultUnionType(dataObject);
        if (unionType != UnionTypeMapper.UNION_TYPE_NULL) {
            return new UnionTypeItemObject(unionType, dataObject);
        }
        return null;
    }

    @Nullable
    public UnionTypeItemObject createTipMessageDefault(@Nullable CharSequence tipMessage) {
        if (tipMessage == null) {
            return null;
        }
        if (TextUtils.isEmpty(tipMessage)) {
            return null;
        }

        final DataObject dataObject = new DataObject(tipMessage);
        return new UnionTypeItemObject(
                IMUikitUnionTypeMapper.UNION_TYPE_IMPL_IM_MESSAGE_TIP_TEXT,
                dataObject
        );
    }

}
