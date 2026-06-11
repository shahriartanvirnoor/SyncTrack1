package com.synctrack.model;

import java.time.LocalDate;

public class DailyStat {
    private int statId;
    private int userId;
    private LocalDate date;
    private int tasksCompleted;
    private long totalTimeSeconds;
    private int xpEarned;
    private double productivityScore;
    
    // Getters and Setters
    public int getStatId() { return statId; }
    public void setStatId(int statId) { this.statId = statId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(int tasksCompleted) { this.tasksCompleted = tasksCompleted; }
    public long getTotalTimeSeconds() { return totalTimeSeconds; }
    public void setTotalTimeSeconds(long totalTimeSeconds) { this.totalTimeSeconds = totalTimeSeconds; }
    public int getXpEarned() { return xpEarned; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }
    public double getProductivityScore() { return productivityScore; }
    public void setProductivityScore(double productivityScore) { this.productivityScore = productivityScore; }
}
