// TimeLog.java
package com.synctrack.model;

import java.time.LocalDateTime;
import java.time.Duration;

public class TimeLog {
    private int logId;
    private int taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;
    private boolean isActive;
    private LocalDateTime createdAt;
    
    public TimeLog() {}
    
    public TimeLog(int taskId, LocalDateTime startTime) {
        this.taskId = taskId;
        this.startTime = startTime;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.durationSeconds = 0;
    }
    
    // Getters and Setters
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public long calculateDuration() {
        if (endTime == null) {
            return Duration.between(startTime, LocalDateTime.now()).getSeconds();
        }
        return Duration.between(startTime, endTime).getSeconds();
    }
}
