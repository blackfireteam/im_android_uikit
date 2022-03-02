package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

import io.github.idonans.core.util.IOUtil;

@Deprecated
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

                MSIMMessageLoader.this.onMessageChangedInternal(message, false);
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

    public void setMessage(@Nullable MSIMMessage message) {
        setMessageInternal(message);
    }

    private void setMessageInternal(@Nullable MSIMMessage message) {
        mMessage = message;
        onMessageLoad(mMessage);

        requestLoadData();
    }

    private void onMessageChangedInternal(@Nullable MSIMMessage message, boolean acceptNull) {
        if (!acceptNull && message == null) {
            return;
        }

        if (message != null && mMessage != null && !message.equals(mMessage)) {
            return;
        }

        mMessage = message;
        onMessageLoad(mMessage);
    }

    protected void onMessageLoad(@Nullable MSIMMessage message) {
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
        onMessageChangedInternal(message, true);
    }

}
