// Task.java
package com.synctrack.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private int taskId;
    private int userId;
    private String title;
    private String description;
    private String category;
    private int priority; // 1-5 (1 highest)
    private String difficulty; // Easy, Medium, Hard, Expert
    private LocalDateTime deadline;
    private Integer estimatedTime; // minutes
    private String status; // pending, in_progress, completed, archived
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private long actualTimeSeconds;
    private int xpEarned;
    
    public Task() {
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
        this.priority = 3;
        this.difficulty = "Medium";
    }
    
    // Getters and Setters
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    
    public Integer getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public long getActualTimeSeconds() { return actualTimeSeconds; }
    public void setActualTimeSeconds(long actualTimeSeconds) { this.actualTimeSeconds = actualTimeSeconds; }
    
    public int getXpEarned() { return xpEarned; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }
    
    public boolean isOverdue() {
        if (deadline == null || status.equals("completed")) return false;
        return deadline.isBefore(LocalDateTime.now());
    }
    
    public String getPriorityColor() {
        switch (priority) {
            case 1: return "#f44336";
            case 2: return "#ff9800";
            case 3: return "#ffc107";
            case 4: return "#8bc34a";
            case 5: return "#4caf50";
            default: return "#9e9e9e";
        }
    }
    
    @Override
    public String toString() {
        return title;
    }
}
