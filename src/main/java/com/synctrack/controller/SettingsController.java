// SettingsController.java
package com.synctrack.controller;

import com.synctrack.SyncTrackApp;
import com.synctrack.model.User;
import com.synctrack.service.AuthService;
import com.synctrack.service.AnalyticsService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;

public class SettingsController {
    private final AuthService authService;
    private final AnalyticsService analyticsService;
    private Scene scene;
    
    private TextField usernameField;
    private TextField emailField;
    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private ComboBox<String> themeSelector;
    private CheckBox dailyRemindersCheck;
    private CheckBox achievementNotificationsCheck;
    
    public SettingsController(AuthService authService) {
        this.authService = authService;
        this.analyticsService = new AnalyticsService();
        createScene();
        loadSettings();
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
    
    private ScrollPane createMainContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Profile Settings
        content.getChildren().add(createProfileSection());
        
        // Security Settings
        content.getChildren().add(createSecuritySection());
        
        // Preferences
        content.getChildren().add(createPreferencesSection());
        
        // Data Management
        content.getChildren().add(createDataManagementSection());
        
        scrollPane.setContent(content);
        return scrollPane;
    }
    
    private VBox createProfileSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        Label title = new Label("Profile Information");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(10));
        
        User user = authService.getCurrentUser();
        
        form.add(new Label("Username:"), 0, 0);
        usernameField = new TextField(user.getUsername());
        form.add(usernameField, 1, 0);
        
        form.add(new Label("Email:"), 0, 1);
        emailField = new TextField(user.getEmail());
        form.add(emailField, 1, 1);
        
        Button updateProfileBtn = new Button("Update Profile");
        updateProfileBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        updateProfileBtn.setOnAction(e -> updateProfile());
        
        form.add(updateProfileBtn, 1, 2);
        
        section.getChildren().addAll(title, form);
        return section;
    }
    
    private VBox createSecuritySection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        Label title = new Label("Change Password");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(10));
        
        form.add(new Label("Current Password:"), 0, 0);
        currentPasswordField = new PasswordField();
        form.add(currentPasswordField, 1, 0);
        
        form.add(new Label("New Password:"), 0, 1);
        newPasswordField = new PasswordField();
        form.add(newPasswordField, 1, 1);
        
        form.add(new Label("Confirm Password:"), 0, 2);
        confirmPasswordField = new PasswordField();
        form.add(confirmPasswordField, 1, 2);
        
        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        changePasswordBtn.setOnAction(e -> changePassword());
        
        form.add(changePasswordBtn, 1, 3);
        
        section.getChildren().addAll(title, form);
        return section;
    }
    
    private VBox createPreferencesSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        Label title = new Label("Preferences");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        VBox preferences = new VBox(10);
        preferences.setPadding(new Insets(10));
        
        // Theme selector
        HBox themeBox = new HBox(15);
        themeBox.setAlignment(Pos.CENTER_LEFT);
        themeBox.getChildren().add(new Label("Theme:"));
        themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll("Light", "Dark");
        themeSelector.setValue("Light");
        themeSelector.setOnAction(e -> changeTheme());
        themeBox.getChildren().add(themeSelector);
        
        // Notifications
        dailyRemindersCheck = new CheckBox("Enable daily reminders");
        achievementNotificationsCheck = new CheckBox("Show achievement notifications");
        
        preferences.getChildren().addAll(themeBox, dailyRemindersCheck, achievementNotificationsCheck);
        
        section.getChildren().addAll(title, preferences);
        return section;
    }
    
    private VBox createDataManagementSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        Label title = new Label("Data Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        VBox actions = new VBox(10);
        actions.setPadding(new Insets(10));
        
        Button exportDataBtn = new Button("📥 Export All Data (CSV)");
        exportDataBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        exportDataBtn.setOnAction(e -> exportData());
        
        Button resetProgressBtn = new Button("⚠️ Reset All Progress");
        resetProgressBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        resetProgressBtn.setOnAction(e -> confirmResetProgress());
        
        actions.getChildren().addAll(exportDataBtn, resetProgressBtn);
        
        section.getChildren().addAll(title, actions);
        return section;
    }
    
    private void loadSettings() {
        User user = authService.getCurrentUser();
        themeSelector.setValue(user.getThemePreference().equals("dark") ? "Dark" : "Light");
        // Load other preferences from database if stored
    }
    
    private void updateProfile() {
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();
        
        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            showAlert("Username and email cannot be empty");
            return;
        }
        
        User user = authService.getCurrentUser();
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        
        authService.updateUser(user);
        showAlert("Profile updated successfully!");
    }
    
    private void changePassword() {
        String currentPwd = currentPasswordField.getText();
        String newPwd = newPasswordField.getText();
        String confirmPwd = confirmPasswordField.getText();
        
        if (!authService.verifyPassword(currentPwd)) {
            showAlert("Current password is incorrect");
            return;
        }
        
        if (newPwd.isEmpty()) {
            showAlert("New password cannot be empty");
            return;
        }
        
        if (!newPwd.equals(confirmPwd)) {
            showAlert("New passwords do not match");
            return;
        }
        
        if (newPwd.length() < 6) {
            showAlert("Password must be at least 6 characters");
            return;
        }
        
        authService.changePassword(newPwd);
        showAlert("Password changed successfully!");
        
        // Clear fields
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
    
    private void changeTheme() {
        String theme = themeSelector.getValue().toLowerCase();
        User user = authService.getCurrentUser();
        user.setThemePreference(theme);
        authService.updateUser(user);
        
        // Apply theme (in production, this would load CSS)
        scene.getRoot().setStyle(theme.equals("dark") ? 
            "-fx-base: #2c3e50; -fx-background: #1a1a2e;" : 
            "-fx-base: #f5f5f5; -fx-background: #f5f5f5;");
        
        showAlert("Theme changed to " + theme + " mode");
    }
    
    private void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Export File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("synctrack_export_" + System.currentTimeMillis() + ".csv");
        
        File file = fileChooser.showSaveDialog(scene.getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Export tasks
                writer.println("TASKS EXPORT");
                writer.println("Task ID,Title,Category,Priority,Difficulty,Status,Estimated Time,Actual Time,Completed");
                
                List<Task> tasks = taskService.getTasksByUser(authService.getCurrentUser().getUserId());
                for (Task task : tasks) {
                    writer.printf("%d,\"%s\",%s,%d,%s,%s,%d,%d,%s%n",
                        task.getTaskId(),
                        task.getTitle(),
                        task.getCategory(),
                        task.getPriority(),
                        task.getDifficulty(),
                        task.getStatus(),
                        task.getEstimatedTime() != null ? task.getEstimatedTime() : 0,
                        task.getActualTimeSeconds() / 60,
                        task.getCompletedAt() != null ? "Yes" : "No"
                    );
                }
                
                showAlert("Data exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Export failed: " + e.getMessage());
            }
        }
    }
    
    private void confirmResetProgress() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset All Progress");
        confirm.setHeaderText("⚠️ WARNING: This action cannot be undone!");
        confirm.setContentText("This will delete all tasks, time logs, and achievements. Continue?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            authService.resetProgress();
            showAlert("All progress has been reset.");
            SyncTrackApp.showMainDashboard(); // Refresh
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Scene getScene() {
        return scene;
    }
}
