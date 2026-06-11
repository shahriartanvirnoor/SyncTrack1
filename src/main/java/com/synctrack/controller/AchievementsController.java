// AchievementsController.java
package com.synctrack.controller;

import com.synctrack.SyncTrackApp;
import com.synctrack.model.Achievement;
import com.synctrack.model.User;
import com.synctrack.model.UserAchievement;
import com.synctrack.service.AuthService;
import com.synctrack.service.GamificationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class AchievementsController {
    private final AuthService authService;
    private final GamificationService gamificationService;
    private Scene scene;
    private GridPane achievementsGrid;
    private Label totalXpLabel;
    private Label levelLabel;
    private ProgressBar levelProgress;
    
    public AchievementsController(AuthService authService) {
        this.authService = authService;
        this.gamificationService = new GamificationService();
        createScene();
        loadAchievements();
    }
    
    private void createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        root.setTop(createNavigationBar());
        root.setCenter(createMainContent());
        
        scene = new Scene(root, 1200, 800);
    }
    
    private HBox createNavigationBar() {
        HBox navBar = new HBox(10);
        navBar.setPadding(new Insets(15, 20, 15, 20));
        navBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");
        navBar.setAlignment(Pos.CENTER_LEFT);
        
        Button[] buttons = {
            createNavButton("🏠 Dashboard", "dashboard"),
            createNavButton("📋 Tasks", "tasks"),
            createNavButton("⏱️ Timer", "timer"),
            createNavButton("📊 Analytics", "analytics"),
            createNavButton("🏆 Achievements", "achievements"),
            createNavButton("⚙️ Settings", "settings")
        };
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        User user = authService.getCurrentUser();
        Label userLabel = new Label("👤 " + user.getUsername());
        userLabel.setTextFill(Color.WHITE);
        
        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> {
            authService.logout();
            SyncTrackApp.showLoginScreen();
        });
        
        navBar.getChildren().addAll(buttons);
        navBar.getChildren().addAll(spacer, userLabel, logoutBtn);
        
        return navBar;
    }
    
    private Button createNavButton(String text, String view) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;"));
        btn.setOnAction(e -> SyncTrackApp.navigateTo(view));
        return btn;
    }
    
    private VBox createMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Header Section
        content.getChildren().add(createHeaderSection());
        
        // Stats Section
        content.getChildren().add(createStatsSection());
        
        // Achievements Grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        achievementsGrid = new GridPane();
        achievementsGrid.setHgap(20);
        achievementsGrid.setVgap(20);
        achievementsGrid.setPadding(new Insets(20));
        
        scrollPane.setContent(achievementsGrid);
        content.getChildren().add(scrollPane);
        
        return content;
    }
    
    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        header.setAlignment(Pos.CENTER);
        
        Label title = new Label("🏆 Achievements & Badges");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        
        Label subtitle = new Label("Complete challenges to earn badges and XP rewards!");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.GRAY);
        
        header.getChildren().addAll(title, subtitle);
        return header;
    }
    
    private HBox createStatsSection() {
        HBox statsBox = new HBox(20);
        statsBox.setPadding(new Insets(20));
        statsBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        User user = authService.getCurrentUser();
        
        // Total XP Card
        VBox xpCard = createStatCard("Total XP", user.getXpTotal() + " XP");
        
        // Level Card
        VBox levelCard = createLevelCard(user);
        
        // Completion Card
        int totalAchievements = gamificationService.getTotalAchievements();
        int completedAchievements = gamificationService.getCompletedAchievements(user.getUserId());
        VBox completionCard = createStatCard("Achievements Completed", 
            completedAchievements + " / " + totalAchievements);
        
        statsBox.getChildren().addAll(xpCard, levelCard, completionCard);
        HBox.setHgrow(xpCard, Priority.ALWAYS);
        HBox.setHgrow(levelCard, Priority.ALWAYS);
        HBox.setHgrow(completionCard, Priority.ALWAYS);
        
        return statsBox;
    }
    
    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8;");
        card.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.GRAY);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web("#667eea"));
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
    
    private VBox createLevelCard(User user) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8;");
        card.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Current Level");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.GRAY);
        
        levelLabel = new Label("Level " + user.getLevel());
        levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        levelLabel.setTextFill(Color.web("#ff9800"));
        
        levelProgress = new ProgressBar(user.getLevelProgress());
        levelProgress.setPrefWidth(200);
        levelProgress.setStyle("-fx-accent: #ff9800;");
        
        totalXpLabel = new Label(user.getXpTotal() + " / " + user.getXpForNextLevel() + " XP");
        totalXpLabel.setFont(Font.font("Arial", 12));
        totalXpLabel.setTextFill(Color.GRAY);
        
        card.getChildren().addAll(titleLabel, levelLabel, levelProgress, totalXpLabel);
        return card;
    }
    
    private void loadAchievements() {
        achievementsGrid.getChildren().clear();
        
        List<Achievement> achievements = gamificationService.getAllAchievements();
        List<UserAchievement> userAchievements = gamificationService.getUserAchievements(
            authService.getCurrentUser().getUserId()
        );
        
        int row = 0;
        int col = 0;
        
        for (Achievement achievement : achievements) {
            UserAchievement userAch = userAchievements.stream()
                .filter(ua -> ua.getAchievementId() == achievement.getAchievementId())
                .findFirst()
                .orElse(null);
            
            VBox achievementCard = createAchievementCard(achievement, userAch);
            achievementsGrid.add(achievementCard, col, row);
            
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }
    
    private VBox createAchievementCard(Achievement achievement, UserAchievement userAch) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(350);
        
        boolean isUnlocked = userAch != null && userAch.isCompleted();
        int progress = userAch != null ? userAch.getProgressCurrent() : 0;
        
        String icon = getAchievementIcon(achievement.getDifficultyLevel());
        String borderColor = isUnlocked ? "#4caf50" : "#e0e0e0";
        
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );
        
        // Icon and Title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(24));
        
        Label nameLabel = new Label(achievement.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(isUnlocked ? Color.web("#4caf50") : Color.BLACK);
        
        header.getChildren().addAll(iconLabel, nameLabel);
        
        // Description
        Label descLabel = new Label(achievement.getDescription());
        descLabel.setFont(Font.font("Arial", 12));
        descLabel.setTextFill(Color.GRAY);
        descLabel.setWrapText(true);
        
        // Progress
        VBox progressBox = new VBox(5);
        Label progressLabel = new Label("Progress: " + progress + " / " + achievement.getCriteriaValue());
        progressLabel.setFont(Font.font("Arial", 11));
        
        ProgressBar progressBar = new ProgressBar((double) progress / achievement.getCriteriaValue());
        progressBar.setPrefWidth(300);
        String progressColor = isUnlocked ? "#4caf50" : "#667eea";
        progressBar.setStyle("-fx-accent: " + progressColor + ";");
        
        progressBox.getChildren().addAll(progressLabel, progressBar);
        
        // XP Reward
        Label xpLabel = new Label("🏅 " + achievement.getXpReward() + " XP");
        xpLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        xpLabel.setTextFill(Color.web("#ff9800"));
        
        // Unlock status
        if (isUnlocked) {
            Label unlockedLabel = new Label("✓ UNLOCKED");
            unlockedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            unlockedLabel.setTextFill(Color.web("#4caf50"));
            card.getChildren().addAll(header, descLabel, progressBox, xpLabel, unlockedLabel);
        } else {
            card.getChildren().addAll(header, descLabel, progressBox, xpLabel);
        }
        
        return card;
    }
    
    private String getAchievementIcon(String difficulty) {
        return switch (difficulty) {
            case "Bronze" -> "🥉";
            case "Silver" -> "🥈";
            case "Gold" -> "🥇";
            case "Platinum" -> "💎";
            default -> "🏆";
        };
    }
    
    public Scene getScene() {
        return scene;
    }
}
