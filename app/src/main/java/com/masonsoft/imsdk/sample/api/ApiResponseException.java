package com.masonsoft.imsdk.sample.api;

public class ApiResponseException extends RuntimeException {

    public final int code;
    public final String message;

    public ApiResponseException(int code, String message) {
        super("code:" + code + ", message:" + message);
        this.code = code;
        this.message = message;
    }

}
