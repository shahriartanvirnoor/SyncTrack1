// UserAchievement.java
package com.synctrack.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * UserAchievement Model - Tracks individual user's progress and completion status for achievements
 * Handles progress tracking, milestone detection, and achievement unlocking logic
 */
public class UserAchievement {
    
    // ==================== ENUMS ====================
    
    /**
     * Current state of the achievement for this user
     */
    public enum AchievementState {
        NOT_STARTED("⚪", "Not Started", 0, "Begin working on this achievement"),
        IN_PROGRESS("🟡", "In Progress", 1, "Keep going! You're making progress"),
        ALMOST_THERE("🟠", "Almost There", 2, "Just a little more to go!"),
        COMPLETED("✅", "Completed", 3, "Achievement unlocked! 🎉"),
        CLAIMED("🎁", "Reward Claimed", 4, "XP reward collected");
        
        private final String icon;
        private final String displayName;
        private final int priority;
        private final String encouragement;
        
        AchievementState(String icon, String displayName, int priority, String encouragement) {
            this.icon = icon;
            this.displayName = displayName;
            this.priority = priority;
            this.encouragement = encouragement;
        }
        
        public String getIcon() { return icon; }
        public String getDisplayName() { return displayName; }
        public int getPriority() { return priority; }
        public String getEncouragement() { return encouragement; }
        
        public static AchievementState fromProgress(double progress) {
            if (progress >= 1.0) return COMPLETED;
            if (progress >= 0.75) return ALMOST_THERE;
            if (progress > 0) return IN_PROGRESS;
            return NOT_STARTED;
        }
    }
    
    /**
     * Types of achievement criteria for specialized handling
     */
    public enum CriteriaType {
        TASKS_COMPLETED("tasks_completed", "Complete {0} tasks", "task"),
        STREAK_DAYS("streak_days", "Maintain a {0}-day streak", "calendar"),
        TOTAL_XP("total_xp", "Earn {0} total XP", "star"),
        TIME_SPENT("time_spent", "Spend {0} hours working", "clock"),
        PERFECT_DAY("perfect_day", "Complete {0} perfect days (all tasks done on time)", "crown"),
        CATEGORY_MASTER("category_master", "Complete {0} tasks in {1} category", "folder"),
        DIFFICULTY_CONQUEROR("difficulty_conqueror", "Complete {0} {1} difficulty tasks", "chart"),
        SPEED_DEMON("speed_demon", "Complete {0} tasks under estimated time", "rocket"),
        NIGHT_OWL("night_owl", "Complete {0} tasks after 10 PM", "moon"),
        EARLY_BIRD("early_bird", "Complete {0} tasks before 6 AM", "sun"),
        CONSISTENCY_KING("consistency_king", "Complete tasks for {0} consecutive weeks", "trophy");
        
        private final String key;
        private final String template;
        private final String icon;
        
        CriteriaType(String key, String template, String icon) {
            this.key = key;
            this.template = template;
            this.icon = icon;
        }
        
        public String getKey() { return key; }
        public String getTemplate() { return template; }
        public String getIcon() { return icon; }
        
        public static CriteriaType fromKey(String key) {
            for (CriteriaType type : values()) {
                if (type.key.equals(key)) return type;
            }
            return TASKS_COMPLETED;
        }
    }
    
    /**
     * Milestone events that trigger special notifications
     */
    public enum MilestoneType {
        FIRST_STEP(10, "First step completed!"),
        QUARTER_WAY(25, "25% complete - You're on your way!"),
        HALFWAY(50, "Halfway there! Keep pushing!"),
        THREE_QUARTERS(75, "75% complete - Almost there!"),
        NEARLY_THERE(90, "90% complete! Just a little more!"),
        COMPLETE(100, "Achievement unlocked! 🎉");
        
        private final int percent;
        private final String message;
        
        MilestoneType(int percent, String message) {
            this.percent = percent;
            this.message = message;
        }
        
        public int getPercent() { return percent; }
        public String getMessage() { return message; }
        
        public static MilestoneType fromPercent(int percent) {
            for (MilestoneType milestone : values()) {
                if (milestone.percent == percent) return milestone;
            }
            return null;
        }
    }
    
