// RewardService.java
package com.synctrack.service;

import com.synctrack.model.*;
import com.synctrack.repository.RewardRepository;
import com.synctrack.repository.UserRewardRepository;
import com.synctrack.repository.UserRepository;
import com.synctrack.util.NotificationUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RewardService - Manages all reward-related operations including:
 * - Reward redemption
 * - User reward tracking
 * - XP spending and validation
 * - Reward effects application
 * - Seasonal/limited-time rewards
 * - Reward recommendations
 */
public class RewardService {
    
    // ==================== DEPENDENCIES ====================
    
    private final RewardRepository rewardRepository;
    private final UserRewardRepository userRewardRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;
    private final NotificationUtil notificationUtil;
    
    // ==================== CONSTANTS ====================
    
    private static final int MAX_REDEEMABLE_REWARDS_PER_DAY = 5;
    private static final int XP_REFUND_PERCENTAGE = 50; // For reward returns
    private static final int DAILY_REDEMPTION_LIMIT = 3;
    
    // ==================== CONSTRUCTORS ====================
    
    public RewardService() {
        this.rewardRepository = new RewardRepository();
        this.userRewardRepository = new UserRewardRepository();
        this.userRepository = new UserRepository();
        this.gamificationService = new GamificationService();
        this.notificationUtil = NotificationUtil.getInstance();
    }
    
    public RewardService(RewardRepository rewardRepository, UserRewardRepository userRewardRepository,
                        UserRepository userRepository, GamificationService gamificationService) {
        this.rewardRepository = rewardRepository;
        this.userRewardRepository = userRewardRepository;
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
        this.notificationUtil = NotificationUtil.getInstance();
    }
    
    // ==================== REWARD REDEMPTION ====================
    
    /**
     * Redeem a reward for a user
     * @param userId User ID
     * @param rewardId Reward ID to redeem
     * @return RedemptionResult with details of the transaction
     */
    public RedemptionResult redeemReward(int userId, int rewardId) {
        // Validate inputs
        if (userId <= 0 || rewardId <= 0) {
            return RedemptionResult.failure("Invalid user or reward ID");
        }
        
        // Fetch user and reward
        User user = userRepository.findById(userId);
        if (user == null) {
            return RedemptionResult.failure("User not found");
        }
        
        Reward reward = rewardRepository.findById(rewardId);
        if (reward == null) {
            return RedemptionResult.failure("Reward not found");
        }
        
        // Check if reward is active
        if (!reward.isActive()) {
            return RedemptionResult.failure("This reward is no longer available");
        }
        
        // Check if reward is available (time-limited)
        if (!reward.isAvailable()) {
            return RedemptionResult.failure("This reward is not currently available");
        }
        
        // Check if user already has this reward
        if (hasUserRedeemedReward(userId, rewardId)) {
            return RedemptionResult.failure("You have already redeemed this reward");
        }
        
        // Check daily redemption limit
        int todayRedemptions = getUserDailyRedemptionCount(userId);
        if (todayRedemptions >= DAILY_REDEMPTION_LIMIT) {
            return RedemptionResult.failure("Daily redemption limit reached (" + DAILY_REDEMPTION_LIMIT + " per day)");
        }
        
        // Check if user meets requirements
        List<String> userAchievements = gamificationService.getUserAchievementNames(userId);
        if (!reward.meetsRequirements(user, userAchievements)) {
            StringBuilder requirements = new StringBuilder("Requirements not met: ");
            if (user.getLevel() < reward.getRequiredLevel()) {
                requirements.append("Level ").append(reward.getRequiredLevel()).append(" required. ");
            }
            if (user.getCurrentStreak() < reward.getRequiredStreak()) {
                requirements.append(reward.getRequiredStreak()).append("-day streak required. ");
            }
            return RedemptionResult.failure(requirements.toString());
        }
        
        // Check if user has enough XP
        int cost = reward.getCalculatedXpCost();
        if (user.getXpTotal() < cost) {
            int xpNeeded = cost - user.getXpTotal();
            return RedemptionResult.failure("Insufficient XP. Need " + xpNeeded + " more XP");
        }
        
        // Process redemption
        try {
            // Deduct XP
            user.addXp(-cost);
            userRepository.update(user);
            
            // Create user reward record
            UserReward userReward = new UserReward(userId, rewardId);
            
            // Set expiration for temporary rewards
            if (reward.getType() == Reward.RewardType.POWER_UP) {
                userReward.setExpiresAt(LocalDateTime.now().plusDays(30));
            }
            
            userReward = userRewardRepository.save(userReward);
            
            // Apply reward effects
            applyRewardEffect(user, reward, userReward);
            
            // Log redemption
            logRedemption(userId, rewardId, cost);
            
            // Send notification
            notificationUtil.sendRewardRedeemedNotification(user, reward);
            
            // Check for reward-related achievements
            checkRewardAchievements(userId);
            
            return RedemptionResult.success(userReward, cost);
            
        } catch (Exception e) {
            // Rollback: Refund XP if something went wrong
            user.addXp(cost);
            userRepository.update(user);
            return RedemptionResult.failure("Redemption failed: " + e.getMessage());
        }
    }
    
