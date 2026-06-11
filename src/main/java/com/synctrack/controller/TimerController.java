// TimerController.java
package com.synctrack.controller;

import com.synctrack.SyncTrackApp;
import com.synctrack.model.Task;
import com.synctrack.service.AuthService;
import com.synctrack.service.TaskService;
import com.synctrack.service.TimerService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;

public class TimerController {
    private final AuthService authService;
    private final TaskService taskService;
    private final TimerService timerService;
    private Scene scene;
    
    private ComboBox<Task> taskSelector;
    private Label timerLabel;
    private Button startBtn;
    private Button pauseBtn;
    private Button stopBtn;
    private Label statusLabel;
    private Timeline timeline;
    private ListView<Task> activeTasksList;
    
    public TimerController(AuthService authService) {
        this.authService = authService;
        this.taskService = new TaskService();
        this.timerService = TimerService.getInstance();
        createScene();
        startTimerUpdates();
    }
    
    private void createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(to bottom right, #667eea, #764ba2);");
        
        root.setTop(createNavigationBar());
        root.setCenter(createTimerPanel());
        root.setBottom(createActiveTasksPanel());
        
        scene = new Scene(root, 1024, 768);
    }
    
    private HBox createNavigationBar() {
        HBox navBar = new HBox(10);
        navBar.setPadding(new Insets(15, 20, 15, 20));
        navBar.setStyle("-fx-background-color: rgba(0,0,0,0.2);");
        navBar.setAlignment(Pos.CENTER_LEFT);
        
        Button[] buttons = {
            createNavButton("Dashboard", "dashboard"),
            createNavButton("Tasks", "tasks"),
            createNavButton("Timer", "timer"),
            createNavButton("Analytics", "analytics"),
            createNavButton("Achievements", "achievements"),
            createNavButton("Settings", "settings")
        };
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userLabel = new Label(authService.getCurrentUser().getUsername());
        userLabel.setTextFill(Color.WHITE);
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> {
            timerService.stopTimer();
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
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;"));
        btn.setOnAction(e -> SyncTrackApp.navigateTo(view));
        return btn;
    }
    
    private VBox createTimerPanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(50));
        
        // Timer Display
        timerLabel = new Label("00:00:00");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        timerLabel.setTextFill(Color.WHITE);
        timerLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.1, 0, 5);");
        
        // Task Selector
        Label selectLabel = new Label("Select Task:");
        selectLabel.setTextFill(Color.WHITE);
        selectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        taskSelector = new ComboBox<>();
        taskSelector.setPromptText("Choose a task to track");
        taskSelector.setPrefWidth(300);
        loadPendingTasks();
        
        // Control Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        startBtn = createControlButton("Start", "#4caf50");
        pauseBtn = createControlButton("Pause", "#ff9800");
        stopBtn = createControlButton("Stop", "#f44336");
        
        pauseBtn.setDisable(true);
        stopBtn.setDisable(true);
        
        startBtn.setOnAction(e -> startTimer());
        pauseBtn.setOnAction(e -> pauseTimer());
        stopBtn.setOnAction(e -> stopTimer());
        
        buttonBox.getChildren().addAll(startBtn, pauseBtn, stopBtn);
        
        // Status
        statusLabel = new Label("⏸ Not tracking");
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setFont(Font.font("Arial", 14));
        
        panel.getChildren().addAll(timerLabel, selectLabel, taskSelector, buttonBox, statusLabel);
        
        return panel;
    }
    
    private Button createControlButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 16;" +
            "-fx-padding: 10 25;" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + adjustBrightness(color, 0.8) + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 16;" +
            "-fx-padding: 10 25;" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 16;" +
            "-fx-padding: 10 25;" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }
    
    private String adjustBrightness(String hexColor, double factor) {
        // Simple brightness adjustment - in production use proper color manipulation
        return hexColor.equals("#4caf50") ? "#388e3c" : 
               hexColor.equals("#ff9800") ? "#f57c00" : "#d32f2f";
    }
    
    private void loadPendingTasks() {
        List<Task> tasks = taskService.getTasksByUser(authService.getCurrentUser().getUserId());
        List<Task> pendingTasks = tasks.stream()
            .filter(t -> !"completed".equals(t.getStatus()))
            .toList();
        taskSelector.setItems(FXCollections.observableArrayList(pendingTasks));
    }
    
    private void startTimer() {
        Task selected = taskSelector.getValue();
        if (selected == null) {
            showAlert("Please select a task first!");
            return;
        }
        
        timerService.startTimer(selected.getTaskId());
        startBtn.setDisable(true);
        pauseBtn.setDisable(false);
        stopBtn.setDisable(false);
        statusLabel.setText("▶ Tracking: " + selected.getTitle());
        statusLabel.setTextFill(Color.web("#4caf50"));
    }
    
    private void pauseTimer() {
        timerService.pauseTimer();
        startBtn.setDisable(false);
        pauseBtn.setDisable(true);
        statusLabel.setText("⏸ Paused");
        statusLabel.setTextFill(Color.web("#ff9800"));
    }
    
    private void stopTimer() {
        long duration = timerService.stopTimer();
        startBtn.setDisable(false);
        pauseBtn.setDisable(true);
        stopBtn.setDisable(true);
        statusLabel.setText("✓ Session completed: " + formatDuration(duration));
        statusLabel.setTextFill(Color.web("#4caf50"));
        
        // Refresh task list
        loadPendingTasks();
        
        showAlert("Time logged successfully!\nDuration: " + formatDuration(duration));
    }
    
    private void startTimerUpdates() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimerDisplay()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
    
    private void updateTimerDisplay() {
        if (timerService.isTimerRunning()) {
            long duration = timerService.getCurrentSessionDuration();
            timerLabel.setText(formatDuration(duration));
        } else if (!timerService.isTimerRunning() && timerLabel.getText().equals("00:00:00")) {
            // Keep as is
        }
    }
    
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
    
    private VBox createActiveTasksPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.9);");
        
        Label title = new Label("Active Tasks");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        activeTasksList = new ListView<>();
        activeTasksList.setPrefHeight(150);
        refreshActiveTasks();
        
        panel.getChildren().addAll(title, activeTasksList);
        return panel;
    }
    
    private void refreshActiveTasks() {
        List<Task> tasks = taskService.getTasksByUser(authService.getCurrentUser().getUserId());
        List<Task> activeTasks = tasks.stream()
            .filter(t -> "in_progress".equals(t.getStatus()))
            .toList();
        activeTasksList.setItems(FXCollections.observableArrayList(activeTasks));
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Timer");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Scene getScene() {
        return scene;
    }
}
