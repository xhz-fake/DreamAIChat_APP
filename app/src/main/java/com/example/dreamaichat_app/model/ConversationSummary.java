package com.example.dreamaichat_app.model;

/**
 * 鍘嗗彶浼氳瘽鍒楄〃浣跨敤鐨勬瑕佷俊鎭€? */
public class ConversationSummary {

    private final long id;
    private final String title;
    private final String snippet;
    private final long lastTimestamp;
    private final String modelTag;
    private final boolean pinned;

    public ConversationSummary(long id,
                               String title,
                               String snippet,
                               long lastTimestamp,
                               String modelTag,
                               boolean pinned) {
        this.id = id;
        this.title = title;
        this.snippet = snippet;
        this.lastTimestamp = lastTimestamp;
        this.modelTag = modelTag;
        this.pinned = pinned;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public String getModelTag() {
        return modelTag;
    }

    public boolean isPinned() {
        return pinned;
    }
}
