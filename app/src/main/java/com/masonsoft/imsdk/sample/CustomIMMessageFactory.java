package com.masonsoft.imsdk.sample;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.masonsoft.imsdk.MSIMMessage;
import com.masonsoft.imsdk.MSIMMessageFactory;

import java.util.HashMap;
import java.util.Map;

public class CustomIMMessageFactory {

    /**
     * 自定义消息(喜欢)
     */
    @NonNull
    public static MSIMMessage createCustomMessageLike() {
        final Map<String, Object> data = new HashMap<>();
        data.put("type", 1);
        data.put("desc", "like");
        final String text = new Gson().toJson(data);
        return MSIMMessageFactory.createCustomMessage(text);
    }

}