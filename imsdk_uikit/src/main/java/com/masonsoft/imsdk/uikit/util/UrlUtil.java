package com.masonsoft.imsdk.uikit.util;

import androidx.annotation.Nullable;

public class UrlUtil {

    private UrlUtil() {
    }

    @Nullable
    public static String alignUrl(@Nullable String url) {
        if (url != null) {
            if (url.startsWith("/")) {
                url = "file://" + url;
            }
        }
        return url;
    }

}
