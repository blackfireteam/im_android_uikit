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

                MSIMConversationLoader.this.onConversationChangedInternal(conversation, false);
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

    public void setConversation(@Nullable MSIMConversation conversation) {
        setConversationInternal(conversation);
    }

    private void setConversationInternal(@Nullable MSIMConversation conversation) {
        mConversation = conversation;
        onConversationLoad(mConversation);

        requestLoadData();
    }

    private void onConversationChangedInternal(@Nullable MSIMConversation conversation, boolean acceptNull) {
        if (!acceptNull && conversation == null) {
            return;
        }

        if (conversation != null && mConversation != null && !conversation.equals(mConversation)) {
            return;
        }

        mConversation = conversation;
        onConversationLoad(mConversation);
    }

    protected void onConversationLoad(@Nullable MSIMConversation conversation) {
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

        return MSIMManager.getInstance().getConversationManager().getConversation(
                conversation.getSessionUserId(),
                conversation.getConversationId()
        );
    }

    @Override
    protected void onDataLoad(@Nullable MSIMConversation conversation) {
        onConversationChangedInternal(conversation, true);
    }

}
