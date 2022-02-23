package com.masonsoft.imsdk.sample.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StringUtil {

    @NonNull
    public static String toStringOrEmpty(@Nullable Object object) {
        if (object != null) {
            return object.toString();
        }
        return "";
    }

}