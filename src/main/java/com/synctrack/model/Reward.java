// Reward.java
package com.synctrack.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Reward Model - Represents a redeemable reward in the SyncTrack gamification system
 * Users can spend XP to unlock various rewards like badges, titles, themes, and features
 */
public class Reward {
    
    // ==================== ENUMS ====================
    
    /**
     * Type of reward available for redemption
     */
    public enum RewardType {
        BADGE("Badge", "Displayable achievement badge"),
        TITLE("Title", "Custom user title displayed on profile"),
        THEME("Theme", "Custom UI theme/skin"),
        FEATURE("Feature", "Unlock special feature"),
        POWER_UP("Power Up", "Temporary productivity boost"),
        AVATAR("Avatar", "Custom profile avatar"),
        SOUND_PACK("Sound Pack", "Custom notification sounds"),
        BACKGROUND("Background", "Custom dashboard background");
        
        private final String displayName;
        private final String description;
        
        RewardType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Rarity level affecting cost and prestige
     */
    public enum Rarity {
        COMMON(1.0, "gray", "Common"),
        UNCOMMON(1.5, "green", "Uncommon"),
        RARE(2.0, "blue", "Rare"),
        EPIC(3.0, "purple", "Epic"),
        LEGENDARY(5.0, "gold", "Legendary");
        
        private final double costMultiplier;
        private final String color;
        private final String displayName;
        
        Rarity(double costMultiplier, String color, String displayName) {
            this.costMultiplier = costMultiplier;
            this.color = color;
            this.displayName = displayName;
        }
        
        public double getCostMultiplier() { return costMultiplier; }
        public String getColor() { return color; }
        public String getDisplayName() { return displayName; }
    }
    
    /**
     * Status of reward for a specific user
     */
    public enum RewardStatus {
        LOCKED("🔒", "Locked - Not enough XP"),
        AVAILABLE("🟢", "Available - Can be redeemed"),
        REDEEMED("✅", "Redeemed - Already unlocked"),
        EQUIPPED("⭐", "Equipped - Currently using");
        
        private final String icon;
        private final String description;
        
        RewardStatus(String icon, String description) {
            this.icon = icon;
            this.description = description;
        }
        
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
    }
    
    // ==================== FIELDS ====================
    
    private int rewardId;
    private String name;
    private String description;
    private RewardType type;
    private Rarity rarity;
    private int baseXpCost;
    private int calculatedXpCost;
    private boolean isLimitedTime;
    private LocalDateTime availableFrom;
    private LocalDateTime availableUntil;
    private String iconPath;
    private String previewImagePath;
    private String unlockMessage;
    private Map<String, Object> properties; // Additional properties specific to reward type
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private int requiredLevel;
    private int requiredStreak;
    private String[] requiredAchievements;
    
    // ==================== CONSTRUCTORS ====================
    
    public Reward() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.rarity = Rarity.COMMON;
        this.type = RewardType.BADGE;
        this.requiredLevel = 1;
        this.requiredStreak = 0;
        this.requiredAchievements = new String[0];
        this.properties = new HashMap<>();
    }
    
    public Reward(String name, String description, RewardType type, Rarity rarity, int baseXpCost) {
        this();
        this.name = name;
        this.description = description;
        this.type = type;
        this.rarity = rarity;
        this.baseXpCost = baseXpCost;
        this.calculatedXpCost = calculateActualCost();
    }
    
    // ==================== BUSINESS LOGIC METHODS ====================
    
    /**
     * Calculate actual XP cost based on rarity multiplier
     */
    public int calculateActualCost() {
        return (int)(baseXpCost * rarity.getCostMultiplier());
    }
    
    /**
     * Check if reward is currently available for redemption
     */
    public boolean isAvailable() {
        if (!isActive) return false;
        if (isLimitedTime) {
            LocalDateTime now = LocalDateTime.now();
            if (availableFrom != null && now.isBefore(availableFrom)) return false;
            if (availableUntil != null && now.isAfter(availableUntil)) return false;
        }
        return true;
    }
    
