package com.masonsoft.imsdk.uikit;

import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMChatRoomContext;
import com.masonsoft.imsdk.MSIMChatRoomMessage;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.util.Objects;

import java.io.Closeable;
import java.io.IOException;

import io.github.idonans.core.util.IOUtil;
import io.github.idonans.core.util.Preconditions;

@Deprecated
public abstract class MSIMBaseMessageLoader extends DataLoaderImpl<MSIMBaseMessage> implements Closeable {

    private static final boolean DEBUG = MSIMUikitConstants.DEBUG_WIDGET;

    @Nullable
    private MSIMMessageLoader mMessageLoader;
    @Nullable
    private MSIMChatRoomMessageLoader mChatRoomMessageLoader;

    @Nullable
    protected MSIMBaseMessage mBaseMessage;

    @Override
    public void close() throws IOException {
        super.close();

        IOUtil.closeQuietly(mMessageLoader);
        IOUtil.closeQuietly(mChatRoomMessageLoader);
    }

    private void initMessageLoader() {
        Preconditions.checkArgument(mChatRoomMessageLoader == null);
        if (mMessageLoader == null) {
            mMessageLoader = new MSIMMessageLoader() {
                @Override
                protected void onMessageLoad(@Nullable MSIMMessage message) {
                    super.onMessageLoad(message);

                    mBaseMessage = message;
                    MSIMBaseMessageLoader.this.onBaseMessageLoad(mBaseMessage);
                }
            };
        }
    }

    private void initChatRoomMessageLoader(MSIMChatRoomContext chatRoomContext) {
        Preconditions.checkArgument(mMessageLoader == null);
        if (mChatRoomMessageLoader == null) {
            mChatRoomMessageLoader = new MSIMChatRoomMessageLoader(chatRoomContext) {
                @Override
                protected void onMessageLoad(@Nullable MSIMChatRoomMessage message) {
                    super.onMessageLoad(message);

                    mBaseMessage = message;
                    MSIMBaseMessageLoader.this.onBaseMessageLoad(mBaseMessage);
                }
            };
        }
    }

    public void setBaseMessage(@Nullable MSIMBaseMessage baseMessage) {
        if (baseMessage instanceof MSIMMessage) {
            initMessageLoader();
            mBaseMessage = baseMessage;
            //noinspection ConstantConditions
            mMessageLoader.setMessage((MSIMMessage) baseMessage);
        } else if (baseMessage instanceof MSIMChatRoomMessage) {
            final MSIMChatRoomMessage chatRoomMessage = (MSIMChatRoomMessage) baseMessage;
            final GlobalChatRoomManager.StaticChatRoomContext staticChatRoomContext = GlobalChatRoomManager.getInstance()
                    .getStaticChatRoomContext(
                            chatRoomMessage.getSessionUserId(),
                            chatRoomMessage.getChatRoomId(),
                            false);
            if (staticChatRoomContext != null) {
                final MSIMChatRoomContext chatRoomContext = staticChatRoomContext.getChatRoomContext();
                initChatRoomMessageLoader(chatRoomContext);
                mBaseMessage = baseMessage;
                //noinspection ConstantConditions
                mChatRoomMessageLoader.setMessage((MSIMChatRoomMessage) baseMessage);
            } else {
                MSIMUikitLog.e("unexpected. chat room context not found %s", baseMessage);
                mBaseMessage = baseMessage;
                MSIMBaseMessageLoader.this.onBaseMessageLoad(mBaseMessage);
            }
        } else {
            mBaseMessage = null;
            MSIMBaseMessageLoader.this.onBaseMessageLoad(null);
        }
    }

    @Nullable
    public MSIMBaseMessage getBaseMessage() {
        return mBaseMessage;
    }

    @Override
    public void requestLoadData() {
        if (mMessageLoader != null) {
            mMessageLoader.requestLoadData();
        }
        if (mChatRoomMessageLoader != null) {
            mChatRoomMessageLoader.requestLoadData();
        }
    }

    @Nullable
    @Override
    protected MSIMBaseMessage loadData() {
        MSIMUikitLog.v("%s not impl loadData", Objects.defaultObjectTag(this));
        // ignore
        return null;
    }

    @Override
    protected void onDataLoad(@Nullable MSIMBaseMessage data) {
        MSIMUikitLog.v("%s not impl onDataLoad", Objects.defaultObjectTag(this));
        // ignore
    }

    protected void onBaseMessageLoad(@Nullable MSIMBaseMessage baseMessage) {
        if (DEBUG) {
            MSIMUikitLog.v("%s onBaseMessageLoad %s", Objects.defaultObjectTag(this), baseMessage);
        }
    }

}