    /**
     * Apply reward effects to user
     */
    private void applyRewardEffect(User user, Reward reward, UserReward userReward) {
        switch (reward.getType()) {
            case TITLE:
                // Set custom title
                String title = reward.getProperties().getOrDefault("titleValue", reward.getName()).toString();
                user.setCustomTitle(title);
                userRepository.update(user);
                break;
                
            case THEME:
                // Apply theme
                String themeName = reward.getName().toLowerCase().replace(" ", "_");
                user.setThemePreference(themeName);
                userRepository.update(user);
                break;
                
            case FEATURE:
                // Unlock feature flag
                String featureKey = getFeatureKey(reward);
                user.unlockFeature(featureKey);
                userRepository.update(user);
                break;
                
            case POWER_UP:
                // Apply power-up effect
                applyPowerUpEffect(user, reward, userReward);
                break;
                
            case AVATAR:
                // Set avatar
                user.setAvatarIcon(reward.getIconPath());
                userRepository.update(user);
                break;
                
            case BADGE:
            case SOUND_PACK:
            case BACKGROUND:
            default:
                // Cosmetic items - just record ownership
                break;
        }
    }
    
    /**
     * Apply power-up effect to user
     */
    private void applyPowerUpEffect(User user, Reward reward, UserReward userReward) {
        String powerUpType = reward.getProperties().getOrDefault("powerUpType", "xp_boost").toString();
        
        switch (powerUpType) {
            case "xp_boost":
                double multiplier = (double) reward.getProperties().getOrDefault("multiplier", 2.0);
                user.setXpMultiplier(multiplier);
                user.setXpMultiplierExpiry(userReward.getExpiresAt());
                break;
                
            case "streak_shield":
                user.setStreakShield(true);
                user.setStreakShieldExpiry(userReward.getExpiresAt());
                break;
                
            case "time_boost":
                // Reduce estimated time requirements
                user.setTimeBoostActive(true);
                user.setTimeBoostExpiry(userReward.getExpiresAt());
                break;
        }
        userRepository.update(user);
    }
    
    // ==================== REWARD QUERIES ====================
    
    /**
     * Get all available rewards for a user
     */
    public List<Reward> getAvailableRewards(int userId) {
        User user = userRepository.findById(userId);
        if (user == null) return new ArrayList<>();
        
        List<String> userAchievements = gamificationService.getUserAchievementNames(userId);
        List<Integer> redeemedRewardIds = getRedeemedRewardIds(userId);
        
        return rewardRepository.findAll().stream()
            .filter(Reward::isActive)
            .filter(Reward::isAvailable)
            .filter(reward -> !redeemedRewardIds.contains(reward.getRewardId()))
            .filter(reward -> reward.meetsRequirements(user, userAchievements))
            .sorted(Comparator.comparingInt(Reward::getCalculatedXpCost))
            .collect(Collectors.toList());
    }
    
    /**
     * Get rewards that user can afford (but may not meet requirements)
     */
    public List<Reward> getAffordableRewards(int userId) {
        User user = userRepository.findById(userId);
        if (user == null) return new ArrayList<>();
        
        List<Integer> redeemedRewardIds = getRedeemedRewardIds(userId);
        
        return rewardRepository.findAll().stream()
            .filter(Reward::isActive)
            .filter(Reward::isAvailable)
            .filter(reward -> !redeemedRewardIds.contains(reward.getRewardId()))
            .filter(reward -> reward.getCalculatedXpCost() <= user.getXpTotal())
            .sorted(Comparator.comparingInt(Reward::getCalculatedXpCost))
            .collect(Collectors.toList());
    }
    
