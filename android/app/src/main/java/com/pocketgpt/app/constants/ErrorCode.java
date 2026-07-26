package com.pocketgpt.app.constants;

public enum ErrorCode {
    UNKNOWN(1000, "Unknown Error"),
    NETWORK_FAILURE(1001, "Network Failure"),
    UNAUTHORIZED(1002, "Unauthorized");

    private final int code;
    private final String value;

    ErrorCode(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}