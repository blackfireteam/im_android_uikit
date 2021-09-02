package com.masonsoft.imsdk.uikit.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import io.github.idonans.core.util.ContextUtil;

/**
 * @since 1.0
 */
public class ClipboardUtil {

    private ClipboardUtil() {
    }

    public static void copy(String text) {
        ClipboardManager cm = (ClipboardManager) ContextUtil.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(null, text));
        }
    }

}
