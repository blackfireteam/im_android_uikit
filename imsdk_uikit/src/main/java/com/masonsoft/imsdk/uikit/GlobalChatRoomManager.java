package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomMessage;
import com.masonsoft.imsdk.MSIMChatRoomMessageListener;
import com.masonsoft.imsdk.MSIMChatRoomMessageListenerProxy;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMSessionListener;
import com.masonsoft.imsdk.MSIMSessionListenerAdapter;
import com.masonsoft.imsdk.core.RuntimeMode;
import com.masonsoft.imsdk.util.WeakObservable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import io.github.idonans.core.Singleton;
import io.github.idonans.core.manager.StorageManager;
import io.github.idonans.core.thread.BatchQueue;
import io.github.idonans.core.thread.TaskQueue;
import io.github.idonans.core.util.Preconditions;

public class GlobalChatRoomManager {

    private static final Singleton<GlobalChatRoomManager> INSTANCE = new Singleton<GlobalChatRoomManager>() {
        @Override
        protected GlobalChatRoomManager create() {
            return new GlobalChatRoomManager();
        }
    };

    public static GlobalChatRoomManager getInstance() {
        return INSTANCE.get();
    }

    public static final long DEFAULT_CHAT_ROOM_ID = 25L;
    private final Map<String, StaticChatRoomContext> mStaticChatRoomContextMap = new HashMap<>();
    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMSessionListener mSessionListener = new MSIMSessionListenerAdapter() {
        @Override
        public void onSessionUserIdChanged() {
            GlobalChatRoomManager.this.onSessionUserIdChanged();
        }
    };

    private GlobalChatRoomManager() {
        MSIMManager.getInstance().addSessionListener(mSessionListener);
        onSessionUserIdChanged();
    }

    public void start() {
    }

    private void onSessionUserIdChanged() {
        // 登录成功后自动加入聊天室
        final long sessionUserId = MSIMManager.getInstance().getSessionUserId();
        if (sessionUserId > 0) {
            final StaticChatRoomContext context = getStaticChatRoomContext();
            Preconditions.checkNotNull(context);
        }
    }

    @Nullable
    public StaticChatRoomContext getStaticChatRoomContext() {
        return getStaticChatRoomContext(DEFAULT_CHAT_ROOM_ID);
    }

    @Nullable
    public StaticChatRoomContext getStaticChatRoomContext(long chatRoomId) {
        return getStaticChatRoomContext(MSIMManager.getInstance().getSessionUserId(), chatRoomId, true);
    }

    @Nullable
    public StaticChatRoomContext getStaticChatRoomContext(long sessionUserId, long chatRoomId, boolean autoCreate) {
        synchronized (mStaticChatRoomContextMap) {
            if (sessionUserId > 0) {
                final String key = sessionUserId + "_" + chatRoomId;
                StaticChatRoomContext context = mStaticChatRoomContextMap.get(key);
                if (context == null && autoCreate) {
                    context = new StaticChatRoomContext(sessionUserId, chatRoomId);
                    mStaticChatRoomContextMap.put(key, context);
                }
                return context;
            }
        }
        return null;
    }

    public void removeStaticChatRoomContext(long sessionUserId) {
        this.removeStaticChatRoomContext(sessionUserId, DEFAULT_CHAT_ROOM_ID);
    }

    public void removeStaticChatRoomContext(long sessionUserId, long chatRoomId) {
        synchronized (mStaticChatRoomContextMap) {
            final StaticChatRoomContext context = getStaticChatRoomContext(sessionUserId, chatRoomId, false);
            if (context != null) {
                final String key = sessionUserId + "_" + chatRoomId;
                mStaticChatRoomContextMap.remove(key);
            }
        }
    }

    public static class StaticChatRoomContext {

        private final long mSessionUserId;
        private final long mChatRoomId;
        private final MSIMChatRoomContext mChatRoomContext;
        private final WeakObservable<OnStaticChatRoomContextChangedListener> mListeners = new WeakObservable<>();
        private final MSIMChatRoomMessageCache mMessageCache = new MSIMChatRoomMessageCache();
        private final BatchQueue<Object> mNotifyOnStaticChatRoomContextChangedListenerBatchQueue = new BatchQueue<>();

        public interface OnStaticChatRoomContextChangedListener {
            void onStaticChatRoomContextChanged(@NonNull StaticChatRoomContext context);
        }

        public StaticChatRoomContext(long sessionUserId, long chatRoomId) {
            mSessionUserId = sessionUserId;
            mChatRoomId = chatRoomId;
            mChatRoomContext = MSIMManager.getInstance().getOrCreateChatRoomContext(mChatRoomId);
            mChatRoomContext.getChatRoomManager().addChatRoomMessageListener(mChatRoomMessageListener);

            mNotifyOnStaticChatRoomContextChangedListenerBatchQueue.setConsumer(new Consumer<List<Object>>() {
                @Override
                public void accept(List<Object> objects) {
                    if (mListeners != null) {
                        mListeners.forEach(listener -> {
                            if (listener != null) {
                                listener.onStaticChatRoomContextChanged(StaticChatRoomContext.this);
                            }
                        });
                    }
                }
            });

            mChatRoomContext.getChatRoomManager().joinChatRoom();
        }

        public long getSessionUserId() {
            return mSessionUserId;
        }

        public long getChatRoomId() {
            return mChatRoomId;
        }

