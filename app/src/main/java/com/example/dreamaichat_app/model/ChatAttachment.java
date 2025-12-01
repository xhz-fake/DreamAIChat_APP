package com.example.dreamaichat_app.model;

import androidx.annotation.Nullable;

/**
 * UI 层用于描述一条消息附带的多媒体资源
 */
public class ChatAttachment {

    public static final String TYPE_IMAGE = "image";

    private final String type;
    private final String localPath;
    private final String mimeType;
    private final String remoteUrl;

    public ChatAttachment(String type, @Nullable String localPath, @Nullable String mimeType, @Nullable String remoteUrl) {
        this.type = type;
        this.localPath = localPath;
        this.mimeType = mimeType;
        this.remoteUrl = remoteUrl;
    }

    public static ChatAttachment image(@Nullable String localPath, @Nullable String mimeType, @Nullable String remoteUrl) {
        return new ChatAttachment(TYPE_IMAGE, localPath, mimeType, remoteUrl);
    }

    public String getType() {
        return type;
    }

    @Nullable
    public String getLocalPath() {
        return localPath;
    }

    @Nullable
    public String getMimeType() {
        return mimeType;
    }

    @Nullable
    public String getRemoteUrl() {
        return remoteUrl;
    }

    public boolean isImage() {
        return TYPE_IMAGE.equals(type);
    }
}


