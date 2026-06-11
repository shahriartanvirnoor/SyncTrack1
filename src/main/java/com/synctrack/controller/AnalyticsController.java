// AnalyticsController.java
package com.synctrack.controller;

import com.synctrack.SyncTrackApp;
import com.synctrack.model.DailyStat;
import com.synctrack.model.Task;
import com.synctrack.model.User;
import com.synctrack.service.AnalyticsService;
import com.synctrack.service.AuthService;
import com.synctrack.service.TaskService;
import javafx.beans.property.SimpleStringProperty;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AnalyticsController {
    private final AuthService authService;
    private final AnalyticsService analyticsService;
    private final TaskService taskService;
    private Scene scene;
    
    private ComboBox<String> periodSelector;
    private Label totalTasksLabel;
    private Label totalTimeLabel;
    private Label avgProductivityLabel;
    private Label bestDayLabel;
    private BarChart<String, Number> barChart;
    private LineChart<String, Number> lineChart;
    private TableView<DailyStat> statsTable;
    
    public AnalyticsController(AuthService authService) {
        this.authService = authService;
        this.analyticsService = new AnalyticsService();
        this.taskService = new TaskService();
        createScene();
        loadData("weekly");
    }
    
    private void createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        root.setTop(createNavigationBar());
        root.setCenter(createMainContent());
        
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
    
    private ScrollPane createMainContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Header
        content.getChildren().add(createHeader());
        
        // Summary Cards
        content.getChildren().add(createSummaryCards());
        
        // Charts
        content.getChildren().add(createChartsSection());
        
        // Detailed Table
        content.getChildren().add(createDetailedTable());
        
        scrollPane.setContent(content);
        return scrollPane;
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        Label title = new Label("Performance Analytics");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        Label periodLabel = new Label("Time Period:");
        periodLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        periodSelector = new ComboBox<>();
        periodSelector.getItems().addAll("weekly", "monthly", "yearly");
        periodSelector.setValue("weekly");
        periodSelector.setOnAction(e -> loadData(periodSelector.getValue()));
        
        controls.getChildren().addAll(periodLabel, periodSelector);
        
        header.getChildren().addAll(title, controls);
        return header;
    }
    
    private GridPane createSummaryCards() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10));
        
        // Total Tasks Card
        VBox tasksCard = createMetricCard("Total Tasks Completed", "tasks");
        totalTasksLabel = new Label("0");
        tasksCard.getChildren().add(totalTasksLabel);
        
        // Total Time Card
        VBox timeCard = createMetricCard("Total Time Tracked", "time");
        totalTimeLabel = new Label("0h");
        timeCard.getChildren().add(totalTimeLabel);
        
        // Avg Productivity Card
        VBox productivityCard = createMetricCard("Average Productivity", "productivity");
        avgProductivityLabel = new Label("0");
        productivityCard.getChildren().add(avgProductivityLabel);
        
        // Best Day Card
        VBox bestDayCard = createMetricCard("Best Day", "best");
        bestDayLabel = new Label("-");
        bestDayCard.getChildren().add(bestDayLabel);
        
        grid.add(tasksCard, 0, 0);
        grid.add(timeCard, 1, 0);
        grid.add(productivityCard, 2, 0);
        grid.add(bestDayCard, 3, 0);
        
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
    
    private VBox createMetricCard(String title, String type) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.GRAY);
        
        card.getChildren().add(titleLabel);
        return card;
    }
    
    private VBox createChartsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        Label title = new Label("Performance Trends");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        // Bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Tasks Completed");
        barChart.setPrefHeight(350);
        
        // Line Chart
        NumberAxis xAxis2 = new NumberAxis();
        NumberAxis yAxis2 = new NumberAxis(0, 100, 20);
        lineChart = new LineChart<>(xAxis2, yAxis2);
        lineChart.setTitle("Productivity Score Trend");
        lineChart.setPrefHeight(350);
        lineChart.setLegendVisible(false);
        
        HBox chartsBox = new HBox(20);
        chartsBox.getChildren().addAll(barChart, lineChart);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        HBox.setHgrow(lineChart, Priority.ALWAYS);
        
        section.getChildren().addAll(title, chartsBox);
        return section;
    }
    
    private VBox createDetailedTable() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        Label title = new Label("Daily Breakdown");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        statsTable = new TableView<>();
        statsTable.setPrefHeight(300);
        
        TableColumn<DailyStat, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate().toString()));
        dateCol.setPrefWidth(150);
        
        TableColumn<DailyStat, Integer> tasksCol = new TableColumn<>("Tasks Completed");
        tasksCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getTasksCompleted())));
        tasksCol.setPrefWidth(150);
        
        TableColumn<DailyStat, String> timeCol = new TableColumn<>("Time Tracked");
        timeCol.setCellValueFactory(cellData -> {
            long hours = cellData.getValue().getTotalTimeSeconds() / 3600;
            long minutes = (cellData.getValue().getTotalTimeSeconds() % 3600) / 60;
            return new SimpleStringProperty(hours + "h " + minutes + "m");
        });
        timeCol.setPrefWidth(150);
        
        TableColumn<DailyStat, Integer> xpCol = new TableColumn<>("XP Earned");
        xpCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getXpEarned())));
        xpCol.setPrefWidth(150);
        
        TableColumn<DailyStat, Double> scoreCol = new TableColumn<>("Productivity Score");
        scoreCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.1f", cellData.getValue().getProductivityScore())));
        scoreCol.setPrefWidth(150);
        
        statsTable.getColumns().addAll(dateCol, tasksCol, timeCol, xpCol, scoreCol);
        
        section.getChildren().addAll(title, statsTable);
        return section;
    }
    
    private void loadData(String period) {
        User user = authService.getCurrentUser();
        List<DailyStat> stats;
        
        switch (period) {
            case "weekly":
                stats = analyticsService.getWeeklyStats(user.getUserId());
                break;
            case "monthly":
                stats = analyticsService.getMonthlyStats(user.getUserId());
                break;
            default:
                stats = analyticsService.getYearlyStats(user.getUserId());
        }
        
        // Update summary cards
        updateSummaryCards(stats);
        
        // Update charts
        updateCharts(stats);
        
        // Update table
        statsTable.setItems(FXCollections.observableArrayList(stats));
    }
    
    private void updateSummaryCards(List<DailyStat> stats) {
        int totalTasks = stats.stream().mapToInt(DailyStat::getTasksCompleted).sum();
        long totalSeconds = stats.stream().mapToLong(DailyStat::getTotalTimeSeconds).sum();
        double avgScore = stats.stream().mapToDouble(DailyStat::getProductivityScore).average().orElse(0);
        
        totalTasksLabel.setText(String.valueOf(totalTasks));
        totalTasksLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        
        long hours = totalSeconds / 3600;
        totalTimeLabel.setText(hours + " hours");
        totalTimeLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #4caf50;");
        
        avgProductivityLabel.setText(String.format("%.1f", avgScore));
        avgProductivityLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #ff9800;");
        
        // Find best day
        DailyStat bestDay = stats.stream()
            .max((a, b) -> Double.compare(a.getProductivityScore(), b.getProductivityScore()))
            .orElse(null);
        
        if (bestDay != null) {
            bestDayLabel.setText(bestDay.getDate().toString());
            bestDayLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #9c27b0;");
        }
    }
    
    private void updateCharts(List<DailyStat> stats) {
        // Update bar chart
        barChart.getData().clear();
        
        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.setName("Tasks Completed");
        
        for (DailyStat stat : stats) {
            barSeries.getData().add(new XYChart.Data<>(
                stat.getDate().format(DateTimeFormatter.ofPattern("MM/dd")),
                stat.getTasksCompleted()
            ));
        }
        
        barChart.getData().add(barSeries);
        
        // Update line chart
        lineChart.getData().clear();
        
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        lineSeries.setName("Productivity Score");
        
        for (int i = 0; i < stats.size(); i++) {
            lineSeries.getData().add(new XYChart.Data<>(
                String.valueOf(i + 1),
                stats.get(i).getProductivityScore()
            ));
        }
        
        lineChart.getData().add(lineSeries);
    }
    
    public Scene getScene() {
        return scene;
    }
}