    // ==================== FIELDS ====================
    
    private int userAchievementId;
    private int userId;
    private int achievementId;
    private LocalDateTime unlockedAt;
    private LocalDateTime claimedAt;
    private LocalDateTime lastProgressUpdate;
    private int progressCurrent;
    private int targetValue;
    private boolean isCompleted;
    private boolean isRewardClaimed;
    private int timesAchieved; // For repeatable achievements
    private double bestProgress; // Track highest progress achieved
    private String notes;
    private Map<String, Object> progressDetails; // Additional context-specific progress data
    private List<Milestone> milestonesReached;
    private LocalDateTime estimatedCompletionDate;
    private int weeklyProgress; // Progress made in current week
    private int dailyProgress; // Progress made today
    
    // ==================== NESTED CLASSES ====================
    
    /**
     * Milestone record for tracking progress milestones
     */
    public static class Milestone {
        private final MilestoneType type;
        private final LocalDateTime reachedAt;
        private final int progressAtMilestone;
        
        public Milestone(MilestoneType type, LocalDateTime reachedAt, int progressAtMilestone) {
            this.type = type;
            this.reachedAt = reachedAt;
            this.progressAtMilestone = progressAtMilestone;
        }
        
        public MilestoneType getType() { return type; }
        public LocalDateTime getReachedAt() { return reachedAt; }
        public int getProgressAtMilestone() { return progressAtMilestone; }
    }
    
    /**
     * Progress snapshot for trend analysis
     */
    public static class ProgressSnapshot {
        private final LocalDateTime timestamp;
        private final int progress;
        private final int dailyChange;
        
        public ProgressSnapshot(LocalDateTime timestamp, int progress, int dailyChange) {
            this.timestamp = timestamp;
            this.progress = progress;
            this.dailyChange = dailyChange;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getProgress() { return progress; }
        public int getDailyChange() { return dailyChange; }
    }
    
    // ==================== CONSTRUCTORS ====================
    
    public UserAchievement() {
        this.unlockedAt = null;
        this.claimedAt = null;
        this.lastProgressUpdate = LocalDateTime.now();
        this.progressCurrent = 0;
        this.isCompleted = false;
        this.isRewardClaimed = false;
        this.timesAchieved = 0;
        this.bestProgress = 0;
        this.progressDetails = new HashMap<>();
        this.milestonesReached = new ArrayList<>();
        this.weeklyProgress = 0;
        this.dailyProgress = 0;
    }
    
    public UserAchievement(int userId, int achievementId, int targetValue) {
        this();
        this.userId = userId;
        this.achievementId = achievementId;
        this.targetValue = targetValue;
    }
    
    public UserAchievement(int userId, int achievementId, int targetValue, int initialProgress) {
        this(userId, achievementId, targetValue);
        this.progressCurrent = Math.min(initialProgress, targetValue);
        this.isCompleted = this.progressCurrent >= targetValue;
        updateState();
    }
    
    // ==================== BUSINESS LOGIC METHODS ====================
    
    /**
     * Update progress and check for completion
     */
    public boolean updateProgress(int newProgress) {
        int oldProgress = this.progressCurrent;
        this.progressCurrent = Math.min(newProgress, targetValue);
        this.lastProgressUpdate = LocalDateTime.now();
        
        // Track best progress
        if (this.progressCurrent > bestProgress) {
            this.bestProgress = this.progressCurrent;
        }
        
        // Calculate daily/weekly delta
        int delta = this.progressCurrent - oldProgress;
        this.dailyProgress += delta;
        this.weeklyProgress += delta;
        
        // Check for completion
        if (!isCompleted && this.progressCurrent >= targetValue) {
            complete();
            return true;
        }
        
        // Check for milestones
        checkMilestones(oldProgress, this.progressCurrent);
        
        updateState();
        return false;
    }
    
    /**
     * Increment progress by a certain amount
     */
    public boolean addProgress(int amount) {
        return updateProgress(progressCurrent + amount);
    }
    
