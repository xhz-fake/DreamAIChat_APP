package com.example.dreamaichat_app.data.remote.model;

/**
 * 后端会话概览响应模型
 */
public class ConversationSummaryResponse {
    public Long id;
    public String title;
    public String snippet;
    public String latestMessage;
    public String provider;
    public String model;
    public Long updatedAt;
    public Boolean pinned;
}