    /**
     * Get user's redeemed rewards
     */
    public List<UserReward> getUserRewards(int userId) {
        return userRewardRepository.findByUserId(userId);
    }
    
    /**
     * Get user's currently equipped rewards
     */
    public List<UserReward> getEquippedRewards(int userId) {
        return userRewardRepository.findByUserId(userId).stream()
            .filter(UserReward::isEquipped)
            .collect(Collectors.toList());
    }
    
    /**
     * Get recommended rewards for user based on preferences and activity
     */
    public List<Reward> getRecommendedRewards(int userId, int limit) {
        User user = userRepository.findById(userId);
        if (user == null) return new ArrayList<>();
        
        List<Reward> availableRewards = getAvailableRewards(userId);
        List<Integer> redeemedIds = getRedeemedRewardIds(userId);
        
        // Score each reward based on user's activity
        Map<Reward, Double> scoredRewards = new HashMap<>();
        
        for (Reward reward : availableRewards) {
            if (redeemedIds.contains(reward.getRewardId())) continue;
            
            double score = calculateRecommendationScore(user, reward);
            scoredRewards.put(reward, score);
        }
        
        return scoredRewards.entrySet().stream()
            .sorted(Map.Entry.<Reward, Double>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate recommendation score for a reward based on user behavior
     */
    private double calculateRecommendationScore(User user, Reward reward) {
        double score = 0.0;
        
        // Cost factor - cheaper rewards recommended more
        score += (1000.0 / (reward.getCalculatedXpCost() + 100)) * 0.3;
        
        // Rarity factor
        switch (reward.getRarity()) {
            case LEGENDARY: score += 10; break;
            case EPIC: score += 8; break;
            case RARE: score += 5; break;
            case UNCOMMON: score += 3; break;
            default: score += 1;
        }
        
        // Type preference based on user's past redemptions
        List<UserReward> userRewards = getUserRewards(user.getUserId());
        Map<Reward.RewardType, Long> typeCount = userRewards.stream()
            .map(ur -> rewardRepository.findById(ur.getRewardId()))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Reward::getType, Collectors.counting()));
        
        // Prefer types user hasn't collected much of
        long countOfType = typeCount.getOrDefault(reward.getType(), 0L);
        score += (10 - Math.min(10, countOfType)) * 2;
        
        // Limited time rewards get boost
        if (reward.isLimitedTime()) {
            score += 15;
        }
        
        return score;
    }
    
    // ==================== REWARD MANAGEMENT ====================
    
    /**
     * Equip a reward (for cosmetic items like titles, avatars, themes)
     */
    public boolean equipReward(int userId, int userRewardId) {
        UserReward userReward = userRewardRepository.findById(userRewardId);
        if (userReward == null || userReward.getUserId() != userId) {
            return false;
        }
        
        Reward reward = rewardRepository.findById(userReward.getRewardId());
        if (reward == null) return false;
        
        // Unequip previous reward of same type
        unequipRewardsOfType(userId, reward.getType());
        
        // Equip new reward
        userReward.setEquipped(true);
        userReward.setEquippedAt(LocalDateTime.now());
        userRewardRepository.update(userReward);
        
        // Apply effect
        User user = userRepository.findById(userId);
        applyRewardEffect(user, reward, userReward);
        
        return true;
    }
    
    /**
     * Unequip all rewards of a specific type
     */
    private void unequipRewardsOfType(int userId, Reward.RewardType type) {
        List<UserReward> userRewards = getUserRewards(userId);
        
        for (UserReward ur : userRewards) {
            Reward r = rewardRepository.findById(ur.getRewardId());
            if (r != null && r.getType() == type && ur.isEquipped()) {
                ur.setEquipped(false);
                userRewardRepository.update(ur);
            }
        }
    }
    
    /**
     * Check and clean up expired power-ups
     */
    public void cleanupExpiredRewards() {
        List<UserReward> allRewards = userRewardRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        for (UserReward reward : allRewards) {
            if (reward.getExpiresAt() != null && reward.getExpiresAt().isBefore(now)) {
                // Remove power-up effects
                User user = userRepository.findById(reward.getUserId());
                if (user != null) {
                    removePowerUpEffects(user, reward);
                }
                // Optionally delete or mark as expired
                reward.setExpiresAt(null);
                userRewardRepository.update(reward);
            }
        }
    }
    
