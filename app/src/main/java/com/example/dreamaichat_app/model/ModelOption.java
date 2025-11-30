package com.example.dreamaichat_app.model;

/**
 * 鍙€夋ā鍨嬩俊鎭€? */
public class ModelOption {

    private final String id;
    private final String displayName;
    private final String description;
    private final String badge;
    private final int accentColor;
    private final boolean available;

    public ModelOption(String id,
                       String displayName,
                       String description,
                       String badge,
                       int accentColor,
                       boolean available) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.badge = badge;
        this.accentColor = accentColor;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getBadge() {
        return badge;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public boolean isAvailable() {
        return available;
    }
}
