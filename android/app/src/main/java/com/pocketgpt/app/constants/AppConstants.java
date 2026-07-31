package com.pocketgpt.app.constants;

public class AppConstants {
    public static final String APP_NAME = "Pocket GPT";
    public enum Status {
        INACTIVE(0, "Inactive"),
        ACTIVE(1, "Active");

        private final int code;
        private final String value;

        Status(int code, String value) {
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
}