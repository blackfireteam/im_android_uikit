package com.masonsoft.imsdk.uikit.widget;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomMessage;
import com.masonsoft.imsdk.MSIMChatRoomMessageListener;
import com.masonsoft.imsdk.MSIMChatRoomMessageListenerProxy;
import com.masonsoft.imsdk.util.Objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import io.github.idonans.core.thread.BatchQueue;
import io.github.idonans.core.thread.Threads;
import io.github.idonans.lang.DisposableHolder;

public abstract class MSIMChatRoomMessageChangedViewHelper {

    private final DisposableHolder mRequestHolder = new DisposableHolder();

    private MSIMChatRoomContext mChatRoomContext;

    public MSIMChatRoomMessageChangedViewHelper() {
    }

    public void setChatRoomContext(MSIMChatRoomContext chatRoomContext) {
        if (mChatRoomContext != chatRoomContext) {
            mChatRoomContext = chatRoomContext;
            if (mChatRoomContext != null) {
                mChatRoomContext.getChatRoomManager().addChatRoomMessageListener(mChatRoomMessageListener);
            }
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

    protected abstract void onChatRoomMessageChanged(@Nonnull MSIMChatRoomContext chatRoomContext, @Nonnull List<MSIMChatRoomMessage> messageList);

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMChatRoomMessageListener mChatRoomMessageListener = new MSIMChatRoomMessageListenerProxy(new MSIMChatRoomMessageListener() {

        private final BatchQueue<MSIMChatRoomMessage> mBatchQueue = new BatchQueue<>();

        {
            mBatchQueue.setConsumer(msimChatRoomMessages -> {
                if (msimChatRoomMessages != null) {
                    final MSIMChatRoomContext chatRoomContext = mChatRoomContext;
                    if (chatRoomContext != null) {
                        final List<MSIMChatRoomMessage> messageList = new ArrayList<>();
                        for (MSIMChatRoomMessage message : msimChatRoomMessages) {
                            if (message == null) {
                                continue;
                            }
                            if (message.getChatRoomId() != chatRoomContext.getChatRoomId()) {
                                continue;
                            }
                            messageList.add(message);
                        }
                        Threads.postUi(() -> {
                            if (chatRoomContext == mChatRoomContext) {
                                onChatRoomMessageChanged(chatRoomContext, messageList);
                            }
                        });
                    }
                }
            });
        }

        private boolean notMatch(MSIMChatRoomMessage msimChatRoomMessage) {
            final long chatRoomId = getChatRoomId();
            if (msimChatRoomMessage == null) {
                return true;
            }
            return msimChatRoomMessage.getChatRoomId() != chatRoomId;
        }

        @Override
        public void onMessageChanged(MSIMChatRoomMessage msimChatRoomMessage) {
            onMessageChangedInternal(msimChatRoomMessage);
        }

        private void onMessageChangedInternal(MSIMChatRoomMessage msimChatRoomMessage) {
            if (notMatch(msimChatRoomMessage)) {
                return;
            }
            mBatchQueue.add(msimChatRoomMessage);
        }
    });

}
