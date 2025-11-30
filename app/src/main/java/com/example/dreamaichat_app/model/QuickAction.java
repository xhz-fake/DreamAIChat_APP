package com.example.dreamaichat_app.model;

/**
 * 蹇嵎鎿嶄綔锛圕hip锛夐厤缃€? */
public class QuickAction {

    private final String id;
    private final String label;
    private final String prompt;

    public QuickAction(String id, String label, String prompt) {
        this.id = id;
        this.label = label;
        this.prompt = prompt;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getPrompt() {
        return prompt;
    }
}