    /**
     * Remove power-up effects from user
     */
    private void removePowerUpEffects(User user, UserReward reward) {
        Reward rewardDef = rewardRepository.findById(reward.getRewardId());
        if (rewardDef != null && rewardDef.getType() == Reward.RewardType.POWER_UP) {
            user.setXpMultiplier(1.0);
            user.setXpMultiplierExpiry(null);
            user.setStreakShield(false);
            user.setStreakShieldExpiry(null);
            userRepository.update(user);
        }
    }
    
    /**
     * Return a reward for partial XP refund
     */
    public RedemptionResult returnReward(int userId, int userRewardId) {
        UserReward userReward = userRewardRepository.findById(userRewardId);
        if (userReward == null || userReward.getUserId() != userId) {
            return RedemptionResult.failure("Reward not found");
        }
        
        Reward reward = rewardRepository.findById(userReward.getRewardId());
        if (reward == null) {
            return RedemptionResult.failure("Reward definition not found");
        }
        
        // Check if reward can be returned (non-cosmetic or within return window)
        LocalDateTime returnDeadline = userReward.getRedeemedAt().plusDays(7);
        if (LocalDateTime.now().isAfter(returnDeadline)) {
            return RedemptionResult.failure("Return window has expired (7 days)");
        }
        
        // Calculate refund amount
        int refundXp = (reward.getCalculatedXpCost() * XP_REFUND_PERCENTAGE) / 100;
        
        // Refund XP
        User user = userRepository.findById(userId);
        user.addXp(refundXp);
        userRepository.update(user);
        
        // Remove reward
        userRewardRepository.delete(userRewardId);
        
        // Remove effects
        removeRewardEffects(user, reward);
        
        return RedemptionResult.success(null, refundXp);
    }
    
    /**
     * Remove reward effects from user
     */
    private void removeRewardEffects(User user, Reward reward) {
        switch (reward.getType()) {
            case TITLE:
                user.setCustomTitle(null);
                break;
            case THEME:
                user.setThemePreference("light");
                break;
            case POWER_UP:
                removePowerUpEffects(user, null);
                break;
        }
        userRepository.update(user);
    }
    
    // ==================== ADMIN FUNCTIONS ====================
    
    /**
     * Create a new reward (admin only)
     */
    public Reward createReward(Reward reward) {
        reward.setCreatedAt(LocalDateTime.now());
        reward.setUpdatedAt(LocalDateTime.now());
        return rewardRepository.save(reward);
    }
    
    /**
     * Update an existing reward (admin only)
     */
    public Reward updateReward(Reward reward) {
        reward.setUpdatedAt(LocalDateTime.now());
        return rewardRepository.update(reward);
    }
    
    /**
     * Delete a reward (admin only)
     */
    public boolean deleteReward(int rewardId) {
        // Check if any user has redeemed this reward
        List<UserReward> redemptions = userRewardRepository.findByRewardId(rewardId);
        if (!redemptions.isEmpty()) {
            return false; // Can't delete rewards that have been redeemed
        }
        return rewardRepository.delete(rewardId);
    }
    
    /**
     * Create seasonal/limited-time reward
     */
    public Reward createSeasonalReward(String name, String description, Reward.RewardType type,
                                       Reward.Rarity rarity, int baseXpCost,
                                       LocalDateTime availableUntil) {
        Reward reward = new Reward(name, description, type, rarity, baseXpCost);
        reward.setLimitedTime(true);
        reward.setAvailableUntil(availableUntil);
        reward.setAvailableFrom(LocalDateTime.now());
        return createReward(reward);
    }
    
    // ==================== STATISTICS & REPORTING ====================
    
    /**
     * Get reward redemption statistics
     */
    public RewardStatistics getRewardStatistics() {
        List<Reward> allRewards = rewardRepository.findAll();
        List<UserReward> allRedemptions = userRewardRepository.findAll();
        
        Map<Integer, Long> redemptionCounts = allRedemptions.stream()
            .collect(Collectors.groupingBy(UserReward::getRewardId, Collectors.counting()));
        
        Reward mostPopular = null;
        long maxRedemptions = 0;
        
        for (Reward reward : allRewards) {
            long count = redemptionCounts.getOrDefault(reward.getRewardId(), 0L);
            if (count > maxRedemptions) {
                maxRedemptions = count;
                mostPopular = reward;
            }
        }
        
        return new RewardStatistics(
            allRewards.size(),
            allRedemptions.size(),
            mostPopular,
            maxRedemptions,
            calculateTotalXPSpent(allRedemptions),
            getRedemptionsByType(allRedemptions),
            getRedemptionsByRarity(allRedemptions)
        );
    }
    
