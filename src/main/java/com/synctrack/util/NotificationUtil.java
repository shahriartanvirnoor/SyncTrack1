// NotificationUtil.java
package com.synctrack.util;

import com.synctrack.model.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * NotificationUtil - Comprehensive notification system for SyncTrack
 * Provides toast notifications, alert dialogs, achievement popups, and reminder system
 */
public class NotificationUtil {
    
    // ==================== SINGLETON INSTANCE ====================
    
    private static NotificationUtil instance;
    private final Queue<QueuedNotification> notificationQueue;
    private ScheduledExecutorService scheduler;
    private boolean isShowingNotification;
    private Stage notificationStage;
    
    private NotificationUtil() {
        this.notificationQueue = new ConcurrentLinkedQueue<>();
        this.isShowingNotification = false;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        startNotificationProcessor();
    }
    
    public static synchronized NotificationUtil getInstance() {
        if (instance == null) {
            instance = new NotificationUtil();
        }
        return instance;
    }
    
    // ==================== NOTIFICATION TYPES ====================
    
    public enum NotificationType {
        SUCCESS("✓", "#4caf50", "Success"),
        ERROR("✗", "#f44336", "Error"),
        WARNING("⚠", "#ff9800", "Warning"),
        INFO("ℹ", "#2196f3", "Information"),
        ACHIEVEMENT("🏆", "#9c27b0", "Achievement Unlocked!"),
        REWARD("🎁", "#ff5722", "Reward Available"),
        REMINDER("⏰", "#ffc107", "Reminder"),
        STREAK("🔥", "#e91e63", "Streak Alert");
        
        private final String icon;
        private final String color;
        private final String title;
        
        NotificationType(String icon, String color, String title) {
            this.icon = icon;
            this.color = color;
            this.title = title;
        }
        
        public String getIcon() { return icon; }
        public String getColor() { return color; }
        public String getTitle() { return title; }
    }
    
    // ==================== NOTIFICATION CLASSES ====================
    
    /**
     * Represents a queued notification
     */
    private static class QueuedNotification {
        final NotificationType type;
        final String title;
        final String message;
        final Duration displayDuration;
        final Runnable onClickAction;
        final LocalDateTime timestamp;
        