    /**
     * Complete the achievement
     */
    public void complete() {
        this.isCompleted = true;
        this.unlockedAt = LocalDateTime.now();
        this.progressCurrent = targetValue;
        
        // Add completion milestone
        milestonesReached.add(new Milestone(
            MilestoneType.COMPLETE, 
            unlockedAt, 
            progressCurrent
        ));
        
        updateState();
    }
    
    /**
     * Claim XP reward for completed achievement
     */
    public void claimReward() {
        if (isCompleted && !isRewardClaimed) {
            this.isRewardClaimed = true;
            this.claimedAt = LocalDateTime.now();
            this.timesAchieved++;
        }
    }
    
    /**
     * Reset achievement (for repeatable achievements)
     */
    public void reset() {
        this.progressCurrent = 0;
        this.isCompleted = false;
        this.isRewardClaimed = false;
        this.unlockedAt = null;
        this.claimedAt = null;
        this.milestonesReached.clear();
        this.dailyProgress = 0;
        this.weeklyProgress = 0;
        updateState();
    }
    
    /**
     * Reset daily progress counter
     */
    public void resetDailyProgress() {
        this.dailyProgress = 0;
    }
    
    /**
     * Reset weekly progress counter
     */
    public void resetWeeklyProgress() {
        this.weeklyProgress = 0;
    }
    
    /**
     * Get current progress percentage
     */
    public double getProgressPercentage() {
        if (targetValue == 0) return 0;
        return Math.min(1.0, (double) progressCurrent / targetValue);
    }
    
    /**
     * Get formatted progress string (e.g., "25/100")
     */
    public String getProgressString() {
        return progressCurrent + " / " + targetValue;
    }
    
    /**
     * Get progress bar representation
     */
    public String getProgressBar(int width) {
        int filled = (int)(getProgressPercentage() * width);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("]");
        return bar.toString();
    }
    
    /**
     * Get current achievement state based on progress
     */
    public AchievementState getCurrentState() {
        if (isCompleted && isRewardClaimed) return AchievementState.CLAIMED;
        if (isCompleted) return AchievementState.COMPLETED;
        return AchievementState.fromProgress(getProgressPercentage());
    }
    
    /**
     * Get encouragement message based on current progress
     */
    public String getEncouragementMessage() {
        AchievementState state = getCurrentState();
        if (state == AchievementState.COMPLETED) {
            return "Congratulations! Don't forget to claim your XP reward!";
        }
        if (state == AchievementState.ALMOST_THERE) {
            return "Almost there! Just " + (targetValue - progressCurrent) + " more to go!";
        }
        if (state == AchievementState.IN_PROGRESS) {
            int remaining = targetValue - progressCurrent;
            return "You're making great progress! Only " + remaining + " remaining.";
        }
        return "Start working on this achievement today!";
    }
    
    /**
     * Estimate time to completion based on recent progress rate
     */
    public LocalDateTime estimateCompletionDate() {
        if (isCompleted) return unlockedAt;
        
        // Calculate daily progress rate from last 7 days
        double dailyRate = weeklyProgress / 7.0;
        if (dailyRate <= 0) {
            estimatedCompletionDate = null;
            return null;
        }
        
        int remaining = targetValue - progressCurrent;
        long daysNeeded = (long) Math.ceil(remaining / dailyRate);
        estimatedCompletionDate = LocalDateTime.now().plusDays(daysNeeded);
        return estimatedCompletionDate;
    }
    
    /**
     * Check and record milestone achievements
     */
    private void checkMilestones(int oldProgress, int newProgress) {
        int oldPercent = (int)((double)oldProgress / targetValue * 100);
        int newPercent = (int)((double)newProgress / targetValue * 100);
        
        MilestoneType[] milestones = {MilestoneType.FIRST_STEP, MilestoneType.QUARTER_WAY, 
                                       MilestoneType.HALFWAY, MilestoneType.THREE_QUARTERS, 
                                       MilestoneType.NEARLY_THERE};
        
        for (MilestoneType milestone : milestones) {
            if (oldPercent < milestone.getPercent() && newPercent >= milestone.getPercent()) {
                boolean alreadyRecorded = milestonesReached.stream()
                    .anyMatch(m -> m.getType() == milestone);
                if (!alreadyRecorded) {
                    milestonesReached.add(new Milestone(milestone, LocalDateTime.now(), newProgress));
                }
            }
        }
    }
    
