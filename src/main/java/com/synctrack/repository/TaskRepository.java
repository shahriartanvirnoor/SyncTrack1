// TaskRepository.java
package com.synctrack.repository;

import com.synctrack.model.Task;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private DatabaseConnection dbConnection;
    
    public TaskRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public Task create(Task task) {
        String sql = "INSERT INTO tasks (user_id, title, description, category, priority, difficulty, deadline, estimated_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, task.getUserId());
            pstmt.setString(2, task.getTitle());
            pstmt.setString(3, task.getDescription());
            pstmt.setString(4, task.getCategory());
            pstmt.setInt(5, task.getPriority());
            pstmt.setString(6, task.getDifficulty());
            pstmt.setString(7, task.getDeadline() != null ? task.getDeadline().toString() : null);
            pstmt.setObject(8, task.getEstimatedTime());
            pstmt.setString(9, task.getStatus());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    task.setTaskId(rs.getInt(1));
                }
            }
            return task;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create task", e);
        }
    }
    
    public Task update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, category = ?, priority = ?, difficulty = ?, deadline = ?, estimated_time = ?, status = ? WHERE task_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getCategory());
            pstmt.setInt(4, task.getPriority());
            pstmt.setString(5, task.getDifficulty());
            pstmt.setString(6, task.getDeadline() != null ? task.getDeadline().toString() : null);
            pstmt.setObject(7, task.getEstimatedTime());
            pstmt.setString(8, task.getStatus());
            pstmt.setInt(9, task.getTaskId());
            
            pstmt.executeUpdate();
            return task;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task", e);
        }
    }
    
    public void delete(int taskId) {
        String sql = "DELETE FROM tasks WHERE task_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task", e);
        }
    }
    
    public List<Task> findByUserId(int userId) {
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY priority ASC, deadline ASC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get tasks", e);
        }
        return tasks;
    }
    
    public Task findById(int taskId) {
        String sql = "SELECT * FROM tasks WHERE task_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTask(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find task", e);
        }
    }
    
    public void completeTask(int taskId, int xpEarned) {
        String sql = "UPDATE tasks SET status = 'completed', completed_at = ?, actual_time_seconds = (SELECT SUM(duration_seconds) FROM time_logs WHERE task_id = ?), xp_earned = ? WHERE task_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, taskId);
            pstmt.setInt(3, xpEarned);
            pstmt.setInt(4, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to complete task", e);
        }
    }
    
    public void addActualTime(int taskId, long seconds) {
        String sql = "UPDATE tasks SET actual_time_seconds = actual_time_seconds + ? WHERE task_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, seconds);
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add actual time", e);
        }
    }
    
    public void updateStatus(int taskId, String status) {
        String sql = "UPDATE tasks SET status = ? WHERE task_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update status", e);
        }
    }
    
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setTaskId(rs.getInt("task_id"));
        task.setUserId(rs.getInt("user_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setCategory(rs.getString("category"));
        task.setPriority(rs.getInt("priority"));
        task.setDifficulty(rs.getString("difficulty"));
        
        String deadline = rs.getString("deadline");
        if (deadline != null) task.setDeadline(LocalDateTime.parse(deadline));
        
        task.setEstimatedTime((Integer) rs.getObject("estimated_time"));
        task.setStatus(rs.getString("status"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) task.setCreatedAt(LocalDateTime.parse(createdAt));
        
        String completedAt = rs.getString("completed_at");
        if (completedAt != null) task.setCompletedAt(LocalDateTime.parse(completedAt));
        
        task.setActualTimeSeconds(rs.getLong("actual_time_seconds"));
        task.setXpEarned(rs.getInt("xp_earned"));
        
        return task;
    }
}
