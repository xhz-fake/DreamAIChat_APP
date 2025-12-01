package com.example.dreamaichat_app.data.remote.model;

public class ChatRequest {
    public String message;
    public Long conversationId;
    public String model;
    public java.util.List<ImagePayload> images;

    public static class ImagePayload {
        public String base64;
        public String mime;
    }
}
