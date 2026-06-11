// ChartUtil.java
package com.synctrack.util;

import com.synctrack.model.DailyStat;
import com.synctrack.model.Task;
import com.synctrack.model.User;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ChartUtil - Comprehensive charting utilities for SyncTrack analytics
 * Provides factory methods for creating various types of charts with consistent styling
 */
public class ChartUtil {
    
    // ==================== CONSTANTS ====================
    
    private static final String[] CHART_COLORS = {
        "#667eea", "#4caf50", "#ff9800", "#f44336", "#9c27b0",
        "#00bcd4", "#e91e63", "#8bc34a", "#ff5722", "#3f51b5",
        "#009688", "#cddc39", "#ffc107", "#795548", "#607d8b"
    };
    
    private static final String BACKGROUND_COLOR = "#ffffff";
    private static final String GRID_LINE_COLOR = "#e0e0e0";
    private static final String AXIS_LABEL_COLOR = "#666666";
    private static final String CHART_TITLE_COLOR = "#333333";
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");
    
    // ==================== PRODUCTIVITY CHARTS ====================
    
    /**
     * Create a productivity trend line chart
     * @param stats List of daily statistics
     * @param period Period label (Week, Month, Year)
     * @return Configured LineChart
     */
    public static LineChart<String, Number> createProductivityTrendChart(List<DailyStat> stats, String period) {
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Productivity Score");
        yAxis.setTickLabelFill(Color.web(AXIS_LABEL_COLOR));
        
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(period);
        xAxis.setTickLabelFill(Color.web(AXIS_LABEL_COLOR));
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Productivity Trend");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setLegendVisible(false);
        chart.setAnimated(true);
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Productivity Score");
        
        for (DailyStat stat : stats) {
            String label = stat.getDate().format(DATE_FORMATTER);
            series.getData().add(new XYChart.Data<>(label, stat.getProductivityScore()));
        }
        
        chart.getData().add(series);
        
        // Add data point styling
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-background-color: #667eea, white; -fx-background-radius: 5px;");
            Tooltip tooltip = new Tooltip(String.format("Score: %.1f", data.getYValue().doubleValue()));
            Tooltip.install(data.getNode(), tooltip);
        }
        
