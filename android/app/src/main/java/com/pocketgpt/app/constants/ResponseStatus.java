package com.pocketgpt.app.constants;

public enum ResponseStatus {
    SUCCESS(200, "Success"),
    ERROR(500, "Error"),
    LOADING(100, "Loading");

    private final int code;
    private final String value;

    ResponseStatus(int code, String value) {
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