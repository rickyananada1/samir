package com.samir.test.Test.Samir.dto;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private String username;

    public TaskResponse(Long id, String title, String description, boolean completed, String username) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.username = username;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