        @Nonnull
        public MSIMChatRoomContext getChatRoomContext() {
            return mChatRoomContext;
        }

        public void addOnStaticChatRoomContextChangedListener(OnStaticChatRoomContextChangedListener listener) {
            if (listener != null) {
                mListeners.registerObserver(listener);
            }
        }

        public void removeOnStaticChatRoomContextChangedListener(OnStaticChatRoomContextChangedListener listener) {
            if (listener != null) {
                mListeners.unregisterObserver(listener);
            }
        }

        @Nonnull
        public List<MSIMChatRoomMessage> getMessageList() {
            return mMessageCache.getMessageList();
        }

        public void markAsRead() {
            mMessageCache.markAsRead();
        }

        public int getUnreadCount() {
            return mMessageCache.getUnreadCount();
        }

        public long getLastReadServerMessageId() {
            return mMessageCache.getLastReadServerMessageId();
        }

        @SuppressWarnings("FieldCanBeLocal")
        private final MSIMChatRoomMessageListener mChatRoomMessageListener = new MSIMChatRoomMessageListenerProxy(new MSIMChatRoomMessageListener() {
            @Override
            public void onMessageChanged(MSIMChatRoomMessage msimChatRoomMessage) {
                mMessageCache.appendOrUpdateMessage(msimChatRoomMessage);
            }
        });

        // 缓存最新的 2000 条聊天室消息
        private class MSIMChatRoomMessageCache {

            private final String NAMESPACE = "imsdk_sample_chat_room";
            @SuppressWarnings("FieldCanBeLocal")
            private final int mMaxSize = 2000;
            private final LinkedList<MSIMChatRoomMessage> mMessageList = new LinkedList<>();
            private long mLastReadServerMessageId;
            private long mLastServerMessageIdNotMe;
            private int mUnreadCount;
            private final TaskQueue mSavedQueue = new TaskQueue(1);

            private MSIMChatRoomMessageCache() {
                mLastReadServerMessageId = restoreLastReadServerMessageId();
                mUnreadCount = restoreUnreadCount();
            }

            public void markAsRead() {
                if (mLastReadServerMessageId != mLastServerMessageIdNotMe
                        || mUnreadCount != 0) {
                    mLastReadServerMessageId = mLastServerMessageIdNotMe;
                    mUnreadCount = 0;
                    saveAsync();
                }
            }

            private void saveAsync() {
                mSavedQueue.skipQueue();
                mSavedQueue.enqueue(() -> {
                    saveLastReadServerMessageId();
                    saveUnreadCount();
                });
            }

            private int restoreUnreadCount() {
                final String key = buildUnreadCountKey();
                String value = StorageManager.getInstance().get(NAMESPACE, key);
                if (value != null) {
                    try {
                        value = value.trim();
                        return Integer.parseInt(value);
                    } catch (Throwable e) {
                        MSIMUikitLog.e(e);
                        RuntimeMode.fixme(e);
                    }
                }
                return 0;
            }

            private long restoreLastReadServerMessageId() {
                final String key = buildLastReadServerMessageIdKey();
                String value = StorageManager.getInstance().get(NAMESPACE, key);
                if (value != null) {
                    try {
                        value = value.trim();
                        return Long.parseLong(value);
                    } catch (Throwable e) {
                        MSIMUikitLog.e(e);
                        RuntimeMode.fixme(e);
                    }
                }
                return 0;
            }

            private void saveUnreadCount() {
                final String key = buildUnreadCountKey();
                StorageManager.getInstance().set(NAMESPACE, key, String.valueOf(mUnreadCount));
            }

            private void saveLastReadServerMessageId() {
                final String key = buildLastReadServerMessageIdKey();
                StorageManager.getInstance().set(NAMESPACE, key, String.valueOf(mLastReadServerMessageId));
            }

            private String buildLastReadServerMessageIdKey() {
                return "chat_room_last_read_server_message_id_20211227_xx9o2_" + mSessionUserId + "_" + mChatRoomId;
            }

            private String buildUnreadCountKey() {
                return "chat_room_unread_count_20211227_xx9o2_" + mSessionUserId + "_" + mChatRoomId;
            }

            public synchronized void appendOrUpdateMessage(MSIMChatRoomMessage message) {
                if (message == null) {
                    return;
                }
                for (MSIMChatRoomMessage m : mMessageList) {
                    if (message.equals(m)) {
                        return;
                    }
                }

                mMessageList.add(message);
                if (mMessageList.size() > mMaxSize) {
                    mMessageList.removeFirst();
                }

                if (message.getFromUserId() != mSessionUserId) {
                    final long serverMessageId = message.getServerMessageId();
                    if (serverMessageId > 0 && serverMessageId > mLastReadServerMessageId) {
                        final int messageType = message.getMessageType();
                        if (MSIMConstants.MessageType.isVisibleMessage(messageType)) {
                            // 可见的新消息，累计未读数
                            mUnreadCount++;
                            mLastServerMessageIdNotMe = serverMessageId;
                            saveAsync();
                        }
                    }
                }

                mNotifyOnStaticChatRoomContextChangedListenerBatchQueue.add(Boolean.TRUE);
            }

            @Nonnull
            public synchronized List<MSIMChatRoomMessage> getMessageList() {
                return new ArrayList<>(this.mMessageList);
            }

            public int getUnreadCount() {
                return mUnreadCount;
            }

            public long getLastReadServerMessageId() {
                return mLastReadServerMessageId;
            }
        }

    }

}