    /**
     * Check if user meets requirements for this reward
     */
    public boolean meetsRequirements(User user, List<String> userAchievements) {
        if (user.getLevel() < requiredLevel) return false;
        if (user.getCurrentStreak() < requiredStreak) return false;
        
        // Check required achievements
        if (requiredAchievements != null && requiredAchievements.length > 0) {
            for (String required : requiredAchievements) {
                if (!userAchievements.contains(required)) return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get status for a specific user based on their XP and redemption status
     */
    public RewardStatus getStatusForUser(User user, boolean isRedeemed, int userXp) {
        if (isRedeemed) return RewardStatus.REDEEMED;
        if (!meetsRequirements(user, new ArrayList<>())) return RewardStatus.LOCKED;
        if (userXp >= calculatedXpCost) return RewardStatus.AVAILABLE;
        return RewardStatus.LOCKED;
    }
    
    /**
     * Calculate progress percentage toward this reward
     */
    public double getProgressPercentage(int userXp) {
        if (userXp >= calculatedXpCost) return 1.0;
        return (double) userXp / calculatedXpCost;
    }
    
    /**
     * Get XP needed to unlock this reward
     */
    public int getXpNeeded(int userXp) {
        return Math.max(0, calculatedXpCost - userXp);
    }
    
    /**
     * Apply reward effects to user (called when redeemed)
     */
    public void applyReward(User user) {
        switch (type) {
            case TITLE:
                // Set custom title prefix/suffix for user
                user.setCustomTitle(name);
                break;
            case THEME:
                // Change UI theme
                user.setThemePreference(name.toLowerCase());
                break;
            case FEATURE:
                // Unlock special feature
                user.unlockFeature(getFeatureKey());
                break;
            case POWER_UP:
                // Apply temporary boost (2x XP for 24 hours)
                user.addPowerUp(getPowerUpEffect(), 24);
                break;
            case AVATAR:
                // Set custom avatar
                user.setAvatarIcon(iconPath);
                break;
            default:
                // Badges and others are purely cosmetic
                break;
        }
        
        // Award a small XP bonus for redeeming (5% of cost, min 10)
        int bonusXp = Math.max(10, calculatedXpCost / 20);
        user.addXp(bonusXp);
    }
    
    private String getFeatureKey() {
        // Map reward name to feature flag
        Map<String, String> featureMap = Map.of(
            "Advanced Analytics", "feature.advanced_analytics",
            "Export Reports", "feature.export_reports",
            "Unlimited Tasks", "feature.unlimited_tasks",
            "Custom Categories", "feature.custom_categories"
        );
        return featureMap.getOrDefault(name, "feature." + name.toLowerCase().replace(" ", "_"));
    }
    
    private String getPowerUpEffect() {
        return "2x XP Boost";
    }
    
    // ==================== GETTERS AND SETTERS ====================
    
    public int getRewardId() { return rewardId; }
    public void setRewardId(int rewardId) { this.rewardId = rewardId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public RewardType getType() { return type; }
    public void setType(RewardType type) { this.type = type; }
    
    public Rarity getRarity() { return rarity; }
    public void setRarity(Rarity rarity) { 
        this.rarity = rarity; 
        this.calculatedXpCost = calculateActualCost();
    }
    
    public int getBaseXpCost() { return baseXpCost; }
    public void setBaseXpCost(int baseXpCost) { 
        this.baseXpCost = baseXpCost; 
        this.calculatedXpCost = calculateActualCost();
    }
    
    public int getCalculatedXpCost() { return calculatedXpCost; }
    
    public boolean isLimitedTime() { return isLimitedTime; }
    public void setLimitedTime(boolean limitedTime) { isLimitedTime = limitedTime; }
    
    public LocalDateTime getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDateTime availableFrom) { this.availableFrom = availableFrom; }
    
    public LocalDateTime getAvailableUntil() { return availableUntil; }
    public void setAvailableUntil(LocalDateTime availableUntil) { this.availableUntil = availableUntil; }
    
    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }
    
    public String getPreviewImagePath() { return previewImagePath; }
    public void setPreviewImagePath(String previewImagePath) { this.previewImagePath = previewImagePath; }
    
    public String getUnlockMessage() { return unlockMessage; }
    public void setUnlockMessage(String unlockMessage) { this.unlockMessage = unlockMessage; }
    
    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }
    
    public int getRequiredStreak() { return requiredStreak; }
    public void setRequiredStreak(int requiredStreak) { this.requiredStreak = requiredStreak; }
    
    public String[] getRequiredAchievements() { return requiredAchievements; }
    public void setRequiredAchievements(String[] requiredAchievements) { this.requiredAchievements = requiredAchievements; }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get color code based on rarity
     */
    public String getRarityColor() {
        return rarity.getColor();
    }
    
    /**
     * Get formatted cost string
     */
    public String getFormattedCost() {
        return String.format("%,d XP", calculatedXpCost);
    }
    
    /**
     * Get time remaining if limited time reward
     */
    public String getTimeRemaining() {
        if (!isLimitedTime || availableUntil == null) return "Always available";
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(availableUntil)) return "Expired";
        
        java.time.Duration duration = java.time.Duration.between(now, availableUntil);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        
        if (days > 0) return days + " days " + hours + " hours left";
        return hours + " hours left";
    }
    
