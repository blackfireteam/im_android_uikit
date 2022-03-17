package com.masonsoft.imsdk.uikit.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.MSIMConstants;
import com.masonsoft.imsdk.MSIMMessage;

import java.util.HashMap;
import java.util.Map;

import io.github.idonans.core.util.Preconditions;

public class SnapchatCountDownHelper {

    private final Scope mScope;
    private final long mSessionUserId;
    private final int mConversationType;
    private final long mTargetUserId;
    private final long mMessageId;

    private long mCountDownStartTimeMs;
    private long mCountDownTimeMs = -1L;

    private SnapchatCountDownHelper(Scope scope, long sessionUserId, int conversationType, long targetUserId, long messageId) {
        mScope = scope;
        mSessionUserId = sessionUserId;
        mConversationType = conversationType;
        mTargetUserId = targetUserId;
        mMessageId = messageId;
    }

    /**
     * 如果倒计时尚未开始，则开始倒计时
     *
     * @param timeMs 倒计时的时长
     */
    public synchronized void startCountDown(long timeMs) {
        Preconditions.checkArgument(timeMs >= 0);

        if (mCountDownStartTimeMs <= 0) {
            mCountDownStartTimeMs = System.currentTimeMillis();

            mCountDownTimeMs = timeMs;
        }
    }

    /**
     * @return 获取当前应当显示的倒计时(毫秒)。如果倒计时已经结束，返回 0；如果倒计时尚未开始返回 -1。
     */
    public synchronized long getCurrentCountDown() {
        if (mCountDownStartTimeMs <= 0) {
            return -1L;
        }

        final long diff = System.currentTimeMillis() - mCountDownStartTimeMs;
        if (diff >= mCountDownTimeMs) {
            return 0L;
        }

        return mCountDownTimeMs - diff;
    }

    @Nullable
    public static SnapchatCountDownHelper from(@NonNull Scope scope, @Nullable MSIMBaseMessage baseMessage) {
        return scope.getOrCreate(baseMessage);
    }

    /**
     * @param baseMessage 阅后即焚消息
     * @return 该阅后即焚消息的倒计时时长 (毫秒)
     */
    public static long buildSnapchatCountDownTime(@NonNull MSIMBaseMessage baseMessage) {
        final int messageType = baseMessage.getMessageType();
        if (messageType == MSIMConstants.MessageType.TEXT) {
            return (long) ((baseMessage.getBody("").length() * 0.2f + 5) * 1000);
        }

        if (messageType == MSIMConstants.MessageType.IMAGE) {
            return 20 * 1000L;
        }

        if (messageType == MSIMConstants.MessageType.VIDEO) {
            //noinspection ConstantConditions
            return Math.max(baseMessage.getVideoElement().getDurationMilliseconds() + 2, 5) * 1000L;
        }

        if (messageType == MSIMConstants.MessageType.AUDIO) {
            //noinspection ConstantConditions
            return Math.max(baseMessage.getAudioElement().getDurationMs() + 2, 5) * 1000L;
        }

        // fallback
        return 10 * 1000L;
    }

    public static class Scope {

        private final Map<String, SnapchatCountDownHelper> mCache = new HashMap<>();

        private String buildKey(long sessionUserId, int conversationType, long targetUserId, long messageId) {
            return sessionUserId + "_" + conversationType + "_" + targetUserId + "_" + messageId + "_1ukd0i2";
        }

        @Nullable
        public synchronized SnapchatCountDownHelper getOrCreate(@Nullable MSIMBaseMessage baseMessage) {
            if (baseMessage == null) {
                return null;
            }

            if (!baseMessage.isSnapchat()) {
                return null;
            }

            if (!(baseMessage instanceof MSIMMessage)) {
                return null;
            }

            final MSIMMessage message = (MSIMMessage) baseMessage;

            return getOrCreate(
                    message.getSessionUserId(),
                    message.getConversationType(),
                    message.getTargetUserId(),
                    message.getMessageId()
            );
        }

        @NonNull
        public synchronized SnapchatCountDownHelper getOrCreate(long sessionUserId, int conversationType, long targetUserId, long messageId) {
            final String key = buildKey(sessionUserId, conversationType, targetUserId, messageId);
            SnapchatCountDownHelper helper = mCache.get(key);
            if (helper == null) {
                helper = new SnapchatCountDownHelper(this, sessionUserId, conversationType, targetUserId, messageId);
                mCache.put(key, helper);
            }
            return helper;
        }

    }

}