    /**
     * Update any derived state
     */
    private void updateState() {
        // Override for any state management needed
    }
    
    /**
     * Get next milestone to achieve
     */
    public MilestoneType getNextMilestone() {
        int currentPercent = (int)(getProgressPercentage() * 100);
        
        MilestoneType[] milestones = {MilestoneType.FIRST_STEP, MilestoneType.QUARTER_WAY,
                                       MilestoneType.HALFWAY, MilestoneType.THREE_QUARTERS,
                                       MilestoneType.NEARLY_THERE, MilestoneType.COMPLETE};
        
        for (MilestoneType milestone : milestones) {
            if (currentPercent < milestone.getPercent()) {
                return milestone;
            }
        }
        return null;
    }
    
    /**
     * Calculate progress trend (improving, stable, declining)
     */
    public String getTrend() {
        if (weeklyProgress > dailyProgress * 3) {
            return "🚀 Accelerating";
        } else if (dailyProgress > 0) {
            return "📈 Improving";
        } else if (dailyProgress == 0 && weeklyProgress == 0) {
            return "⏸ Stalled";
        }
        return "📊 Steady";
    }
    
    /**
     * Get time since last progress update
     */
    public String getTimeSinceLastUpdate() {
        long hours = ChronoUnit.HOURS.between(lastProgressUpdate, LocalDateTime.now());
        if (hours < 1) return "Just now";
        if (hours < 24) return hours + " hours ago";
        return (hours / 24) + " days ago";
    }
    
    /**
     * Check if achievement is likely to be completed soon
     */
    public boolean isNearCompletion() {
        return getProgressPercentage() >= 0.9 && !isCompleted;
    }
    
    /**
     * Get reward status message
     */
    public String getRewardStatusMessage() {
        if (isCompleted && !isRewardClaimed) {
            return "⚠️ Reward available! Claim your XP!";
        }
        if (isRewardClaimed) {
            return "✅ Reward claimed";
        }
        return "🔒 Complete to earn reward";
    }
    
    /**
     * Create a detailed progress report
     */
    public Map<String, Object> createProgressReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("achievementId", achievementId);
        report.put("progress", progressCurrent);
        report.put("target", targetValue);
        report.put("percentage", String.format("%.1f%%", getProgressPercentage() * 100));
        report.put("state", getCurrentState().getDisplayName());
        report.put("trend", getTrend());
        report.put("milestonesReached", milestonesReached.size());
        report.put("lastUpdate", getTimeSinceLastUpdate());
        report.put("dailyProgress", dailyProgress);
        report.put("weeklyProgress", weeklyProgress);
        
        if (estimatedCompletionDate != null) {
            report.put("estimatedCompletion", estimatedCompletionDate.toString());
        }
        
        return report;
    }
    
    // ==================== GETTERS AND SETTERS ====================
    
