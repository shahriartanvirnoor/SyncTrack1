// TimeLogRepository.java
package com.synctrack.repository;

import com.synctrack.model.TimeLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimeLogRepository {
    private DatabaseConnection dbConnection;
    
    public TimeLogRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public TimeLog save(TimeLog timeLog) {
        String sql = "INSERT INTO time_logs (task_id, start_time, is_active) VALUES (?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, timeLog.getTaskId());
            pstmt.setString(2, timeLog.getStartTime().toString());
            pstmt.setBoolean(3, timeLog.isActive());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    timeLog.setLogId(rs.getInt(1));
                }
            }
            return timeLog;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save time log", e);
        }
    }
    
    public void update(TimeLog timeLog) {
        String sql = "UPDATE time_logs SET end_time = ?, duration_seconds = ?, is_active = ? WHERE log_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, timeLog.getEndTime() != null ? timeLog.getEndTime().toString() : null);
            pstmt.setLong(2, timeLog.getDurationSeconds());
            pstmt.setBoolean(3, timeLog.isActive());
            pstmt.setInt(4, timeLog.getLogId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update time log", e);
        }
    }
    
    public List<TimeLog> findByTaskId(int taskId) {
        String sql = "SELECT * FROM time_logs WHERE task_id = ? ORDER BY start_time DESC";
        List<TimeLog> logs = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToTimeLog(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get time logs", e);
        }
        return logs;
    }
    
    public long getTotalTimeForTask(int taskId) {
        String sql = "SELECT COALESCE(SUM(duration_seconds), 0) as total FROM time_logs WHERE task_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("total");
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get total time", e);
        }
    }
    
    private TimeLog mapResultSetToTimeLog(ResultSet rs) throws SQLException {
        TimeLog log = new TimeLog();
        log.setLogId(rs.getInt("log_id"));
        log.setTaskId(rs.getInt("task_id"));
        
        String startTime = rs.getString("start_time");
        if (startTime != null) log.setStartTime(LocalDateTime.parse(startTime));
        
        String endTime = rs.getString("end_time");
        if (endTime != null) log.setEndTime(LocalDateTime.parse(endTime));
        
        log.setDurationSeconds(rs.getLong("duration_seconds"));
        log.setActive(rs.getBoolean("is_active"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) log.setCreatedAt(LocalDateTime.parse(createdAt));
        
        return log;
    }
}