    /**
     * Create a copy of this reward
     */
    public Reward copy() {
        Reward copy = new Reward();
        copy.rewardId = this.rewardId;
        copy.name = this.name;
        copy.description = this.description;
        copy.type = this.type;
        copy.rarity = this.rarity;
        copy.baseXpCost = this.baseXpCost;
        copy.calculatedXpCost = this.calculatedXpCost;
        copy.isLimitedTime = this.isLimitedTime;
        copy.availableFrom = this.availableFrom;
        copy.availableUntil = this.availableUntil;
        copy.iconPath = this.iconPath;
        copy.previewImagePath = this.previewImagePath;
        copy.unlockMessage = this.unlockMessage;
        copy.properties = new HashMap<>(this.properties);
        copy.createdAt = this.createdAt;
        copy.updatedAt = LocalDateTime.now();
        copy.isActive = this.isActive;
        copy.requiredLevel = this.requiredLevel;
        copy.requiredStreak = this.requiredStreak;
        copy.requiredAchievements = this.requiredAchievements != null ? this.requiredAchievements.clone() : null;
        return copy;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reward reward = (Reward) o;
        return rewardId == reward.rewardId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rewardId);
    }
    
    @Override
    public String toString() {
        return String.format("Reward{id=%d, name='%s', type=%s, rarity=%s, cost=%d XP}", 
            rewardId, name, type.getDisplayName(), rarity.getDisplayName(), calculatedXpCost);
    }
    
    // ==================== FACTORY METHODS FOR DEFAULT REWARDS ====================
    
    /**
     * Create default set of rewards for the system
     */
    public static List<Reward> createDefaultRewards() {
        List<Reward> rewards = new ArrayList<>();
        
        // Badges (Cosmetic)
        rewards.add(createBadge("Productivity Novice", "Complete 25 tasks", Rarity.COMMON, 100));
        rewards.add(createBadge("Productivity Pro", "Complete 100 tasks", Rarity.RARE, 500));
        rewards.add(createBadge("Productivity Master", "Complete 500 tasks", Rarity.EPIC, 2000));
        rewards.add(createBadge("Early Bird", "Complete 10 tasks before 9 AM", Rarity.UNCOMMON, 150));
        rewards.add(createBadge("Night Owl", "Complete 10 tasks after 10 PM", Rarity.UNCOMMON, 150));
        
        // Titles
        rewards.add(createTitle("Task Master", "\"Task Master\" title", Rarity.RARE, 750));
        rewards.add(createTitle("Focus Guru", "\"Focus Guru\" title", Rarity.EPIC, 1500));
        rewards.add(createTitle("XP Champion", "\"XP Champion\" title", Rarity.LEGENDARY, 5000));
        
        // Themes
        rewards.add(createTheme("Dark Mode Pro", "Enhanced dark theme", Rarity.UNCOMMON, 300));
        rewards.add(createTheme("Forest Theme", "Nature-inspired theme", Rarity.RARE, 600));
        rewards.add(createTheme("Midnight Blue", "Premium dark theme", Rarity.EPIC, 1200));
        
        // Features
        rewards.add(createFeature("Advanced Analytics", "Detailed productivity insights", Rarity.RARE, 1000));
        rewards.add(createFeature("Export Reports", "Export data to CSV/PDF", Rarity.UNCOMMON, 500));
        rewards.add(createFeature("Custom Categories", "Create your own task categories", Rarity.RARE, 800));
        rewards.add(createFeature("Unlimited Tasks", "Remove task limits", Rarity.EPIC, 2000));
        
        // Power Ups
        rewards.add(createPowerUp("2x XP Boost (24h)", "Double XP for 24 hours", Rarity.RARE, 500));
        rewards.add(createPowerUp("Streak Shield", "Protect streak for 3 days", Rarity.EPIC, 1000));
        
        // Avatars
        rewards.add(createAvatar("Ninja Avatar", "Stealthy ninja profile picture", Rarity.RARE, 400));
        rewards.add(createAvatar("Wizard Avatar", "Magic-themed avatar", Rarity.EPIC, 800));
        
        // Apply level requirements
        for (Reward r : rewards) {
            switch (r.getRarity()) {
                case COMMON -> r.setRequiredLevel(1);
                case UNCOMMON -> r.setRequiredLevel(5);
                case RARE -> r.setRequiredLevel(10);
                case EPIC -> r.setRequiredLevel(20);
                case LEGENDARY -> r.setRequiredLevel(50);
            }
        }
        
        return rewards;
    }
    
    private static Reward createBadge(String name, String description, Rarity rarity, int cost) {
        return new Reward(name, description, RewardType.BADGE, rarity, cost);
    }
    
    private static Reward createTitle(String name, String description, Rarity rarity, int cost) {
        return new Reward(name, description, RewardType.TITLE, rarity, cost);
    }
    
    private static Reward createTheme(String name, String description, Rarity rarity, int cost) {
        return new Reward(name, description, RewardType.THEME, rarity, cost);
    }
    
    private static Reward createFeature(String name, String description, Rarity rarity, int cost) {
        Reward reward = new Reward(name, description, RewardType.FEATURE, rarity, cost);
        reward.unlockMessage = "New feature unlocked: " + name + "! Check settings to configure.";
        return reward;
    }
    
    private static Reward createPowerUp(String name, String description, Rarity rarity, int cost) {
        Reward reward = new Reward(name, description, RewardType.POWER_UP, rarity, cost);
        reward.isLimitedTime = true;
        reward.availableUntil = LocalDateTime.now().plusDays(30); // Available for 30 days
        return reward;
    }
    
    private static Reward createAvatar(String name, String description, Rarity rarity, int cost) {
        return new Reward(name, description, RewardType.AVATAR, rarity, cost);
    }
}

