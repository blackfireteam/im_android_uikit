package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

import io.github.idonans.core.util.IOUtil;

public abstract class MSIMMessageLoader extends DataLoaderImpl<MSIMMessage> implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    private final MSIMMessageChangedHelper mHelper;

    @Nullable
    protected MSIMMessage mMessage;

    public MSIMMessageLoader() {
        mHelper = new MSIMMessageChangedHelper() {
            @Override
            protected void onMessageChanged(@NonNull MSIMMessage message) {
                super.onMessageChanged(message);

                MSIMMessageLoader.this.onMessageChangedInternal(message);
            }
        };
    }

    @Override
    public void close() throws IOException {
        super.close();
        IOUtil.closeQuietly(mHelper);
    }

    @Nullable
    public MSIMMessage getMessage() {
        return mMessage;
    }

    public void setMessage(@NonNull MSIMMessage message, boolean forceReplace) {
        setMessageInternal(message, forceReplace);
    }

    private void setMessageInternal(@NonNull MSIMMessage message, boolean forceReplace) {
        final MSIMMessage currentMessage = mMessage;
        if (!forceReplace) {
            if (currentMessage != null) {
                if (match(currentMessage, message)) {
                    // 继续使用当前 message
                    message = currentMessage;
                }
            }
        }

        mMessage = message;
        onMessageLoad(mMessage);

        requestLoadData();
    }

    private void onMessageChangedInternal(@NonNull MSIMMessage message) {
        final MSIMMessage currentMessage = mMessage;
        if (currentMessage == null) {
            return;
        }

        if (match(currentMessage, message)) {
            mMessage = message;
            onMessageLoad(message);
        }
    }

    private boolean match(@NonNull MSIMMessage obj1, @NonNull MSIMMessage obj2) {
        final long sessionUserId = obj1.getSessionUserId();
        final int conversationType = obj1.getConversationType();
        final long targetUserId = obj1.getTargetUserId();
        final long messageId = obj1.getMessageId();

        if (sessionUserId > 0 && targetUserId > 0 && messageId > 0) {
            return sessionUserId == obj2.getSessionUserId()
                    && conversationType == obj2.getConversationType()
                    && targetUserId == obj2.getTargetUserId()
                    && messageId == obj2.getMessageId();
        }

        return false;
    }

    protected void onMessageLoad(@NonNull MSIMMessage message) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onMessageLoad %s", Objects.defaultObjectTag(this), message);
        }
    }

    @Nullable
    @Override
    protected MSIMMessage loadData() {
        final MSIMMessage message = mMessage;
        if (message == null) {
            return null;
        }

        return MSIMManager.getInstance().getMessageManager().getMessage(
                message.getSessionUserId(),
                message.getConversationType(),
                message.getTargetUserId(),
                message.getMessageId()
        );
    }

    @Override
    protected void onDataLoad(@Nullable MSIMMessage message) {
        if (message != null) {
            onMessageChangedInternal(message);
        }
    }

}
