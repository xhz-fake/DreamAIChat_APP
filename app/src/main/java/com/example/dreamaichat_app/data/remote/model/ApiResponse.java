package com.example.dreamaichat_app.data.remote.model;

/**
 * API 统一响应格式
 */
public class ApiResponse<T> {
    public int code;
    public String message;
    public boolean success;
    public T data;
}

