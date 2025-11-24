package com.example.dreamaichat_app.data.remote.model;

/**
 * 聊天请求
 */
public class ChatRequest {
    public String message;
    public Long conversationId;
    public String model;
    public String token; // 临时字段，实际应该放在Header中
}

