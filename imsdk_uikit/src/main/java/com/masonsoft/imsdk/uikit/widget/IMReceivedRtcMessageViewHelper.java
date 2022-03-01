package com.masonsoft.imsdk.uikit.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMManager;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMMessageListener;
import com.masonsoft.imsdk.MSIMMessageListenerAdapter;
import com.masonsoft.imsdk.MSIMMessagePageContext;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.entity.CustomMessagePayload;
import com.masonsoft.imsdk.uikit.entity.RtcMessagePayload;

import io.github.idonans.core.thread.TaskQueue;
import io.github.idonans.core.thread.Threads;

public abstract class IMReceivedRtcMessageViewHelper extends IMReceivedCustomSignalingMessageViewHelper {

    public IMReceivedRtcMessageViewHelper() {
        MSIMManager.getInstance().getMessageManager().addMessageListener(
                MSIMMessagePageContext.GLOBAL,
                mMessageListener
        );
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

        private boolean notMatch(@NonNull MSIMMessage message) {
            return !MSIMConstants.isIdMatch(IMReceivedRtcMessageViewHelper.this.getSessionUserId(), message.getSessionUserId())
                    || !MSIMConstants.isIdMatch(IMReceivedRtcMessageViewHelper.this.getTargetUserId(), message.getTargetUserId());
        }

        @Override
        public void onMessageChanged(@NonNull MSIMMessage message) {
            super.onMessageChanged(message);

            if (notMatch(message)) {
                return;
            }

            mDispatchQueue.enqueue(() -> {
                final int messageType = message.getMessageType();
                if (!MSIMConstants.MessageType.isCustomMessage(messageType)) {
                    MSIMUikitLog.i("ignore. message is not custom message. messageType:%s", messageType);
                    return;
                }

                final CustomMessagePayload customMessagePayload = createCustomObject(message);
                Threads.postUi(() -> {
                    if (notMatch(message)) {
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
