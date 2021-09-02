package com.masonsoft.imsdk.uikit.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMMessageListener;
import com.masonsoft.imsdk.MSIMMessageListenerAdapter;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.entity.CustomMessagePayload;
import com.masonsoft.imsdk.uikit.entity.RtcMessagePayload;

import io.github.idonans.core.thread.TaskQueue;
import io.github.idonans.core.thread.Threads;

public abstract class IMReceivedRtcMessageViewHelper extends IMReceivedCustomSignalingMessageViewHelper {

    public IMReceivedRtcMessageViewHelper() {
        MSIMManager.getInstance().getMessageManager().addMessageListener(mMessageListener);
    }

    @Override
    protected final void onReceivedCustomSignalingMessage(@NonNull MSIMMessage message, @Nullable CustomMessagePayload customMessagePayload) {
        if (customMessagePayload == null) {
            return;
        }
        if (customMessagePayload.isTypeAudio() || customMessagePayload.isTypeVideo()) {
            final JsonObject jsonObject = customMessagePayload.requireOriginJson();
            final RtcMessagePayload rtcMessagePayload = RtcMessagePayload.fromJsonObject(jsonObject);
            if (rtcMessagePayload != null) {
                onReceivedRtcMessage(message, customMessagePayload, rtcMessagePayload);
            } else {
                MSIMUikitLog.e("unexpected rtcMessagePayload is null");
            }
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final MSIMMessageListener mMessageListener = new MSIMMessageListenerAdapter() {
        private final TaskQueue mDispatchQueue = new TaskQueue(1);

        private boolean notMatch(long sessionUserId, long targetUserId) {
            return !MSIMConstants.isIdMatch(IMReceivedRtcMessageViewHelper.this.getSessionUserId(), sessionUserId)
                    || !MSIMConstants.isIdMatch(IMReceivedRtcMessageViewHelper.this.getTargetUserId(), targetUserId);
        }

        @Override
        public void onMessageCreated(long sessionUserId, int conversationType, long targetUserId, long localMessageId) {
            if (notMatch(sessionUserId, targetUserId)) {
                return;
            }

            mDispatchQueue.enqueue(() -> {
                final MSIMMessage message = MSIMManager.getInstance().getMessageManager().getMessage(
                        sessionUserId, conversationType, targetUserId, localMessageId);
                if (message == null) {
                    MSIMUikitLog.e("unexpected message is null");
                    return;
                }
                final int messageType = message.getMessageType();
                if (!MSIMConstants.MessageType.isCustomMessage(messageType)) {
                    MSIMUikitLog.i("ignore. message is not custom message. messageType:%s", messageType);
                    return;
                }

                final CustomMessagePayload customMessagePayload = createCustomObject(message);
                Threads.postUi(() -> {
                    if (notMatch(sessionUserId, targetUserId)) {
                        return;
                    }

                    IMReceivedRtcMessageViewHelper.this.onReceivedCustomSignalingMessage(message, customMessagePayload);
                });
            });
        }
    };

    /**
     * 收到 rtc(音视频通话) 消息(可能来自于自定义信令消息，也可能来自于自定义普通消息)
     */
    protected abstract void onReceivedRtcMessage(@NonNull MSIMMessage message, @NonNull CustomMessagePayload customMessagePayload, @NonNull RtcMessagePayload rtcMessagePayload);

}
