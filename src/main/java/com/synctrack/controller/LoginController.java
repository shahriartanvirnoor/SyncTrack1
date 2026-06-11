package com.synctrack.controller;

import com.synctrack.SyncTrackApp;
import com.synctrack.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginController {
    private AuthService authService;
    private Scene scene;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;
    
    public LoginController(AuthService authService) {
        this.authService = authService;
        createScene();
    }
    
    private void createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(to bottom right, #667eea, #764ba2);");
        
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        
        Label title = new Label("SyncTrack");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#667eea"));
        
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-pref-height: 40; -fx-background-radius: 8;");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-pref-height: 40; -fx-background-radius: 8;");
        
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        
        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40; -fx-background-radius: 20;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());
        
        Hyperlink registerLink = new Hyperlink("Don't have an account? Register");
        registerLink.setOnAction(e -> showRegisterDialog());
        
        card.getChildren().addAll(title, usernameField, passwordField, errorLabel, loginBtn, registerLink);
        
        VBox wrapper = new VBox(card);
        wrapper.setAlignment(Pos.CENTER);
        root.setCenter(wrapper);
        
        scene = new Scene(root, 900, 600);
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        try {
            authService.login(username, password);
            SyncTrackApp.showMainDashboard();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
        }
    }
    
    private void showRegisterDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Register");
        
        ButtonType registerBtn = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerBtn, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField regUsername = new TextField();
        regUsername.setPromptText("Username");
        TextField regEmail = new TextField();
        regEmail.setPromptText("Email");
        PasswordField regPassword = new PasswordField();
        regPassword.setPromptText("Password");
        PasswordField regConfirm = new PasswordField();
        regConfirm.setPromptText("Confirm Password");
        
        grid.add(new Label("Username:"), 0, 0);
        grid.add(regUsername, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(regEmail, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(regPassword, 1, 2);
        grid.add(new Label("Confirm:"), 0, 3);
        grid.add(regConfirm, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(btn -> {
            if (btn == registerBtn) {
                try {
                    return authService.register(regUsername.getText(), regEmail.getText(), regPassword.getText());
                } catch (Exception e) {
                    errorLabel.setText(e.getMessage());
                    errorLabel.setVisible(true);
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(user -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Registration successful! Please login.");
            alert.showAndWait();
        });
    }
    
    public Scene getScene() {
        return scene;
    }
}
