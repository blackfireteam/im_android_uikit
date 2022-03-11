package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMConversation;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

import io.github.idonans.core.util.IOUtil;

public abstract class MSIMConversationLoader extends DataLoaderImpl<MSIMConversation> implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private final MSIMConversationChangedHelper mHelper;

    @Nullable
    protected MSIMConversation mConversation;

    public MSIMConversationLoader() {
        mHelper = new MSIMConversationChangedHelper() {
            @Override
            protected void onConversationChanged(@NonNull MSIMConversation conversation) {
                super.onConversationChanged(conversation);

                MSIMConversationLoader.this.onConversationChangedInternal(conversation);
            }
        };
    }

    @Override
    public void close() throws IOException {
        super.close();
        IOUtil.closeQuietly(mHelper);
    }

    @Nullable
    public MSIMConversation getConversation() {
        return mConversation;
    }

    public void setConversation(@NonNull MSIMConversation conversation, boolean forceReplace) {
        setConversationInternal(conversation, forceReplace);
    }

    private void setConversationInternal(@NonNull MSIMConversation conversation, boolean forceReplace) {
        final MSIMConversation currentConversation = mConversation;
        if (!forceReplace) {
            if (currentConversation != null) {
                if (match(currentConversation, conversation)) {
                    // 继续使用当前 conversation
                    conversation = currentConversation;
                }
            }
        }

        mConversation = conversation;
        onConversationLoad(conversation);

        requestLoadData();
    }

    private void onConversationChangedInternal(@NonNull MSIMConversation conversation) {
        final MSIMConversation currentConversation = mConversation;
        if (currentConversation == null) {
            return;
        }

        if (match(currentConversation, conversation)) {
            mConversation = conversation;
            onConversationLoad(conversation);
        }
    }

    private boolean match(@NonNull MSIMConversation obj1, @NonNull MSIMConversation obj2) {
        final long sessionUserId = obj1.getSessionUserId();
        final long conversationId = obj1.getConversationId();
        final int conversationType = obj1.getConversationType();
        final long targetUserId = obj1.getTargetUserId();

        if (sessionUserId > 0 && sessionUserId == obj2.getSessionUserId()) {
            if (conversationId > 0) {
                return conversationId == obj2.getConversationId();
            }

            return conversationType == obj2.getConversationType()
                    && targetUserId > 0
                    && targetUserId == obj2.getTargetUserId();
        }

        return false;
    }

    protected void onConversationLoad(@NonNull MSIMConversation conversation) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onConversationLoad %s", Objects.defaultObjectTag(this), conversation);
        }
    }

    @Nullable
    @Override
    protected MSIMConversation loadData() {
        final MSIMConversation conversation = mConversation;
        if (conversation == null) {
            return null;
        }

        final long sessionUserId = conversation.getSessionUserId();
        final long conversationId = conversation.getConversationId();
        final int conversationType = conversation.getConversationType();
        final long targetUserId = conversation.getTargetUserId();

        if (sessionUserId > 0) {
            if (conversationId > 0) {
                return MSIMManager.getInstance().getConversationManager().getConversation(
                        sessionUserId,
                        conversationId
                );
            }

            if (targetUserId > 0) {
                return MSIMManager.getInstance().getConversationManager().getConversationByTargetUserId(
                        sessionUserId,
                        conversationType,
                        targetUserId
                );
            }
        }

        return null;
    }

    @Override
    protected void onDataLoad(@Nullable MSIMConversation conversation) {
        if (conversation != null) {
            onConversationChangedInternal(conversation);
        }
    }

}
