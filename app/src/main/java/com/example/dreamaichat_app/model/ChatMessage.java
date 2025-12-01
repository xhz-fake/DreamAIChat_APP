package com.example.dreamaichat_app.model;

import java.util.List;
import java.util.Objects;

/**
 * 浼氳瘽涓殑鍗曟潯娑堟伅銆? */
public class ChatMessage {

    private final long id;
    private final ChatRole role;
    private final String content;
    private final long timestamp;
    private final List<ChatAttachment> attachments;
    private MessageStatus status;

    public ChatMessage(long id,
                       ChatRole role,
                       String content,
                       long timestamp,
                       List<ChatAttachment> attachments,
                       MessageStatus status) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
        this.attachments = attachments;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public ChatRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<ChatAttachment> getAttachments() {
        return attachments;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
