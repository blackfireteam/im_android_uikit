package com.masonsoft.imsdk.uikit.util;

import android.text.TextUtils;

import androidx.annotation.StringRes;

import com.masonsoft.imsdk.uikit.R;

import io.github.idonans.core.util.ContextUtil;
import io.github.idonans.core.util.ToastUtil;

public class TipUtil {

    public static void showOrDefault(String message) {
        if (TextUtils.isEmpty(message)) {
            show(R.string.imsdk_uikit_tip_text_error_unknown);
        } else {
            show(message);
        }
    }

    public static void show(String message) {
        ToastUtil.show(message);
    }

    public static void show(@StringRes int resId) {
        show(ContextUtil.getContext().getString(resId));
    }

    public static void show(@StringRes int resId, Object... formatArgs) {
        show(ContextUtil.getContext().getString(resId, formatArgs));
    }

    public static void showNetworkError() {
        show(R.string.imsdk_uikit_tip_text_network_error);
    }

}
