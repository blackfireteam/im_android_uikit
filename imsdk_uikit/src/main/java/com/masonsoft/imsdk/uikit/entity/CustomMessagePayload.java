package com.masonsoft.imsdk.uikit.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Verify;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.masonsoft.imsdk.MSIMBaseMessage;
import com.masonsoft.imsdk.lang.StateProp;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;
import com.masonsoft.imsdk.uikit.uniontype.DataObject;

public class CustomMessagePayload {

    /**
     * 喜欢
     */
    public static final int TYPE_LIKE = 1;
    /**
     * 对方正在输入...
     */
    public static final int TYPE_BEING_TYPED = 100;
    /**
     * 实时语音聊天
     */
    public static final int TYPE_AUDIO = 200;
    /**
     * 实时视频聊天
     */
    public static final int TYPE_VIDEO = 300;

    private final StateProp<JsonObject> mOriginJson = new StateProp<>();
    private final StateProp<Integer> mType = new StateProp<>();

    public CustomMessagePayload(String json) {
        try {
            final JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            if (jsonObject != null) {
                if (jsonObject.has("type")) {
                    final int type = jsonObject.getAsJsonPrimitive("type").getAsInt();
                    mType.set(type);
                    mOriginJson.set(jsonObject);
                }
            }
        } catch (Throwable e) {
            MSIMUikitLog.e(e);
        }
    }

    public boolean isLike() {
        return !mType.isUnset() && mType.get() == TYPE_LIKE;
    }

    public boolean isTypeBeingTyped() {
        return !mType.isUnset() && mType.get() == TYPE_BEING_TYPED;
    }

    public boolean isTypeAudio() {
        return !mType.isUnset() && mType.get() == TYPE_AUDIO;
    }

    public boolean isTypeVideo() {
        return !mType.isUnset() && mType.get() == TYPE_VIDEO;
    }

    @NonNull
    public JsonObject requireOriginJson() {
        final JsonObject originJson = mOriginJson.get();
        Verify.verifyNotNull(originJson);
        return originJson;
    }

    public boolean hasType() {
        return !mType.isUnset();
    }

    public int getType() {
        return mType.get();
    }

    public boolean hasOriginJson() {
        return !mOriginJson.isUnset();
    }

    @Nullable
    public static CustomMessagePayload fromBaseMessage(@Nullable MSIMBaseMessage baseMessage) {
        if (baseMessage == null) {
            return null;
        }
        final String body = baseMessage.getBody();
        final CustomMessagePayload customMessagePayload = new CustomMessagePayload(body);
        if (!customMessagePayload.hasType()) {
            return null;
        }
        return customMessagePayload;
    }

    private static final String CUSTOM_MESSAGE_PAYLOAD_CACHE_KEY = "CustomMessagePayloadCacheKey_20220309_xx9o2lf";

    @Nullable
    public static CustomMessagePayload fromDataObjectWithCache(@Nullable DataObject dataObject) {
        if (dataObject == null) {
            return null;
        }

        CustomMessagePayload customMessagePayload = dataObject.getExtObject(CUSTOM_MESSAGE_PAYLOAD_CACHE_KEY, null);
        if (customMessagePayload != null) {
            return customMessagePayload;
        }

        final MSIMBaseMessage baseMessage = dataObject.getObject(MSIMBaseMessage.class);
        customMessagePayload = fromBaseMessage(baseMessage);
        if (customMessagePayload != null) {
            dataObject.putExtObject(CUSTOM_MESSAGE_PAYLOAD_CACHE_KEY, customMessagePayload);
        }
        return customMessagePayload;
    }
}
