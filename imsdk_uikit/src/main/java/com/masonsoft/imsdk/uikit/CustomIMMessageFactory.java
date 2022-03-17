package com.masonsoft.imsdk.uikit;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMMessageFactory;
import com.masonsoft.imsdk.uikit.entity.RtcMessagePayload;

import java.util.HashMap;
import java.util.Map;

public class CustomIMMessageFactory {

    /**
     * 自定义消息:喜欢
     */
    @NonNull
    public static MSIMMessage createCustomMessageLike() {
        return MSIMMessageFactory.createCustomEmotionMessage("008");
    }

    /**
     * 自定义信令消息:输入中...
     */
    @NonNull
    public static String createCustomSignalingTyped() {
        final Map<String, Object> data = new HashMap<>();
        data.put("type", 100);
        return new Gson().toJson(data);
    }

    /**
     * 自定义 rtc 消息(通过自定义信令消息或者自定义普通消息发送)
     */
    @NonNull
    public static String createCustomRtcMessage(@NonNull RtcMessagePayload rtcMessagePayload) {
        final Map<String, Object> data = new HashMap<>();
        data.put("type", rtcMessagePayload.type.get());
        data.put("event", rtcMessagePayload.event.get());
        data.put("room_id", rtcMessagePayload.roomId.get());
        if (!rtcMessagePayload.duration.isUnset()) {
            data.put("duration", rtcMessagePayload.duration.get());
        }
        return new Gson().toJson(data);
    }

}