package com.masonsoft.imsdk.sample.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    @NonNull
    public static Map<String, Object> toMapOrEmpty(@Nullable String originJsonObjectString) {
        Map<String, Object> mapJson = null;
        try {
            String json = originJsonObjectString;
            if (json != null) {
                try {
                    json = json.trim();
                    if (json.length() > 0) {
                        mapJson = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
                        }.getType());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (mapJson == null) {
            mapJson = new HashMap<>();
        }
        return mapJson;
    }

    @NonNull
    public static String modifyJsonObject(@Nullable String originJsonObjectString, @NonNull Consumer<Map<String, Object>> modify) {
        final Map<String, Object> mapJson = toMapOrEmpty(originJsonObjectString);
        modify.accept(mapJson);
        return new Gson().toJson(mapJson);
    }

}