        applyChartStyling(chart);
        return chart;
    }
    
    /**
     * Create productivity comparison bar chart (actual vs target)
     */
    public static BarChart<String, Number> createProductivityComparisonChart(
            List<DailyStat> stats, String period) {
        
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(period);
        NumberAxis yAxis = new NumberAxis(0, 100, 20);
        yAxis.setLabel("Score");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Productivity Comparison");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
        actualSeries.setName("Actual");
        
        XYChart.Series<String, Number> targetSeries = new XYChart.Series<>();
        targetSeries.setName("Target (70)");
        
        for (DailyStat stat : stats) {
            String label = stat.getDate().format(DATE_FORMATTER);
            actualSeries.getData().add(new XYChart.Data<>(label, stat.getProductivityScore()));
            targetSeries.getData().add(new XYChart.Data<>(label, 70.0));
        }
        
        chart.getData().addAll(actualSeries, targetSeries);
        applyBarChartStyling(chart);
        
        return chart;
    }
    
    // ==================== TASK CHARTS ====================
    
    /**
     * Create task distribution pie chart by category
     */
    public static PieChart createTaskDistributionChart(Map<String, Integer> categoryCounts) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            pieData.add(slice);
            colorIndex++;
        }
        
        PieChart chart = new PieChart(pieData);
        chart.setTitle("Task Distribution by Category");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setLabelsVisible(true);
        chart.setLabelLineLength(10);
        chart.setLegendVisible(true);
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Apply colors
        colorIndex = 0;
        for (PieChart.Data data : chart.getData()) {
            String color = CHART_COLORS[colorIndex % CHART_COLORS.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            Tooltip tooltip = new Tooltip(String.format("%s: %d tasks (%.1f%%)", 
                data.getName(), (int)data.getPieValue(), 
                (data.getPieValue() / pieData.stream().mapToDouble(PieChart.Data::getPieValue).sum()) * 100));
            Tooltip.install(data.getNode(), tooltip);
            colorIndex++;
        }
        
        return chart;
    }
    
    /**
     * Create task completion trend chart
     */
    public static LineChart<String, Number> createTaskCompletionTrendChart(List<DailyStat> stats) {
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Tasks Completed");
        yAxis.setTickLabelFill(Color.web(AXIS_LABEL_COLOR));
        
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        xAxis.setTickLabelFill(Color.web(AXIS_LABEL_COLOR));
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Task Completion Trend");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks Completed");
        
        for (DailyStat stat : stats) {
            String label = stat.getDate().format(DATE_FORMATTER);
            series.getData().add(new XYChart.Data<>(label, stat.getTasksCompleted()));
        }
        
        chart.getData().add(series);
        
        // Add area fill for better visualization
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-background-color: #4caf50, white; -fx-background-radius: 5px;");
        }
        
        applyChartStyling(chart);
        return chart;
    }
    
    /**
     * Create task priority breakdown chart (horizontal bar chart)
     */
    public static BarChart<String, Number> createTaskPriorityChart(Map<Integer, Integer> priorityCounts) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Priority Level (1=Highest, 5=Lowest)");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Tasks");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Tasks by Priority");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Task Count");
        
        String[] priorityLabels = {"Priority 1", "Priority 2", "Priority 3", "Priority 4", "Priority 5"};
        for (int i = 1; i <= 5; i++) {
            int count = priorityCounts.getOrDefault(i, 0);
            series.getData().add(new XYChart.Data<>(priorityLabels[i-1], count));
        }
        
        chart.getData().add(series);
        
        // Color bars based on priority
        String[] barColors = {"#f44336", "#ff9800", "#ffc107", "#8bc34a", "#4caf50"};
        for (int i = 0; i < series.getData().size(); i++) {
            XYChart.Data<String, Number> data = series.getData().get(i);
            data.getNode().setStyle("-fx-bar-fill: " + barColors[i] + ";");
        }
        
        applyBarChartStyling(chart);
        return chart;
    }
    
    /**
     * Create task difficulty breakdown chart
     */
    public static PieChart createTaskDifficultyChart(Map<String, Integer> difficultyCounts) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        String[] difficulties = {"Easy", "Medium", "Hard", "Expert"};
        String[] colors = {"#4caf50", "#ffc107", "#ff9800", "#f44336"};
        
        for (String difficulty : difficulties) {
            int count = difficultyCounts.getOrDefault(difficulty, 0);
            if (count > 0) {
                pieData.add(new PieChart.Data(difficulty + " (" + count + ")", count));
            }
        }
        
        PieChart chart = new PieChart(pieData);
        chart.setTitle("Tasks by Difficulty");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setLabelsVisible(true);
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Apply colors
        for (int i = 0; i < chart.getData().size() && i < colors.length; i++) {
            chart.getData().get(i).getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
        }
        
        return chart;
    }
    
    // ==================== TIME TRACKING CHARTS ====================
    
    /**
     * Create time tracking bar chart (hours per day)
     */
    public static BarChart<String, Number> createTimeTrackingChart(List<DailyStat> stats) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Hours Tracked");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Daily Time Tracking");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Hours");
        
        for (DailyStat stat : stats) {
            double hours = stat.getTotalTimeSeconds() / 3600.0;
            String label = stat.getDate().format(DATE_FORMATTER);
            series.getData().add(new XYChart.Data<>(label, Math.round(hours * 10) / 10.0));
        }
        
        chart.getData().add(series);
        
        // Color bars based on hours
        for (XYChart.Data<String, Number> data : series.getData()) {
            double hours = data.getYValue().doubleValue();
            String color = hours >= 4 ? "#4caf50" : (hours >= 2 ? "#ff9800" : "#f44336");
            data.getNode().setStyle("-fx-bar-fill: " + color + ";");
            
            Tooltip tooltip = new Tooltip(String.format("%.1f hours", hours));
            Tooltip.install(data.getNode(), tooltip);
        }
        
        applyBarChartStyling(chart);
        return chart;
    }
    
    /**
     * Create category time distribution pie chart
     */
    public static PieChart createCategoryTimeChart(Map<String, Long> categoryTimeSeconds) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Long> entry : categoryTimeSeconds.entrySet()) {
            double hours = entry.getValue() / 3600.0;
            pieData.add(new PieChart.Data(entry.getKey() + String.format(" (%.1fh)", hours), entry.getValue()));
        }
        
        PieChart chart = new PieChart(pieData);
        chart.setTitle("Time Distribution by Category");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setLabelsVisible(true);
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Apply colors
        int colorIndex = 0;
        for (PieChart.Data data : chart.getData()) {
            String color = CHART_COLORS[colorIndex % CHART_COLORS.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            colorIndex++;
        }
        
        return chart;
    }
    
    // ==================== XP & GAMIFICATION CHARTS ====================
    
    /**
     * Create XP accumulation line chart
     */
    public static AreaChart<String, Number> createXpAccumulationChart(List<DailyStat> stats) {
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("XP Earned");
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        
        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle("XP Accumulation Over Time");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("XP");
        
        int cumulativeXp = 0;
        for (DailyStat stat : stats) {
            cumulativeXp += stat.getXpEarned();
            String label = stat.getDate().format(DATE_FORMATTER);
            series.getData().add(new XYChart.Data<>(label, cumulativeXp));
        }
        
        chart.getData().add(series);
        
        // Fill area with gradient
        series.getNode().setStyle("-fx-area-fill: #667eea; -fx-opacity: 0.3;");
        
        applyChartStyling(chart);
        return chart;
    }
    
    /**
     * Create level progression gauge chart
     */
    public static StackPane createLevelGauge(User user) {
        double progress = user.getLevelProgress();
        
        // Create custom gauge using JavaFX shapes
        StackPane gauge = new StackPane();
        gauge.setAlignment(Pos.CENTER);
        
        // Background circle
        javafx.scene.shape.Circle bgCircle = new javafx.scene.shape.Circle(100);
        bgCircle.setFill(Color.web("#f0f0f0"));
        bgCircle.setStroke(Color.web("#e0e0e0"));
        bgCircle.setStrokeWidth(2);
        
        // Progress arc
        javafx.scene.shape.Arc progressArc = new javafx.scene.shape.Arc();
        progressArc.setCenterX(100);
        progressArc.setCenterY(100);
        progressArc.setRadiusX(90);
        progressArc.setRadiusY(90);
        progressArc.setStartAngle(90);
        progressArc.setLength(-360 * progress);
        progressArc.setType(javafx.scene.shape.ArcType.ROUND);
        progressArc.setFill(null);
        progressArc.setStroke(Color.web("#667eea"));
        progressArc.setStrokeWidth(15);
        
        // Level label
        Label levelLabel = new Label("Level " + user.getLevel());
        levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        levelLabel.setTextFill(Color.web("#667eea"));
        
        // XP label
        Label xpLabel = new Label(user.getXpTotal() + " XP");
        xpLabel.setFont(Font.font("Arial", 12));
        xpLabel.setTextFill(Color.web("#999999"));
        
        VBox textBox = new VBox(5, levelLabel, xpLabel);
        textBox.setAlignment(Pos.CENTER);
        
        gauge.getChildren().addAll(bgCircle, progressArc, textBox);
        
        // Add animation
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                progressArc.setLength(-360 * user.getLevelProgress());
            })
        );
        timeline.setCycleCount(1);
        timeline.play();
        
        return gauge;
    }
    
    // ==================== STREAK & CONSISTENCY CHARTS ====================
    
    /**
     * Create streak heatmap calendar view
     */
    public static GridPane createStreakHeatmap(Map<LocalDate, Integer> dailyActivity, int year, int month) {
        GridPane heatmap = new GridPane();
        heatmap.setHgap(5);
        heatmap.setVgap(5);
        heatmap.setPadding(new Insets(10));
        heatmap.setStyle("-fx-background-color: " + BACKGROUND_COLOR + "; -fx-background-radius: 10;");
        
        // Add day labels
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < dayNames.length; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            dayLabel.setTextFill(Color.web(AXIS_LABEL_COLOR));
            heatmap.add(dayLabel, i, 0);
        }
        
        // Get first day of month
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int startOffset = firstDay.getDayOfWeek().getValue() - 1; // Monday=1
        
        int row = 1;
        int col = startOffset;
        
        for (int day = 1; day <= firstDay.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(year, month, day);
            int activityCount = dailyActivity.getOrDefault(date, 0);
            
            // Determine color based on activity
            String color;
            if (activityCount >= 5) color = "#4caf50";
            else if (activityCount >= 3) color = "#8bc34a";
            else if (activityCount >= 1) color = "#cddc39";
            else color = "#eeeeee";
            
            // Create day cell
            VBox cell = new VBox(5);
            cell.setAlignment(Pos.CENTER);
            cell.setPrefSize(40, 40);
            cell.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5;");
            
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            dayLabel.setTextFill(activityCount > 0 ? Color.WHITE : Color.web("#999999"));
            
            Label countLabel = new Label(activityCount + " tasks");
            countLabel.setFont(Font.font("Arial", 8));
            countLabel.setTextFill(activityCount > 0 ? Color.WHITE : Color.web("#cccccc"));
            
            cell.getChildren().addAll(dayLabel, countLabel);
            
            Tooltip tooltip = new Tooltip(String.format("%s: %d tasks completed", 
                date.format(DateTimeFormatter.ISO_LOCAL_DATE), activityCount));
            Tooltip.install(cell, tooltip);
            
            heatmap.add(cell, col, row);
            
            col++;
            if (col >= 7) {
                col = 0;
                row++;
            }
        }
        
        return heatmap;
    }
    
    /**
     * Create consistency score trend chart
     */
    public static LineChart<String, Number> createConsistencyTrendChart(List<DailyStat> stats) {
        NumberAxis yAxis = new NumberAxis(0, 100, 20);
        yAxis.setLabel("Consistency Score");
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Week");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Consistency Trend");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Consistency");
        
        // Calculate weekly consistency
        Map<Integer, List<DailyStat>> weeklyStats = stats.stream()
            .collect(Collectors.groupingBy(stat -> stat.getDate().get(WeekFields.ISO.weekOfYear())));
        
        for (Map.Entry<Integer, List<DailyStat>> entry : weeklyStats.entrySet()) {
            double avgConsistency = entry.getValue().stream()
                .mapToDouble(stat -> calculateConsistencyScore(stat))
                .average()
                .orElse(0);
            
            series.getData().add(new XYChart.Data<>("Week " + entry.getKey(), avgConsistency));
        }
        
        chart.getData().add(series);
        applyChartStyling(chart);
        
        return chart;
    }
    
    // ==================== COMPARISON & BENCHMARK CHARTS ====================
    
    /**
     * Create performance comparison radar chart
     */
    public static StackedBarChart<String, Number> createPerformanceRadar(User user, User benchmarkUser) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Metric");
        NumberAxis yAxis = new NumberAxis(0, 100, 20);
        yAxis.setLabel("Score");
        
        StackedBarChart<String, Number> chart = new StackedBarChart<>(xAxis, yAxis);
        chart.setTitle("Performance Comparison");
        chart.setTitleFill(Color.web(CHART_TITLE_COLOR));
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("You");
        
        XYChart.Series<String, Number> benchmarkSeries = new XYChart.Series<>();
        benchmarkSeries.setName("Average User");
        
        String[] metrics = {"Productivity", "Consistency", "Efficiency", "Streak", "XP Rate"};
        double[] userScores = calculateUserScores(user);
        double[] benchmarkScores = calculateBenchmarkScores(benchmarkUser);
        
        for (int i = 0; i < metrics.length; i++) {
            userSeries.getData().add(new XYChart.Data<>(metrics[i], userScores[i]));
            benchmarkSeries.getData().add(new XYChart.Data<>(metrics[i], benchmarkScores[i]));
        }
        
        chart.getData().addAll(userSeries, benchmarkSeries);
        applyBarChartStyling(chart);
        
        return chart;
    }
    
    // ==================== CUSTOM DASHBOARD WIDGETS ====================
    
    /**
     * Create a mini stats card with sparkline
     */
    public static VBox createStatsCard(String title, String value, String trend, List<Double> sparklineData) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + BACKGROUND_COLOR + "; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(200);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.web(AXIS_LABEL_COLOR));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(CHART_TITLE_COLOR));
        
        HBox trendBox = new HBox(5);
        trendBox.setAlignment(Pos.CENTER_LEFT);
        
        Label trendLabel = new Label(trend);
        trendLabel.setFont(Font.font("Arial", 11));
        
        // Add trend icon
        Label trendIcon = new Label(trend.startsWith("+") ? "▲" : (trend.startsWith("-") ? "▼" : "●"));
        trendIcon.setTextFill(trend.startsWith("+") ? Color.web("#4caf50") : 
                             (trend.startsWith("-") ? Color.web("#f44336") : Color.web("#ff9800")));
        
        trendBox.getChildren().addAll(trendIcon, trendLabel);
        
        // Create simple sparkline
        if (sparklineData != null && !sparklineData.isEmpty()) {
            HBox sparkline = createSparkline(sparklineData);
            card.getChildren().addAll(titleLabel, valueLabel, trendBox, sparkline);
        } else {
            card.getChildren().addAll(titleLabel, valueLabel, trendBox);
        }
        
        return card;
    }
    
    /**
     * Create a simple sparkline from data points
     */
    private static HBox createSparkline(List<Double> data) {
        HBox sparkline = new HBox(2);
        sparkline.setAlignment(Pos.BOTTOM_CENTER);
        sparkline.setPrefHeight(30);
        
        double max = data.stream().max(Double::compare).orElse(1.0);
        double min = data.stream().min(Double::compare).orElse(0.0);
        double range = max - min;
        
        for (int i = 0; i < data.size(); i++) {
            double value = data.get(i);
            double height = range > 0 ? ((value - min) / range) * 25 : 12.5;
            
            javafx.scene.shape.Rectangle bar = new javafx.scene.shape.Rectangle(3, Math.max(3, height));
            bar.setFill(Color.web("#667eea"));
            bar.setArcWidth(2);
            bar.setArcHeight(2);
            
            sparkline.getChildren().add(bar);
        }
        
        return sparkline;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Apply consistent styling to line/area charts
     */
    private static void applyChartStyling(Chart chart) {
        chart.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Style the plot background
        chart.lookupAll(".chart-plot-background").forEach(node -> 
            node.setStyle("-fx-background-color: transparent;"));
        
        // Style grid lines
        chart.lookupAll(".chart-vertical-grid-lines").forEach(node -> 
            node.setStyle("-fx-stroke: " + GRID_LINE_COLOR + ";"));
        chart.lookupAll(".chart-horizontal-grid-lines").forEach(node -> 
            node.setStyle("-fx-stroke: " + GRID_LINE_COLOR + ";"));
        
        // Style axis labels
        chart.lookupAll(".axis-label").forEach(node -> 
            node.setStyle("-fx-text-fill: " + AXIS_LABEL_COLOR + ";"));
        
        // Style tick labels
        chart.lookupAll(".axis-tick-label").forEach(node -> 
            node.setStyle("-fx-text-fill: " + AXIS_LABEL_COLOR + ";"));
    }
    
    /**
     * Apply consistent styling to bar charts
     */
    private static void applyBarChartStyling(BarChart<?, ?> chart) {
        applyChartStyling(chart);
        
        // Add rounded corners to bars
        chart.lookupAll(".bar").forEach(bar -> 
            bar.setStyle("-fx-background-radius: 5 5 0 0;"));
    }
    
    /**
     * Calculate consistency score for a day
     */
    private static double calculateConsistencyScore(DailyStat stat) {
        // Implementation depends on your consistency formula
        double baseScore = stat.getProductivityScore();
        double streakBonus = Math.min(20, stat.getTasksCompleted() * 2);
        return Math.min(100, baseScore + streakBonus);
    }
    
    /**
     * Calculate user scores for radar chart
     */
    private static double[] calculateUserScores(User user) {
        return new double[]{
            calculateProductivityScore(user),
            calculateConsistencyScore(user),
            calculateEfficiencyScore(user),
            Math.min(100, user.getCurrentStreak() * 2),
            calculateXpRate(user)
        };
    }
    
    /**
     * Calculate benchmark scores
     */
    private static double[] calculateBenchmarkScores(User benchmarkUser) {
        // Average user scores
        return new double[]{65, 60, 55, 70, 50};
    }
    
    private static double calculateProductivityScore(User user) {
        // Implementation based on your productivity formula
        return user.getTotalTasksCompleted() > 0 ? Math.min(100, (user.getXpTotal() / 1000.0) * 100) : 0;
    }
    
    private static double calculateConsistencyScore(User user) {
        return Math.min(100, user.getCurrentStreak() * 3.33);
    }
    
    private static double calculateEfficiencyScore(User user) {
        // Placeholder - would need actual efficiency metrics
        return 70.0;
    }
    
    private static double calculateXpRate(User user) {
        double daysSinceStart = user.getCreatedAt() != null ? 
            java.time.Duration.between(user.getCreatedAt(), LocalDate.now().atStartOfDay()).toDays() : 1;
        return daysSinceStart > 0 ? Math.min(100, user.getXpTotal() / daysSinceStart / 10) : 0;
    }
    
    // ==================== ANIMATION UTILITIES ====================
    
    /**
     * Animate chart data entry with fade-in effect
     */
    public static void animateChart(Chart chart, Duration duration) {
        chart.setOpacity(0);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, e -> chart.setOpacity(0)),
            new KeyFrame(duration, e -> chart.setOpacity(1))
        );
        timeline.setCycleCount(1);
        timeline.play();
    }
    
    /**
     * Create animated transition between chart datasets
     */
    public static void transitionChart(Chart oldChart, Chart newChart, StackPane container) {
        oldChart.setOpacity(1);
        newChart.setOpacity(0);
        
        container.getChildren().add(newChart);
        
        Timeline fadeOut = new Timeline(
            new KeyFrame(Duration.millis(300), e -> oldChart.setOpacity(0))
        );
        
        Timeline fadeIn = new Timeline(
            new KeyFrame(Duration.millis(300), e -> newChart.setOpacity(1))
        );
        
        fadeOut.setOnFinished(e -> {
            container.getChildren().remove(oldChart);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    // ==================== EXPORT UTILITIES ====================
    
    /**
     * Convert chart to image (requires snapshot)
     */
    public static void exportChartAsImage(Chart chart, String filename) {
        // Implementation would use chart.snapshot() to save as PNG
        // This requires JavaFX's SnapshotParameters
        System.out.println("Export chart to: " + filename);
    }
}

// Note: Add these imports at the top of your file:
// import javafx.animation.*;
// import javafx.collections.FXCollections;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.scene.chart.*;
// import javafx.scene.control.Label;
// import javafx.scene.control.Tooltip;
// import javafx.scene.layout.*;
// import javafx.scene.paint.Color;
// import javafx.scene.text.Font;
// import javafx.scene.text.FontWeight;
// import javafx.util.Duration;
// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;
// import java.time.temporal.WeekFields;
// import java.util.*;
// import java.util.stream.Collectors;