    /**
     * Get user's reward statistics
     */
    public UserRewardStatistics getUserRewardStatistics(int userId) {
        List<UserReward> userRewards = getUserRewards(userId);
        
        int totalSpent = userRewards.stream()
            .mapToInt(ur -> {
                Reward r = rewardRepository.findById(ur.getRewardId());
                return r != null ? r.getCalculatedXpCost() : 0;
            })
            .sum();
        
        Map<Reward.RewardType, Long> byType = userRewards.stream()
            .map(ur -> rewardRepository.findById(ur.getRewardId()))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Reward::getType, Collectors.counting()));
        
        return new UserRewardStatistics(
            userRewards.size(),
            totalSpent,
            byType,
            userRewards.stream().filter(UserReward::isEquipped).count()
        );
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Check if user has already redeemed a specific reward
     */
    private boolean hasUserRedeemedReward(int userId, int rewardId) {
        return userRewardRepository.findByUserId(userId).stream()
            .anyMatch(ur -> ur.getRewardId() == rewardId);
    }
    
    /**
     * Get IDs of rewards user has redeemed
     */
    private List<Integer> getRedeemedRewardIds(int userId) {
        return userRewardRepository.findByUserId(userId).stream()
            .map(UserReward::getRewardId)
            .collect(Collectors.toList());
    }
    
    /**
     * Get user's daily redemption count
     */
    private int getUserDailyRedemptionCount(int userId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return (int) userRewardRepository.findByUserId(userId).stream()
            .filter(ur -> ur.getRedeemedAt().isAfter(startOfDay))
            .count();
    }
    
    /**
     * Get feature key from reward properties
     */
    private String getFeatureKey(Reward reward) {
        return reward.getProperties().getOrDefault("featureKey", 
            "feature." + reward.getName().toLowerCase().replace(" ", "_")).toString();
    }
    
    /**
     * Log reward redemption for analytics
     */
    private void logRedemption(int userId, int rewardId, int xpSpent) {
        // In production, this would write to an audit log
        System.out.printf("[REWARD] User %d redeemed reward %d for %d XP at %s%n",
            userId, rewardId, xpSpent, LocalDateTime.now());
    }
    
    /**
     * Check and unlock reward-related achievements
     */
    private void checkRewardAchievements(int userId) {
        int totalRedeemed = getUserRewards(userId).size();
        
        // Check for reward collector achievements
        if (totalRedeemed == 1) {
            gamificationService.unlockAchievement(userId, "first_reward");
        } else if (totalRedeemed == 10) {
            gamificationService.unlockAchievement(userId, "reward_collector");
        } else if (totalRedeemed == 50) {
            gamificationService.unlockAchievement(userId, "reward_master");
        }
    }
    
    /**
     * Calculate total XP spent across all redemptions
     */
    private int calculateTotalXPSpent(List<UserReward> redemptions) {
        return redemptions.stream()
            .mapToInt(ur -> {
                Reward r = rewardRepository.findById(ur.getRewardId());
                return r != null ? r.getCalculatedXpCost() : 0;
            })
            .sum();
    }
    
    /**
     * Get redemption counts by reward type
     */
    private Map<Reward.RewardType, Long> getRedemptionsByType(List<UserReward> redemptions) {
        return redemptions.stream()
            .map(ur -> rewardRepository.findById(ur.getRewardId()))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Reward::getType, Collectors.counting()));
    }
    
    /**
     * Get redemption counts by rarity
     */
    private Map<Reward.Rarity, Long> getRedemptionsByRarity(List<UserReward> redemptions) {
        return redemptions.stream()
            .map(ur -> rewardRepository.findById(ur.getRewardId()))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Reward::getRarity, Collectors.counting()));
    }
    
    // ==================== RESULT CLASSES ====================
    
    /**
     * Redemption result wrapper
     */
    public static class RedemptionResult {
        private final boolean success;
        private final String message;
        private final UserReward userReward;
        private final int xpSpent;
        
        private RedemptionResult(boolean success, String message, UserReward userReward, int xpSpent) {
            this.success = success;
            this.message = message;
            this.userReward = userReward;
            this.xpSpent = xpSpent;
        }
        
        public static RedemptionResult success(UserReward userReward, int xpSpent) {
            return new RedemptionResult(true, "Reward redeemed successfully!", userReward, xpSpent);
        }
        
        public static RedemptionResult failure(String message) {
            return new RedemptionResult(false, message, null, 0);
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public UserReward getUserReward() { return userReward; }
        public int getXpSpent() { return xpSpent; }
    }
    
    /**
     * Reward statistics wrapper
     */
    public static class RewardStatistics {
        private final int totalRewards;
        private final int totalRedemptions;
        private final Reward mostPopularReward;
        private final long mostPopularRedemptions;
        private final int totalXPSpent;
        private final Map<Reward.RewardType, Long> redemptionsByType;
        private final Map<Reward.Rarity, Long> redemptionsByRarity;
        
        public RewardStatistics(int totalRewards, int totalRedemptions, Reward mostPopularReward,
                                long mostPopularRedemptions, int totalXPSpent,
                                Map<Reward.RewardType, Long> redemptionsByType,
                                Map<Reward.Rarity, Long> redemptionsByRarity) {
            this.totalRewards = totalRewards;
            this.totalRedemptions = totalRedemptions;
            this.mostPopularReward = mostPopularReward;
            this.mostPopularRedemptions = mostPopularRedemptions;
            this.totalXPSpent = totalXPSpent;
            this.redemptionsByType = redemptionsByType;
            this.redemptionsByRarity = redemptionsByRarity;
        }
        
        // Getters
        public int getTotalRewards() { return totalRewards; }
        public int getTotalRedemptions() { return totalRedemptions; }
        public Reward getMostPopularReward() { return mostPopularReward; }
        public long getMostPopularRedemptions() { return mostPopularRedemptions; }
        public int getTotalXPSpent() { return totalXPSpent; }
        public Map<Reward.RewardType, Long> getRedemptionsByType() { return redemptionsByType; }
        public Map<Reward.Rarity, Long> getRedemptionsByRarity() { return redemptionsByRarity; }
    }
    
    /**
     * User reward statistics wrapper
     */
    public static class UserRewardStatistics {
        private final int totalRewardsRedeemed;
        private final int totalXPSpent;
        private final Map<Reward.RewardType, Long> redemptionsByType;
        private final long equippedCount;
        
        public UserRewardStatistics(int totalRewardsRedeemed, int totalXPSpent,
                                    Map<Reward.RewardType, Long> redemptionsByType, long equippedCount) {
            this.totalRewardsRedeemed = totalRewardsRedeemed;
            this.totalXPSpent = totalXPSpent;
            this.redemptionsByType = redemptionsByType;
            this.equippedCount = equippedCount;
        }
        
        // Getters
        public int getTotalRewardsRedeemed() { return totalRewardsRedeemed; }
        public int getTotalXPSpent() { return totalXPSpent; }
        public Map<Reward.RewardType, Long> getRedemptionsByType() { return redemptionsByType; }
        public long getEquippedCount() { return equippedCount; }
    }
}

// ==================== REPOSITORY INTERFACES ====================

/**
 * Repository interface for Reward data access
 */
interface RewardRepository {
    Reward save(Reward reward);
    Reward update(Reward reward);
    Reward findById(int rewardId);
    List<Reward> findAll();
    List<Reward> findByType(Reward.RewardType type);
    List<Reward> findByRarity(Reward.Rarity rarity);
    List<Reward> findAvailableRewards();
    List<Reward> findLimitedTimeRewards();
    boolean delete(int rewardId);
}

/**
 * Repository interface for UserReward data access
 */
interface UserRewardRepository {
    UserReward save(UserReward userReward);
    UserReward update(UserReward userReward);
    UserReward findById(int userRewardId);
    List<UserReward> findByUserId(int userId);
    List<UserReward> findByRewardId(int rewardId);
    List<UserReward> findAll();
    void delete(int userRewardId);
    void deleteByUserId(int userId);
}

// Note: Add these imports at the top of your file:
// import java.time.LocalDateTime;
// import java.util.*;
// import java.util.stream.Collectors;
