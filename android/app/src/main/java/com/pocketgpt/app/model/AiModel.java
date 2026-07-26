package com.pocketgpt.app.model;

public class AiModel {
    private String id;
    private String name;
    private String publisher;
    private String sizeFormatted;
    private String description;

    public AiModel(String id, String name, String publisher, String sizeFormatted, String description) {
        this.id = id;
        this.name = name;
        this.publisher = publisher;
        this.sizeFormatted = sizeFormatted;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getSizeFormatted() {
        return sizeFormatted;
    }

    public String getDescription() {
        return description;
    }
}