    public int getUserAchievementId() { return userAchievementId; }
    public void setUserAchievementId(int userAchievementId) { this.userAchievementId = userAchievementId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getAchievementId() { return achievementId; }
    public void setAchievementId(int achievementId) { this.achievementId = achievementId; }
    
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
    
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
    
    public LocalDateTime getLastProgressUpdate() { return lastProgressUpdate; }
    public void setLastProgressUpdate(LocalDateTime lastProgressUpdate) { this.lastProgressUpdate = lastProgressUpdate; }
    
    public int getProgressCurrent() { return progressCurrent; }
    public void setProgressCurrent(int progressCurrent) { 
        this.progressCurrent = progressCurrent; 
        updateProgress(progressCurrent);
    }
    
    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public boolean isRewardClaimed() { return isRewardClaimed; }
    public void setRewardClaimed(boolean rewardClaimed) { isRewardClaimed = rewardClaimed; }
    
    public int getTimesAchieved() { return timesAchieved; }
    public void setTimesAchieved(int timesAchieved) { this.timesAchieved = timesAchieved; }
    
    public double getBestProgress() { return bestProgress; }
    public void setBestProgress(double bestProgress) { this.bestProgress = bestProgress; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Map<String, Object> getProgressDetails() { return progressDetails; }
    public void setProgressDetails(Map<String, Object> progressDetails) { this.progressDetails = progressDetails; }
    
    public List<Milestone> getMilestonesReached() { return milestonesReached; }
    public void setMilestonesReached(List<Milestone> milestonesReached) { this.milestonesReached = milestonesReached; }
    
    public LocalDateTime getEstimatedCompletionDate() { return estimatedCompletionDate; }
    public void setEstimatedCompletionDate(LocalDateTime estimatedCompletionDate) { this.estimatedCompletionDate = estimatedCompletionDate; }
    
    public int getWeeklyProgress() { return weeklyProgress; }
    public void setWeeklyProgress(int weeklyProgress) { this.weeklyProgress = weeklyProgress; }
    
    public int getDailyProgress() { return dailyProgress; }
    public void setDailyProgress(int dailyProgress) { this.dailyProgress = dailyProgress; }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Add custom progress detail
     */
    public void addProgressDetail(String key, Object value) {
        this.progressDetails.put(key, value);
    }
    
    /**
     * Get progress detail
     */
    public Object getProgressDetail(String key) {
        return this.progressDetails.get(key);
    }
    
    /**
     * Create a copy of this user achievement
     */
    public UserAchievement copy() {
        UserAchievement copy = new UserAchievement();
        copy.userAchievementId = this.userAchievementId;
        copy.userId = this.userId;
        copy.achievementId = this.achievementId;
        copy.unlockedAt = this.unlockedAt;
        copy.claimedAt = this.claimedAt;
        copy.lastProgressUpdate = this.lastProgressUpdate;
        copy.progressCurrent = this.progressCurrent;
        copy.targetValue = this.targetValue;
        copy.isCompleted = this.isCompleted;
        copy.isRewardClaimed = this.isRewardClaimed;
        copy.timesAchieved = this.timesAchieved;
        copy.bestProgress = this.bestProgress;
        copy.notes = this.notes;
        copy.progressDetails = new HashMap<>(this.progressDetails);
        copy.milestonesReached = new ArrayList<>(this.milestonesReached);
        copy.estimatedCompletionDate = this.estimatedCompletionDate;
        copy.weeklyProgress = this.weeklyProgress;
        copy.dailyProgress = this.dailyProgress;
        return copy;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAchievement that = (UserAchievement) o;
        return userAchievementId == that.userAchievementId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userAchievementId);
    }
    
    @Override
    public String toString() {
        return String.format("UserAchievement{id=%d, userId=%d, achievementId=%d, progress=%d/%d, completed=%s, claimed=%s}",
            userAchievementId, userId, achievementId, progressCurrent, targetValue, isCompleted, isRewardClaimed);
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Create a user achievement for a new user
     */
    public static UserAchievement createForNewUser(int userId, Achievement achievement) {
        UserAchievement ua = new UserAchievement();
        ua.userId = userId;
        ua.achievementId = achievement.getAchievementId();
        ua.targetValue = achievement.getCriteriaValue();
        ua.progressCurrent = 0;
        ua.isCompleted = false;
        ua.isRewardClaimed = false;
        ua.lastProgressUpdate = LocalDateTime.now();
        return ua;
    }
    
    /**
     * Batch create user achievements for a new user
     */
    public static List<UserAchievement> createAllForNewUser(int userId, List<Achievement> achievements) {
        List<UserAchievement> userAchievements = new ArrayList<>();
        for (Achievement achievement : achievements) {
            userAchievements.add(createForNewUser(userId, achievement));
        }
        return userAchievements;
    }
    
    /**
     * Calculate progress based on criteria type and user stats
     */
    public static int calculateProgress(CriteriaType criteriaType, User user, Map<String, Object> context) {
        switch (criteriaType) {
            case TASKS_COMPLETED:
                return user.getTotalTasksCompleted();
            case STREAK_DAYS:
                return user.getCurrentStreak();
            case TOTAL_XP:
                return user.getXpTotal();
            case TIME_SPENT:
                return (int)(user.getTotalTimeSeconds() / 3600);
            case PERFECT_DAY:
                // Implementation depends on perfect day tracking
                return 0;
            case CATEGORY_MASTER:
                String category = (String) context.getOrDefault("category", "Work");
                // Would need category-specific task count
                return 0;
            case DIFFICULTY_CONQUEROR:
                // Would need difficulty-specific task count
                return 0;
            default:
                return 0;
        }
    }
}

// ==================== REPOSITORY INTERFACE ====================

/**
 * Repository interface for UserAchievement data access
 */
interface UserAchievementRepository {
    UserAchievement save(UserAchievement userAchievement);
    UserAchievement findById(int userAchievementId);
    List<UserAchievement> findByUserId(int userId);
    List<UserAchievement> findByAchievementId(int achievementId);
    List<UserAchievement> findCompletedByUserId(int userId);
    List<UserAchievement> findInProgressByUserId(int userId);
    void delete(int userAchievementId);
    void deleteByUserId(int userId);
    int countCompletedByUserId(int userId);
    int countTotalProgressByUserId(int userId);
    Map<Integer, Integer> getProgressMapForUser(int userId);
}

// ==================== SERVICE CLASS ====================

/**
 * Service class for managing user achievements
 */
class UserAchievementService {
    private UserAchievementRepository repository;
    
    public UserAchievementService(UserAchievementRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Update progress for an achievement based on user action
     */
    public boolean updateProgress(int userId, int achievementId, int newProgress) {
        UserAchievement ua = findOrCreate(userId, achievementId);
        boolean completed = ua.updateProgress(newProgress);
        repository.save(ua);
        return completed;
    }
    
    /**
     * Add progress to an achievement
     */
    public boolean addProgress(int userId, int achievementId, int amount) {
        UserAchievement ua = findOrCreate(userId, achievementId);
        boolean completed = ua.addProgress(amount);
        repository.save(ua);
        
        if (completed) {
            // Trigger achievement completion event
            onAchievementCompleted(ua);
        }
        
        return completed;
    }
    
    /**
     * Claim reward for completed achievement
     */
    public void claimReward(int userId, int achievementId) {
        UserAchievement ua = findOrCreate(userId, achievementId);
        ua.claimReward();
        repository.save(ua);
    }
    
    /**
     * Get all achievements for a user with progress
     */
    public List<UserAchievement> getUserAchievementsWithProgress(int userId) {
        return repository.findByUserId(userId);
    }
    
    /**
     * Get completion percentage for a user
     */
    public double getUserCompletionPercentage(int userId) {
        List<UserAchievement> all = repository.findByUserId(userId);
        if (all.isEmpty()) return 0;
        long completed = all.stream().filter(UserAchievement::isCompleted).count();
        return (double) completed / all.size();
    }
    
    /**
     * Get recently completed achievements
     */
    public List<UserAchievement> getRecentlyCompleted(int userId, int limit) {
        return repository.findCompletedByUserId(userId).stream()
            .filter(ua -> ua.getUnlockedAt() != null)
            .sorted((a, b) -> b.getUnlockedAt().compareTo(a.getUnlockedAt()))
            .limit(limit)
            .toList();
    }
    
    /**
     * Find or create a user achievement record
     */
    private UserAchievement findOrCreate(int userId, int achievementId) {
        List<UserAchievement> existing = repository.findByUserId(userId);
        return existing.stream()
            .filter(ua -> ua.getAchievementId() == achievementId)
            .findFirst()
            .orElseGet(() -> {
                // Would need to fetch achievement target value from AchievementService
                UserAchievement newUa = new UserAchievement(userId, achievementId, 100);
                return repository.save(newUa);
            });
    }
    
    /**
     * Handle achievement completion event
     */
    private void onAchievementCompleted(UserAchievement ua) {
        // Trigger notifications, award XP, update user stats, etc.
        System.out.println("Achievement completed! User: " + ua.getUserId() + 
                          ", Achievement: " + ua.getAchievementId());
    }
}

// Note: Add these imports at the top of your file:
// import java.time.LocalDateTime;
// import java.time.temporal.ChronoUnit;
// import java.util.*;
