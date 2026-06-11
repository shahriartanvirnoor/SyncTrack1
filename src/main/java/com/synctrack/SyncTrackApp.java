// SyncTrackApp.java
package com.synctrack;

import com.synctrack.controller.LoginController;
import com.synctrack.repository.DatabaseConnection;
import com.synctrack.service.AuthService;
import com.synctrack.service.TimerService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SyncTrackApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(SyncTrackApp.class);
    private static Stage primaryStage;
    private static AuthService authService;
    private static Scene currentScene;
    
    @Override
    public void init() throws Exception {
        super.init();
        logger.info("Initializing SyncTrack Application");
        
        // Initialize database
        DatabaseConnection.getInstance();
        
        // Initialize services
        authService = new AuthService();
    }
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("SyncTrack - Productivity Suite");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        
        // Set icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/app-icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            logger.warn("Icon not found");
        }
        
        showLoginScreen();
        
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            handleExit();
        });
        
        primaryStage.show();
    }
    
    public static void showLoginScreen() {
        LoginController loginController = new LoginController(authService);
        currentScene = loginController.getScene();
        primaryStage.setScene(currentScene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
    }
    
    public static void showMainDashboard() {
        com.synctrack.controller.DashboardController controller = 
            new com.synctrack.controller.DashboardController(authService);
        currentScene = controller.getScene();
        primaryStage.setScene(currentScene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
    }
    
    public static void navigateTo(String viewName) {
        switch (viewName) {
            case "dashboard":
                showMainDashboard();
                break;
            case "tasks":
                com.synctrack.controller.TaskController taskController = 
                    new com.synctrack.controller.TaskController(authService);
                currentScene = taskController.getScene();
                primaryStage.setScene(currentScene);
                break;
            case "timer":
                com.synctrack.controller.TimerController timerController = 
                    new com.synctrack.controller.TimerController(authService);
                currentScene = timerController.getScene();
                primaryStage.setScene(currentScene);
                break;
            case "analytics":
                com.synctrack.controller.AnalyticsController analyticsController = 
                    new com.synctrack.controller.AnalyticsController(authService);
                currentScene = analyticsController.getScene();
                primaryStage.setScene(currentScene);
                break;
            case "achievements":
                com.synctrack.controller.AchievementsController achController = 
                    new com.synctrack.controller.AchievementsController(authService);
                currentScene = achController.getScene();
                primaryStage.setScene(currentScene);
                break;
            case "settings":
                com.synctrack.controller.SettingsController settingsController = 
                    new com.synctrack.controller.SettingsController(authService);
                currentScene = settingsController.getScene();
                primaryStage.setScene(currentScene);
                break;
        }
    }
    
    private void handleExit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Exit SyncTrack");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Any running timers will be paused automatically.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (authService.isAuthenticated()) {
                TimerService.getInstance().pauseTimer();
            }
            Platform.exit();
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
