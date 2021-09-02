package com.masonsoft.imsdk.uikit.util;

import io.github.idonans.core.util.Sha1Util;

public class RequestSignUtil {

    private RequestSignUtil() {
    }

    public static String calSign(String appSecret, int nonce, long timestamp/*秒*/) {
        final String input = appSecret + nonce + timestamp;
        return Sha1Util.sha1(input);
    }

}
