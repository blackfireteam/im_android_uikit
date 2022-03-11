package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomMessage;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

import io.github.idonans.core.util.IOUtil;

// TODO FIXME
@Deprecated
public abstract class MSIMChatRoomMessageLoader extends DataLoaderImpl<MSIMChatRoomMessage> implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    @NonNull
    private final MSIMChatRoomContext mChatRoomContext;
    private final MSIMChatRoomMessageChangedHelper mHelper;

    @Nullable
    protected MSIMChatRoomMessage mMessage;

    public MSIMChatRoomMessageLoader(@NonNull MSIMChatRoomContext chatRoomContext) {
        mChatRoomContext = chatRoomContext;
        mHelper = new MSIMChatRoomMessageChangedHelper(chatRoomContext) {
            @Override
            protected void onMessageChanged(@NonNull MSIMChatRoomMessage message) {
                super.onMessageChanged(message);

                MSIMChatRoomMessageLoader.this.onMessageChangedInternal(message, false);
            }
        };
    }

    @Override
    public void close() throws IOException {
        super.close();
        IOUtil.closeQuietly(mHelper);
    }

    @Nullable
    public MSIMChatRoomMessage getMessage() {
        return mMessage;
    }

    public void setMessage(@Nullable MSIMChatRoomMessage message) {
        setMessageInternal(message);
    }

    private void setMessageInternal(@Nullable MSIMChatRoomMessage message) {
        mMessage = message;
        onMessageLoad(mMessage);
    }

    private void onMessageChangedInternal(@Nullable MSIMChatRoomMessage message, boolean acceptNull) {
        if (!acceptNull && message == null) {
            return;
        }

        if (message != null && mMessage != null && !message.equals(mMessage)) {
            return;
        }

        mMessage = message;
        onMessageLoad(mMessage);
    }

    protected void onMessageLoad(@Nullable MSIMChatRoomMessage message) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onMessageLoad %s", Objects.defaultObjectTag(this), message);
        }
    }

    @Override
    public void requestLoadData() {
        MSIMUikitLog.v("%s do not require requestLoadData", Objects.defaultObjectTag(this));
    }

    @Nullable
    @Override
    protected MSIMChatRoomMessage loadData() {
        MSIMUikitLog.v("%s not impl loadData", Objects.defaultObjectTag(this));
        // ignore
        return null;
    }

    @Override
    protected void onDataLoad(@Nullable MSIMChatRoomMessage data) {
        MSIMUikitLog.v("%s not impl onDataLoad", Objects.defaultObjectTag(this));
        // ignore
    }

}
