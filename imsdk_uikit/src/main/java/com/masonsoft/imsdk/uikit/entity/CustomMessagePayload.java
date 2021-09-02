package com.masonsoft.imsdk.uikit.entity;

import androidx.annotation.NonNull;

import com.google.common.base.Verify;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.masonsoft.imsdk.lang.StateProp;
import com.masonsoft.imsdk.uikit.MSIMUikitLog;

public class CustomMessagePayload {

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

}
