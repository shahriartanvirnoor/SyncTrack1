// TaskController.java
package com.synctrack.controller;

import com.synctrack.SyncTrackApp;
import com.synctrack.model.Task;
import com.synctrack.model.User;
import com.synctrack.service.AuthService;
import com.synctrack.service.TaskService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TaskController {
    private final AuthService authService;
    private final TaskService taskService;
    private Scene scene;
    
    private TableView<Task> taskTable;
    private ObservableList<Task> taskList;
    private FilteredList<Task> filteredTasks;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> statusFilter;
    private TextField searchField;
    
    public TaskController(AuthService authService) {
        this.authService = authService;
        this.taskService = new TaskService();
        createScene();
    }
    
    private void createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Top Navigation Bar
        root.setTop(createNavigationBar());
        
        // Center Content
        VBox centerContent = new VBox(10);
        centerContent.setPadding(new Insets(20));
        
        // Header
        Label headerLabel = new Label("Task Management");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Action Buttons
        HBox actionBar = createActionBar();
        
        // Filters
        HBox filters = createFilters();
        
        // Task Table
        taskTable = createTaskTable();
        
        centerContent.getChildren().addAll(headerLabel, actionBar, filters, taskTable);
        root.setCenter(centerContent);
        
        // Bottom Status Bar
        root.setBottom(createStatusBar());
        
        scene = new Scene(root, 1200, 800);
    }
    
    private HBox createNavigationBar() {
        HBox navBar = new HBox(10);
        navBar.setPadding(new Insets(15, 20, 15, 20));
        navBar.setStyle("-fx-background-color: #667eea;");
        navBar.setAlignment(Pos.CENTER_LEFT);
        
        Button dashboardBtn = createNavButton("Dashboard", "#dashboard");
        Button tasksBtn = createNavButton("Tasks", "#tasks");
        Button timerBtn = createNavButton("Timer", "#timer");
        Button analyticsBtn = createNavButton("Analytics", "#analytics");
        Button achievementsBtn = createNavButton("Achievements", "#achievements");
        Button settingsBtn = createNavButton("Settings", "#settings");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userLabel = new Label(authService.getCurrentUser().getUsername());
        userLabel.setTextFill(Color.WHITE);
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> {
            authService.logout();
            SyncTrackApp.showLoginScreen();
        });
        
        navBar.getChildren().addAll(dashboardBtn, tasksBtn, timerBtn, analyticsBtn, achievementsBtn, settingsBtn, spacer, userLabel, logoutBtn);
        
        return navBar;
    }
    
    private Button createNavButton(String text, String view) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;"));
        btn.setOnAction(e -> SyncTrackApp.navigateTo(view.substring(1)));
        return btn;
    }
    
    private HBox createActionBar() {
        HBox actionBar = new HBox(10);
        actionBar.setPadding(new Insets(10, 0, 10, 0));
        
        Button newTaskBtn = new Button("+ New Task");
        newTaskBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        newTaskBtn.setOnAction(e -> showTaskDialog(null));
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-padding: 8 15;");
        editBtn.setOnAction(e -> {
            Task selected = taskTable.getSelectionModel().getSelectedItem();
            if (selected != null) showTaskDialog(selected);
            else showAlert("Please select a task to edit");
        });
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 8 15;");
        deleteBtn.setOnAction(e -> {
            Task selected = taskTable.getSelectionModel().getSelectedItem();
            if (selected != null) deleteTask(selected);
            else showAlert("Please select a task to delete");
        });
        
        Button completeBtn = new Button("Complete");
        completeBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-padding: 8 15;");
        completeBtn.setOnAction(e -> {
            Task selected = taskTable.getSelectionModel().getSelectedItem();
            if (selected != null) completeTask(selected);
            else showAlert("Please select a task to complete");
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        actionBar.getChildren().addAll(newTaskBtn, editBtn, deleteBtn, completeBtn, spacer);
        return actionBar;
    }
    
    private HBox createFilters() {
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10, 0, 10, 0));
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filters:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("All", "Work", "Personal", "Study", "Health", "Other");
        categoryFilter.setValue("All");
        categoryFilter.setOnAction(e -> applyFilters());
        
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "pending", "in_progress", "completed");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> applyFilters());
        
        searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        
        Button clearFiltersBtn = new Button("Clear");
        clearFiltersBtn.setOnAction(e -> {
            categoryFilter.setValue("All");
            statusFilter.setValue("All");
            searchField.clear();
        });
        
        filterBox.getChildren().addAll(filterLabel, categoryFilter, statusFilter, searchField, clearFiltersBtn);
        return filterBox;
    }
    
    private TableView<Task> createTaskTable() {
        TableView<Task> table = new TableView<>();
        
        TableColumn<Task, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);
        
        TableColumn<Task, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(100);
        
        TableColumn<Task, Integer> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(80);
        priorityCol.setCellFactory(col -> new TableCell<Task, Integer>() {
            @Override
            protected void updateItem(Integer priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(priority));
                    String color = getPriorityColor(priority);
                    setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
                }
            }
        });
        
        TableColumn<Task, String> difficultyCol = new TableColumn<>("Difficulty");
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        difficultyCol.setPrefWidth(100);
        
        TableColumn<Task, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    String style = switch(status) {
                        case "completed" -> "-fx-background-color: #4caf50; -fx-text-fill: white;";
                        case "in_progress" -> "-fx-background-color: #ff9800; -fx-text-fill: white;";
                        default -> "-fx-background-color: #9e9e9e; -fx-text-fill: white;";
                    };
                    setStyle(style);
                }
            }
        });
        
        TableColumn<Task, String> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        deadlineCol.setPrefWidth(150);
        
        TableColumn<Task, Integer> estTimeCol = new TableColumn<>("Est. Time (min)");
        estTimeCol.setCellValueFactory(new PropertyValueFactory<>("estimatedTime"));
        estTimeCol.setPrefWidth(100);
        
        TableColumn<Task, Long> actualTimeCol = new TableColumn<>("Actual Time (min)");
        actualTimeCol.setCellValueFactory(new PropertyValueFactory<>("actualTimeSeconds"));
        actualTimeCol.setCellFactory(col -> new TableCell<Task, Long>() {
            @Override
            protected void updateItem(Long seconds, boolean empty) {
                super.updateItem(seconds, empty);
                if (empty || seconds == null) setText("-");
                else setText(String.format("%.1f", seconds / 60.0));
            }
        });
        
        table.getColumns().addAll(titleCol, categoryCol, priorityCol, difficultyCol, statusCol, deadlineCol, estTimeCol, actualTimeCol);
        
        // Load tasks
        loadTasks();
        
        return table;
    }
    
    private void loadTasks() {
        User currentUser = authService.getCurrentUser();
        List<Task> tasks = taskService.getTasksByUser(currentUser.getUserId());
        taskList = FXCollections.observableArrayList(tasks);
        filteredTasks = new FilteredList<>(taskList, p -> true);
        taskTable.setItems(filteredTasks);
    }
    
    private void applyFilters() {
        String category = categoryFilter.getValue();
        String status = statusFilter.getValue();
        String search = searchField.getText().toLowerCase();
        
        filteredTasks.setPredicate(task -> {
            if (!category.equals("All") && !task.getCategory().equals(category)) return false;
            if (!status.equals("All") && !task.getStatus().equals(status)) return false;
            if (!search.isEmpty() && !task.getTitle().toLowerCase().contains(search)) return false;
            return true;
        });
    }
    
    private void showTaskDialog(Task existingTask) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(existingTask == null ? "Create New Task" : "Edit Task");
        dialog.setHeaderText(null);
        
        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Task title");
        if (existingTask != null) titleField.setText(existingTask.getTitle());
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);
        if (existingTask != null) descArea.setText(existingTask.getDescription());
        
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Work", "Personal", "Study", "Health", "Other");
        if (existingTask != null) categoryCombo.setValue(existingTask.getCategory());
        else categoryCombo.setValue("Work");
        
        ComboBox<Integer> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(1, 2, 3, 4, 5);
        if (existingTask != null) priorityCombo.setValue(existingTask.getPriority());
        else priorityCombo.setValue(3);
        
        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard", "Expert");
        if (existingTask != null) difficultyCombo.setValue(existingTask.getDifficulty());
        else difficultyCombo.setValue("Medium");
        
        DatePicker deadlineDate = new DatePicker();
        TextField deadlineTime = new TextField();
        deadlineTime.setPromptText("HH:MM");
        if (existingTask != null && existingTask.getDeadline() != null) {
            deadlineDate.setValue(existingTask.getDeadline().toLocalDate());
            deadlineTime.setText(existingTask.getDeadline().toLocalTime().toString().substring(0, 5));
        }
        
        TextField estTimeField = new TextField();
        estTimeField.setPromptText("Estimated time (minutes)");
        if (existingTask != null && existingTask.getEstimatedTime() != null) {
            estTimeField.setText(String.valueOf(existingTask.getEstimatedTime()));
        }
        
        grid.add(new Label("Title:*"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityCombo, 1, 3);
        grid.add(new Label("Difficulty:"), 0, 4);
        grid.add(difficultyCombo, 1, 4);
        
        HBox deadlineBox = new HBox(5, deadlineDate, deadlineTime);
        grid.add(new Label("Deadline:"), 0, 5);
        grid.add(deadlineBox, 1, 5);
        grid.add(new Label("Est. Time (min):"), 0, 6);
        grid.add(estTimeField, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        
        // Enable/disable save button
        Node saveButton = dialog.getDialogPane().lookupButton(saveBtnType);
        saveButton.setDisable(true);
        titleField.textProperty().addListener((obs, old, val) -> 
            saveButton.setDisable(val.trim().isEmpty()));
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                Task task = existingTask != null ? existingTask : new Task();
                task.setTitle(titleField.getText().trim());
                task.setDescription(descArea.getText());
                task.setCategory(categoryCombo.getValue());
                task.setPriority(priorityCombo.getValue());
                task.setDifficulty(difficultyCombo.getValue());
                task.setEstimatedTime(estTimeField.getText().isEmpty() ? null : Integer.parseInt(estTimeField.getText()));
                task.setUserId(authService.getCurrentUser().getUserId());
                
                if (deadlineDate.getValue() != null && !deadlineTime.getText().isEmpty()) {
                    LocalTime time = LocalTime.parse(deadlineTime.getText() + ":00");
                    task.setDeadline(LocalDateTime.of(deadlineDate.getValue(), time));
                }
                
                return task;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(task -> {
            if (existingTask == null) {
                taskService.createTask(task);
            } else {
                taskService.updateTask(task);
            }
            loadTasks();
        });
    }
    
    private void deleteTask(Task task) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Task");
        confirm.setHeaderText("Delete " + task.getTitle() + "?");
        confirm.setContentText("This action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            taskService.deleteTask(task.getTaskId());
            loadTasks();
        }
    }
    
    private void completeTask(Task task) {
        taskService.completeTask(task.getTaskId());
        loadTasks();
        showAlert("Task completed! +" + task.getXpEarned() + " XP earned!");
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(10, 20, 10, 20));
        statusBar.setStyle("-fx-background-color: #e0e0e0;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        Label statsLabel = new Label("Total Tasks: " + taskList.size() + 
            " | Completed: " + taskList.stream().filter(t -> "completed".equals(t.getStatus())).count());
        
        statusBar.getChildren().add(statsLabel);
        return statusBar;
    }
    
    private String getPriorityColor(int priority) {
        return switch(priority) {
            case 1 -> "#f44336";
            case 2 -> "#ff9800";
            case 3 -> "#ffc107";
            case 4 -> "#8bc34a";
            case 5 -> "#4caf50";
            default -> "#9e9e9e";
        };
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Scene getScene() {
        return scene;
    }
}
