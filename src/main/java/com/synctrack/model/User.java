// User.java
package com.synctrack.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String salt;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private int xpTotal;
    private int level;
    private int currentStreak;
    private int longestStreak;
    private int totalTasksCompleted;
    private long totalTimeSeconds;
    private String themePreference;
    
    public User() {}
    
    public User(String username, String email, String passwordHash, String salt) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = LocalDateTime.now();
        this.level = 1;
        this.xpTotal = 0;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.totalTasksCompleted = 0;
        this.totalTimeSeconds = 0;
        this.themePreference = "light";
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public int getXpTotal() { return xpTotal; }
    public void setXpTotal(int xpTotal) { this.xpTotal = xpTotal; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    
    public int getTotalTasksCompleted() { return totalTasksCompleted; }
    public void setTotalTasksCompleted(int totalTasksCompleted) { this.totalTasksCompleted = totalTasksCompleted; }
    
    public long getTotalTimeSeconds() { return totalTimeSeconds; }
    public void setTotalTimeSeconds(long totalTimeSeconds) { this.totalTimeSeconds = totalTimeSeconds; }
    
    public String getThemePreference() { return themePreference; }
    public void setThemePreference(String themePreference) { this.themePreference = themePreference; }
    
    public void addXp(int xp) {
        this.xpTotal += xp;
        updateLevel();
    }
    
    private void updateLevel() {
        int newLevel = 1;
        while (xpTotal >= 100 * (newLevel * newLevel)) {
            newLevel++;
        }
        this.level = newLevel - 1;
    }
    
    public int getXpForNextLevel() {
        return (100 * ((level + 1) * (level + 1))) - xpTotal;
    }
    
    public double getLevelProgress() {
        int currentLevelXp = 100 * (level * level);
        int nextLevelXp = 100 * ((level + 1) * (level + 1));
        if (nextLevelXp == currentLevelXp) return 0;
        return (double)(xpTotal - currentLevelXp) / (nextLevelXp - currentLevelXp);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
