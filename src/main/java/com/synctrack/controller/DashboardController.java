// DashboardController.java
package com.synctrack.controller;

import com.synctrack.SyncTrackApp;
import com.synctrack.model.DailyStat;
import com.synctrack.model.Task;
import com.synctrack.model.User;
import com.synctrack.service.AnalyticsService;
import com.synctrack.service.AuthService;
import com.synctrack.service.TaskService;
import com.synctrack.service.GamificationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController {
    private final AuthService authService;
    private final TaskService taskService;
    private final AnalyticsService analyticsService;
    private final GamificationService gamificationService;
    private Scene scene;
    
    // UI Components
    private Label welcomeLabel;
    private Label levelLabel;
    private ProgressBar levelProgress;
    private Label xpLabel;
    private Label streakLabel;
    private Label todayTasksLabel;
    private Label todayTimeLabel;
    private Label productivityScoreLabel;
    private LineChart<String, Number> trendChart;
    private PieChart categoryChart;
    private ListView<Task> upcomingTasksList;
    private ListView<String> recentAchievementsList;
    
    public DashboardController(AuthService authService) {
        this.authService = authService;
        this.taskService = new TaskService();
        this.analyticsService = new AnalyticsService();
        this.gamificationService = new GamificationService();
        createScene();
        refreshData();
        startAutoRefresh();
    }
    
    private void createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        root.setTop(createNavigationBar());
        root.setCenter(createMainContent());
        root.setBottom(createStatusBar());
        
        scene = new Scene(root, 1400, 900);
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
        
        // User info
        User currentUser = authService.getCurrentUser();
        Label userLabel = new Label("👤 " + currentUser.getUsername());
        userLabel.setTextFill(Color.WHITE);
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
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
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;"));
        btn.setOnAction(e -> SyncTrackApp.navigateTo(view));
        return btn;
    }
    
    private ScrollPane createMainContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Welcome Header
        content.getChildren().add(createWelcomeHeader());
        
        // Stats Grid
        content.getChildren().add(createStatsGrid());
        
        // Charts Row
        HBox chartsRow = new HBox(20);
        chartsRow.getChildren().addAll(createTrendChart(), createCategoryChart());
        content.getChildren().add(chartsRow);
        
        // Bottom Row
        HBox bottomRow = new HBox(20);
        bottomRow.getChildren().addAll(createUpcomingTasks(), createRecentAchievements());
        content.getChildren().add(bottomRow);
        
        scrollPane.setContent(content);
        return scrollPane;
    }
    
    private VBox createWelcomeHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        User user = authService.getCurrentUser();
        welcomeLabel = new Label("Welcome back, " + user.getUsername() + "! 👋");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(Font.font("Arial", 14));
        dateLabel.setTextFill(Color.GRAY);
        
        header.getChildren().addAll(welcomeLabel, dateLabel);
        return header;
    }
    
    private GridPane createStatsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10));
        
        // Level Card
        VBox levelCard = createStatCard("Current Level", "level");
        levelLabel = new Label();
        levelProgress = new ProgressBar();
        xpLabel = new Label();
        levelCard.getChildren().addAll(levelLabel, levelProgress, xpLabel);
        
        // Streak Card
        VBox streakCard = createStatCard("Daily Streak", "streak");
        streakLabel = new Label();
        streakCard.getChildren().add(streakLabel);
        
        // Today's Stats Card
        VBox todayCard = createStatCard("Today's Progress", "today");
        todayTasksLabel = new Label();
        todayTimeLabel = new Label();
        todayCard.getChildren().addAll(todayTasksLabel, todayTimeLabel);
        
        // Productivity Score Card
        VBox scoreCard = createStatCard("Productivity Score", "score");
        productivityScoreLabel = new Label();
        scoreCard.getChildren().add(productivityScoreLabel);
        
        grid.add(levelCard, 0, 0);
        grid.add(streakCard, 1, 0);
        grid.add(todayCard, 2, 0);
        grid.add(scoreCard, 3, 0);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(25);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);
        
        return grid;
    }
    
    private VBox createStatCard(String title, String type) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.GRAY);
        
        card.getChildren().add(titleLabel);
        return card;
    }
    
    private VBox createTrendChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        container.setPrefWidth(700);
        
        Label title = new Label("Productivity Trend (Last 7 Days)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Create line chart
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 20);
        trendChart = new LineChart<>(xAxis, yAxis);
        trendChart.setTitle("Productivity Score");
        trendChart.setLegendVisible(false);
        trendChart.setPrefHeight(300);
        trendChart.setPrefWidth(650);
        
        container.getChildren().addAll(title, trendChart);
        return container;
    }
    
    private VBox createCategoryChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        container.setPrefWidth(400);
        
        Label title = new Label("Task Distribution");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        categoryChart = new PieChart();
        categoryChart.setPrefHeight(300);
        categoryChart.setPrefWidth(350);
        categoryChart.setLabelsVisible(true);
        categoryChart.setLegendVisible(true);
        
        container.getChildren().addAll(title, categoryChart);
        return container;
    }
    
    private VBox createUpcomingTasks() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        container.setPrefWidth(550);
        
        Label title = new Label("Upcoming Tasks");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        upcomingTasksList = new ListView<>();
        upcomingTasksList.setPrefHeight(250);
        upcomingTasksList.setCellFactory(list -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String deadline = task.getDeadline() != null ? 
                        task.getDeadline().format(DateTimeFormatter.ofPattern("MMM dd")) : "No deadline";
                    setText(task.getTitle() + " - Due: " + deadline);
                    if (task.isOverdue()) {
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        container.getChildren().addAll(title, upcomingTasksList);
        return container;
    }
    
    private VBox createRecentAchievements() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        container.setPrefWidth(550);
        
        Label title = new Label("Recent Achievements");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        recentAchievementsList = new ListView<>();
        recentAchievementsList.setPrefHeight(250);
        
        container.getChildren().addAll(title, recentAchievementsList);
        return container;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(10, 20, 10, 20));
        statusBar.setStyle("-fx-background-color: #2c3e50;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        Label statusLabel = new Label("✅ System Online | SyncTrack v1.0");
        statusLabel.setTextFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label tipLabel = new Label("💡 Tip: Complete tasks daily to maintain your streak!");
        tipLabel.setTextFill(Color.WHITE);
        tipLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        
        statusBar.getChildren().addAll(statusLabel, spacer, tipLabel);
        return statusBar;
    }
    
    private void refreshData() {
        User user = authService.getCurrentUser();
        
        // Update level info
        levelLabel.setText("Level " + user.getLevel());
        levelProgress.setProgress(user.getLevelProgress());
        xpLabel.setText(user.getXpTotal() + " / " + user.getXpForNextLevel() + " XP");
        
        // Update streak
        streakLabel.setText("🔥 " + user.getCurrentStreak() + " days");
        
        // Today's stats
        DailyStat todayStat = analyticsService.getDailyStat(user.getUserId(), LocalDate.now());
        todayTasksLabel.setText("📋 Tasks: " + todayStat.getTasksCompleted());
        long hours = todayStat.getTotalTimeSeconds() / 3600;
        long minutes = (todayStat.getTotalTimeSeconds() % 3600) / 60;
        todayTimeLabel.setText("⏱️ Time: " + hours + "h " + minutes + "m");
        
        // Productivity score
        double score = todayStat.getProductivityScore();
        productivityScoreLabel.setText(String.format("%.1f", score) + "/100");
        String scoreColor = score >= 80 ? "#4caf50" : (score >= 50 ? "#ff9800" : "#f44336");
        productivityScoreLabel.setStyle("-fx-text-fill: " + scoreColor + "; -fx-font-size: 24; -fx-font-weight: bold;");
        
        // Load charts
        loadTrendChart();
        loadCategoryChart();
        
        // Load upcoming tasks
        List<Task> tasks = taskService.getTasksByUser(user.getUserId());
        List<Task> upcoming = tasks.stream()
            .filter(t -> !"completed".equals(t.getStatus()))
            .limit(10)
            .toList();
        upcomingTasksList.setItems(FXCollections.observableArrayList(upcoming));
        
        // Load recent achievements
        List<String> achievements = gamificationService.getRecentAchievements(user.getUserId());
        recentAchievementsList.setItems(FXCollections.observableArrayList(achievements));
    }
    
    private void loadTrendChart() {
        trendChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Productivity");
        
        List<DailyStat> weekStats = analyticsService.getWeeklyStats(authService.getCurrentUser().getUserId());
        for (DailyStat stat : weekStats) {
            series.getData().add(new XYChart.Data<>(
                stat.getDate().format(DateTimeFormatter.ofPattern("MM/dd")),
                stat.getProductivityScore()
            ));
        }
        
        trendChart.getData().add(series);
    }
    
    private void loadCategoryChart() {
        categoryChart.getData().clear();
        
        Map<String, Integer> categoryCounts = taskService.getTaskCountByCategory(
            authService.getCurrentUser().getUserId()
        );
        
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            categoryChart.getData().add(slice);
        }
        
        // Style the pie chart
        for (PieChart.Data data : categoryChart.getData()) {
            data.getNode().setStyle("-fx-pie-label-visible: true;");
        }
    }
    
    private void startAutoRefresh() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(5), e -> refreshData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    public Scene getScene() {
        return scene;
    }
}
