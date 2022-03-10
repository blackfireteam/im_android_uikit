package com.masonsoft.imsdk.uikit.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMReceivedCustomSignalingMessageListener;
import com.masonsoft.imsdk.MSIMReceivedCustomSignalingMessageListenerProxy;
import com.masonsoft.imsdk.uikit.entity.CustomMessagePayload;
import com.masonsoft.imsdk.util.Objects;

import io.github.idonans.core.thread.Threads;

public abstract class IMReceivedCustomSignalingMessageViewHelper {

    private long mSessionUserId = Long.MIN_VALUE / 2;
    private long mTargetUserId = Long.MIN_VALUE / 2;

    public IMReceivedCustomSignalingMessageViewHelper() {
        MSIMManager.getInstance().getMessageManager().addReceivedCustomSignalingMessageListener(mReceivedCustomSignalingMessageListener);
    }

    public void setTarget(long sessionUserId, long targetUserId) {
        if (mSessionUserId != sessionUserId
                || mTargetUserId != targetUserId) {
            mSessionUserId = sessionUserId;
            mTargetUserId = targetUserId;
        }
    }

    public String getDebugString() {
        //noinspection StringBufferReplaceableByString
        final StringBuilder builder = new StringBuilder();
        builder.append(Objects.defaultObjectTag(this));
        builder.append(" sessionUserId:").append(this.mSessionUserId);
        builder.append(" targetUserId:").append(this.mTargetUserId);
        return builder.toString();
    }

    public long getSessionUserId() {
        return mSessionUserId;
    }

    public long getTargetUserId() {
        return mTargetUserId;
    }

    protected abstract void onReceivedCustomSignalingMessage(@NonNull MSIMMessage message, @Nullable CustomMessagePayload customMessagePayload);

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMReceivedCustomSignalingMessageListener mReceivedCustomSignalingMessageListener = new MSIMReceivedCustomSignalingMessageListenerProxy(new MSIMReceivedCustomSignalingMessageListener() {
        private boolean notMatch(@NonNull MSIMMessage message) {
            return !MSIMConstants.isIdMatch(mSessionUserId, message.getSessionUserId())
                    || !MSIMConstants.isIdMatch(mTargetUserId, message.getTargetUserId());
        }

        @Override
        public void onReceivedCustomSignalingMessage(@NonNull MSIMMessage message) {
            if (notMatch(message)) {
                return;
            }

            final CustomMessagePayload customMessagePayload = CustomMessagePayload.fromBaseMessage(message);
            if (customMessagePayload == null) {
                return;
            }

            Threads.postUi(() -> {
                if (notMatch(message)) {
                    return;
                }
                IMReceivedCustomSignalingMessageViewHelper.this.onReceivedCustomSignalingMessage(message, customMessagePayload);
            });
        }
    });

}