// ==================== ADDITIONAL CLASSES ====================

/**
 * UserReward - Tracks redeemed rewards per user
 */
class UserReward {
    private int userRewardId;
    private int userId;
    private int rewardId;
    private LocalDateTime redeemedAt;
    private LocalDateTime equippedAt;
    private boolean isEquipped;
    private LocalDateTime expiresAt; // For temporary rewards like power-ups
    private Map<String, Object> customData;
    
    public UserReward() {
        this.redeemedAt = LocalDateTime.now();
        this.isEquipped = false;
        this.customData = new HashMap<>();
    }
    
    public UserReward(int userId, int rewardId) {
        this();
        this.userId = userId;
        this.rewardId = rewardId;
    }
    
    // Getters and Setters
    public int getUserRewardId() { return userRewardId; }
    public void setUserRewardId(int userRewardId) { this.userRewardId = userRewardId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getRewardId() { return rewardId; }
    public void setRewardId(int rewardId) { this.rewardId = rewardId; }
    
    public LocalDateTime getRedeemedAt() { return redeemedAt; }
    public void setRedeemedAt(LocalDateTime redeemedAt) { this.redeemedAt = redeemedAt; }
    
    public LocalDateTime getEquippedAt() { return equippedAt; }
    public void setEquippedAt(LocalDateTime equippedAt) { this.equippedAt = equippedAt; }
    
    public boolean isEquipped() { return isEquipped; }
    public void setEquipped(boolean equipped) { isEquipped = equipped; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public Map<String, Object> getCustomData() { return customData; }
    public void setCustomData(Map<String, Object> customData) { this.customData = customData; }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public long getTimeRemainingHours() {
        if (expiresAt == null) return -1;
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toHours();
    }
}

// Note: Add these imports at the top of your file:
// import java.time.LocalDateTime;
// import java.util.*;
