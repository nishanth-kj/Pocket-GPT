package com.pocketgpt.app.model.response;

import com.pocketgpt.app.constants.ResponseStatus;

public class ApiResponse<T> {
    public ResponseStatus status;
    public T data;
    public ApiError error;
}