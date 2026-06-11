package com.synctrack.model;

public class Achievement {
    private int achievementId;
    private String name;
    private String description;
    private String criteriaType;
    private int criteriaValue;
    private String difficultyLevel;
    private int xpReward;
    private String iconPath;
    
    // Getters and Setters
    public int getAchievementId() { return achievementId; }
    public void setAchievementId(int achievementId) { this.achievementId = achievementId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCriteriaType() { return criteriaType; }
    public void setCriteriaType(String criteriaType) { this.criteriaType = criteriaType; }
    public int getCriteriaValue() { return criteriaValue; }
    public void setCriteriaValue(int criteriaValue) { this.criteriaValue = criteriaValue; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }
}