        QueuedNotification(NotificationType type, String title, String message, 
                          Duration displayDuration, Runnable onClickAction) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.displayDuration = displayDuration;
            this.onClickAction = onClickAction;
            this.timestamp = LocalDateTime.now();
        }
    }
    
    /**
     * Notification builder for fluent API
     */
    public static class NotificationBuilder {
        private NotificationType type = NotificationType.INFO;
        private String title;
        private String message;
        private Duration duration = Duration.seconds(3);
        private Runnable onClickAction;
        private boolean isPersistent = false;
        
        public NotificationBuilder type(NotificationType type) {
            this.type = type;
            if (title == null) this.title = type.getTitle();
            return this;
        }
        
        public NotificationBuilder title(String title) {
            this.title = title;
            return this;
        }
        
        public NotificationBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public NotificationBuilder duration(Duration duration) {
            this.duration = duration;
            return this;
        }
        
        public NotificationBuilder persistent() {
            this.isPersistent = true;
            this.duration = Duration.INDEFINITE;
            return this;
        }
        
        public NotificationBuilder onClick(Runnable action) {
            this.onClickAction = action;
            return this;
        }
        
        public void show() {
            NotificationUtil.getInstance().showNotification(
                type, title, message, duration, onClickAction
            );
        }
        
        public void showAndWait() {
            if (isPersistent) {
                show();
            } else {
                NotificationUtil.getInstance().showModalNotification(type, title, message);
            }
        }
    }
    
    // ==================== TOAST NOTIFICATIONS ====================
    
    /**
     * Show a simple toast notification
     */
    public void showToast(String message, NotificationType type) {
        showNotification(type, type.getTitle(), message, Duration.seconds(3), null);
    }
    
    /**
     * Show a success toast
     */
    public void showSuccess(String message) {
        showToast(message, NotificationType.SUCCESS);
    }
    
    /**
     * Show an error toast
     */
    public void showError(String message) {
        showToast(message, NotificationType.ERROR);
    }
    
    /**
     * Show a warning toast
     */
    public void showWarning(String message) {
        showToast(message, NotificationType.WARNING);
    }
    
    /**
     * Show an info toast
     */
    public void showInfo(String message) {
        showToast(message, NotificationType.INFO);
    }
    
    /**
     * Show a custom notification
     */
    public void showNotification(NotificationType type, String title, String message, 
                                 Duration duration, Runnable onClickAction) {
        QueuedNotification notification = new QueuedNotification(type, title, message, duration, onClickAction);
        notificationQueue.offer(notification);
    }
    
    /**
     * Process notification queue
     */
    private void startNotificationProcessor() {
        scheduler.scheduleAtFixedRate(() -> {
            if (!isShowingNotification && !notificationQueue.isEmpty()) {
                QueuedNotification notification = notificationQueue.poll();
                if (notification != null) {
                    Platform.runLater(() -> showNotificationWindow(notification));
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Display notification window
     */
    private void showNotificationWindow(QueuedNotification notification) {
        isShowingNotification = true;
        
        // Create notification stage
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.NONE);
        stage.setAlwaysOnTop(true);
        
        // Create notification content
        VBox content = createNotificationContent(notification, stage);
        
        // Create scene with transparent background
        Scene scene = new Scene(content);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        
        // Position at bottom-right corner of primary screen
        stage.setX(getScreenWidth() - content.getPrefWidth() - 20);
        stage.setY(getScreenHeight() - content.getPrefHeight() - 20);
        
        // Add entrance animation
        content.setOpacity(0);
        content.setTranslateX(100);
        
        Timeline entrance = new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                content.setOpacity(0);
                content.setTranslateX(100);
            }),
            new KeyFrame(Duration.millis(300), e -> {
                content.setOpacity(1);
                content.setTranslateX(0);
            })
        );
        
        stage.show();
        entrance.play();
        
        // Auto-close after duration
        if (notification.displayDuration != Duration.INDEFINITE) {
            PauseTransition pause = new PauseTransition(notification.displayDuration);
            pause.setOnFinished(e -> closeNotification(stage, content));
            pause.play();
        }
    }
    
    /**
     * Create notification content
     */
    private VBox createNotificationContent(QueuedNotification notification, Stage stage) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15, 20, 15, 20));
        content.setPrefWidth(350);
        content.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);"
        );
        
        // Header with icon and title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(notification.type.getIcon());
        iconLabel.setFont(Font.font(24));
        
        Label titleLabel = new Label(notification.title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(notification.type.getColor()));
        
        // Close button
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #999;");
        closeBtn.setOnAction(e -> closeNotification(stage, content));
        
        header.getChildren().addAll(iconLabel, titleLabel, spacer, closeBtn);
        
        // Message
        Label messageLabel = new Label(notification.message);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Arial", 12));
        messageLabel.setTextFill(Color.web("#555555"));
        
        // Timestamp
        Label timeLabel = new Label(notification.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(Font.font("Arial", 10));
        timeLabel.setTextFill(Color.web("#999999"));
        
        content.getChildren().addAll(header, messageLabel, timeLabel);
        
        // Add click action
        if (notification.onClickAction != null) {
            content.setOnMouseClicked(e -> {
                notification.onClickAction.run();
                closeNotification(stage, content);
            });
            content.setCursor(javafx.scene.Cursor.HAND);
        }
        
        return content;
    }
    
    /**
     * Close notification with exit animation
     */
    private void closeNotification(Stage stage, VBox content) {
        Timeline exit = new Timeline(
            new KeyFrame(Duration.millis(200), e -> {
                content.setOpacity(0);
                content.setTranslateX(100);
            })
        );
        exit.setOnFinished(e -> {
            stage.close();
            isShowingNotification = false;
        });
        exit.play();
    }
    
    // ==================== MODAL NOTIFICATIONS ====================
    
    /**
     * Show modal dialog (blocking)
     */
    public void showModalNotification(NotificationType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            // Style the alert
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: white;");
            
            // Add icon based on type
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().clear();
            
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(okButton);
            
            alert.showAndWait();
        });
    }
    
    /**
     * Show confirmation dialog
     */
    public boolean showConfirmation(String title, String message) {
        // Since this is called from non-JavaFX threads, we need to block
        final boolean[] result = {false};
        final Object lock = new Object();
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);
            
            Optional<ButtonType> resultOption = alert.showAndWait();
            result[0] = resultOption.isPresent() && resultOption.get() == yesButton;
            
            synchronized (lock) {
                lock.notify();
            }
        });
        
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return result[0];
    }
    
    // ==================== ACHIEVEMENT NOTIFICATIONS ====================
    
    /**
     * Show achievement unlocked notification with fanfare
     */
    public void showAchievementUnlocked(Achievement achievement) {
        QueuedNotification notification = new QueuedNotification(
            NotificationType.ACHIEVEMENT,
            "Achievement Unlocked! 🏆",
            achievement.getName() + "\n+" + achievement.getXpReward() + " XP",
            Duration.seconds(4),
            null
        );
        notificationQueue.offer(notification);
        
        // Play fanfare sound (would need audio system)
        playAchievementSound();
    }
    
    /**
     * Show achievement progress update
     */
    public void showAchievementProgress(Achievement achievement, int progress, int target) {
        int percent = (progress * 100) / target;
        String message = String.format("%s: %d/%d (%d%%)", 
            achievement.getName(), progress, target, percent);
        
        QueuedNotification notification = new QueuedNotification(
            NotificationType.INFO,
            "Achievement Progress 📈",
            message,
            Duration.seconds(2),
            null
        );
        notificationQueue.offer(notification);
    }
    
    // ==================== REWARD NOTIFICATIONS ====================
    
    /**
     * Show reward redeemed notification
     */
    public void showRewardRedeemedNotification(User user, Reward reward) {
        String message = String.format("You redeemed %s for %d XP!", 
            reward.getName(), reward.getCalculatedXpCost());
        
        QueuedNotification notification = new QueuedNotification(
            NotificationType.REWARD,
            "Reward Redeemed! 🎁",
            message,
            Duration.seconds(4),
            null
        );
        notificationQueue.offer(notification);
    }
    
    /**
     * Show new reward available notification
     */
    public void showRewardAvailableNotification(Reward reward) {
        String message = String.format("New reward available: %s (%d XP)", 
            reward.getName(), reward.getCalculatedXpCost());
        
        QueuedNotification notification = new QueuedNotification(
            NotificationType.REWARD,
            "New Reward Available! 🎉",
            message,
            Duration.seconds(5),
            null
        );
        notificationQueue.offer(notification);
    }
    
    // ==================== TASK REMINDERS ====================
    
    /**
     * Show task deadline reminder
     */
    public void showTaskReminder(Task task) {
        String timeUntil;
        if (task.getDeadline().isBefore(LocalDateTime.now())) {
            timeUntil = "OVERDUE!";
        } else {
            Duration duration = Duration.between(LocalDateTime.now(), task.getDeadline());
            long hours = duration.toHours();
            if (hours < 24) {
                timeUntil = "Due in " + hours + " hours";
            } else {
                timeUntil = "Due in " + (hours / 24) + " days";
            }
        }
        
        String message = String.format("%s\n%s", task.getTitle(), timeUntil);
        
        QueuedNotification notification = new QueuedNotification(
            NotificationType.REMINDER,
            "Task Reminder ⏰",
            message,
            Duration.seconds(5),
            () -> openTaskDetails(task)
        );
        notificationQueue.offer(notification);
    }
    
    /**
     * Show daily summary reminder
     */
    public void showDailySummary(int tasksCompleted, int totalTasks, long timeSpentSeconds) {
        String timeString = DateUtils.formatDurationCompact(timeSpentSeconds);
        String message = String.format("You completed %d/%d tasks today!\nTotal time: %s", 
            tasksCompleted, totalTasks, timeString);
        
        QueuedNotification notification = new QueuedNotification(
            NotificationType.INFO,
            "Daily Summary 📊",
            message,
            Duration.seconds(6),
            null
        );
        notificationQueue.offer(notification);
    }
    
    // ==================== STREAK NOTIFICATIONS ====================
    
    /**
     * Show streak milestone notification
     */
    public void showStreakMilestone(int streakDays) {
        String message;
        if (streakDays == 7) {
            message = "7-day streak! Weekly Warrior badge unlocked!";
        } else if (streakDays == 30) {
            message = "30-day streak! Monthly Champion badge unlocked!";
        } else if (streakDays == 100) {
            message = "100-day streak! Unstoppable! 🔥🔥🔥";
        } else {
            message = streakDays + " day streak! Keep it going! 🔥";
        }
        
        QueuedNotification notification = new QueuedNotification(
            NotificationType.STREAK,
            "Streak Milestone! 🔥",
            message,
            Duration.seconds(4),
            null
        );
        notificationQueue.offer(notification);
    }
    
    /**
     * Show streak at risk notification
     */
    public void showStreakAtRisk(int currentStreak) {
        String message = "You haven't completed any tasks today!\n" +
                        "Complete a task to maintain your " + currentStreak + "-day streak.";
        
        QueuedNotification notification = new QueuedNotification(
            NotificationType.WARNING,
            "Streak at Risk! ⚠️",
            message,
            Duration.seconds(5),
            () -> openTaskCreation()
        );
        notificationQueue.offer(notification);
    }
    
    // ==================== LEVEL UP NOTIFICATIONS ====================
    
    /**
     * Show level up notification with celebration
     */
    public void showLevelUp(User user, int oldLevel, int newLevel) {
        String message = String.format("Congratulations! You reached Level %d!\n" +
                                      "You earned a bonus of %d XP!", 
                                      newLevel, newLevel * 100);
        
        QueuedNotification notification = new QueuedNotification(
            NotificationType.ACHIEVEMENT,
            "Level Up! 🎉",
            message,
            Duration.seconds(5),
            null
        );
        notificationQueue.offer(notification);
        
        // Add celebration effect
        showCelebrationEffect();
    }
    
    // ==================== PRODUCTIVITY INSIGHTS ====================
    
    /**
     * Show productivity insight notification
     */
    public void showProductivityInsight(String insight, double score) {
        String scoreEmoji = score >= 80 ? "🎯" : (score >= 60 ? "📈" : "💪");
        String message = String.format("%s\nProductivity Score: %.0f", insight, score);
        
        QueuedNotification notification = new QueuedNotification(
            NotificationType.INFO,
            "Productivity Insight " + scoreEmoji,
            message,
            Duration.seconds(4),
            () -> openAnalytics()
        );
        notificationQueue.offer(notification);
    }
    
    // ==================== IN-APP ALERTS ====================
    
    /**
     * Show in-app banner alert (non-modal, appears at top)
     */
    public void showInAppBanner(String message, NotificationType type, Duration duration) {
        Platform.runLater(() -> {
            // Get main stage (would need reference to primary stage)
            // This is a simplified version
            System.out.println("[" + type.name() + "] " + message);
        });
    }
    
    /**
     * Show tooltip on a node
     */
    public void showTooltip(javafx.scene.Node node, String message, Duration duration) {
        Platform.runLater(() -> {
            Tooltip tooltip = new Tooltip(message);
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setShowDuration(duration);
            Tooltip.install(node, tooltip);
            
            // Manually show tooltip
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(e -> {
                javafx.scene.control.Tooltip t = new javafx.scene.control.Tooltip(message);
                t.show(node, node.getLayoutX() + 10, node.getLayoutY() + 10);
            });
            pause.play();
        });
    }
    
    // ==================== BATCH NOTIFICATIONS ====================
    
    /**
     * Show daily digest of notifications
     */
    public void showDailyDigest(Map<String, Integer> digest) {
        StringBuilder sb = new StringBuilder();
        digest.forEach((key, value) -> {
            sb.append(key).append(": ").append(value).append("\n");
        });
        
        QueuedNotification notification = new QueuedNotification(
            NotificationType.INFO,
            "Daily Digest 📬",
            sb.toString(),
            Duration.seconds(8),
            null
        );
        notificationQueue.offer(notification);
    }
    
    /**
     * Clear all pending notifications
     */
    public void clearAllNotifications() {
        notificationQueue.clear();
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Get screen width (simplified)
     */
    private double getScreenWidth() {
        return javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
    }
    
    /**
     * Get screen height (simplified)
     */
    private double getScreenHeight() {
        return javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
    }
    
    /**
     * Play achievement sound (placeholder - would need audio system)
     */
    private void playAchievementSound() {
        // Audio implementation would go here
        System.out.println("🔊 Achievement sound played");
    }
    
    /**
     * Show celebration effect (placeholder)
     */
    private void showCelebrationEffect() {
        System.out.println("🎉 Celebration effect triggered");
    }
    
    /**
     * Open task details (navigation callback - would need to implement)
     */
    private void openTaskDetails(Task task) {
        // Navigation to task details view
        System.out.println("Opening task: " + task.getTitle());
    }
    
    /**
     * Open task creation (callback)
     */
    private void openTaskCreation() {
        System.out.println("Opening task creation");
    }
    
    /**
     * Open analytics (callback)
     */
    private void openAnalytics() {
        System.out.println("Opening analytics");
    }
    
    // ==================== REMINDER SCHEDULER ====================
    
    /**
     * Schedule a reminder for a specific time
     */
    public void scheduleReminder(LocalDateTime reminderTime, String title, String message) {
        long delayMillis = Duration.between(LocalDateTime.now(), reminderTime).toMillis();
        
        if (delayMillis > 0) {
            scheduler.schedule(() -> {
                showNotification(NotificationType.REMINDER, title, message, Duration.seconds(5), null);
            }, delayMillis, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Schedule recurring daily reminder
     */
    public void scheduleDailyReminder(LocalTime reminderTime, String title, String message) {
        Runnable reminderTask = () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderDateTime = now.withHour(reminderTime.getHour())
                                                .withMinute(reminderTime.getMinute())
                                                .withSecond(0);
            
            if (reminderDateTime.isBefore(now)) {
                reminderDateTime = reminderDateTime.plusDays(1);
            }
            
            long initialDelay = Duration.between(now, reminderDateTime).toMillis();
            
            scheduler.scheduleAtFixedRate(() -> {
                showNotification(NotificationType.REMINDER, title, message, Duration.seconds(5), null);
            }, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
        };
        
        new Thread(reminderTask).start();
    }
    
    // ==================== CLEANUP ====================
    
    /**
     * Shutdown notification system
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // ==================== STATIC UTILITY METHODS ====================
    
    /**
     * Quick success notification (static convenience)
     */
    public static void success(String message) {
        getInstance().showSuccess(message);
    }
    
    /**
     * Quick error notification (static convenience)
     */
    public static void error(String message) {
        getInstance().showError(message);
    }
    
    /**
     * Quick warning notification (static convenience)
     */
    public static void warning(String message) {
        getInstance().showWarning(message);
    }
    
    /**
     * Quick info notification (static convenience)
     */
    public static void info(String message) {
        getInstance().showInfo(message);
    }
}

// Note: Add these imports at the top of your file:
// import javafx.animation.*;
// import javafx.application.Platform;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.scene.Scene;
// import javafx.scene.control.*;
// import javafx.scene.effect.DropShadow;
// import javafx.scene.layout.*;
// import javafx.scene.paint.Color;
// import javafx.scene.text.Font;
// import javafx.scene.text.FontWeight;
// import javafx.stage.Modality;
// import javafx.stage.Stage;
// import javafx.stage.StageStyle;
// import javafx.util.Duration;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.time.format.DateTimeFormatter;
// import java.util.*;
// import java.util.concurrent.ConcurrentLinkedQueue;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;
