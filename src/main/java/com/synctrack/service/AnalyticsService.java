// AnalyticsService.java
package com.synctrack.repository;

import com.synctrack.model.DailyStat;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatsRepository {
    private DatabaseConnection dbConnection;
    
    public StatsRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public DailyStat getDailyStat(int userId, LocalDate date) {
        String sql = "SELECT * FROM daily_stats WHERE user_id = ? AND date = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapToDailyStat(rs);
            } else {
                // Return empty stat
                DailyStat stat = new DailyStat();
                stat.setUserId(userId);
                stat.setDate(date);
                stat.setTasksCompleted(0);
                stat.setTotalTimeSeconds(0);
                stat.setXpEarned(0);
                stat.setProductivityScore(0);
                return stat;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get daily stat", e);
        }
    }
    
    public void updateDailyStat(DailyStat stat) {
        String sql = "INSERT OR REPLACE INTO daily_stats (user_id, date, tasks_completed, total_time_seconds, xp_earned, productivity_score) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, stat.getUserId());
            pstmt.setString(2, stat.getDate().toString());
            pstmt.setInt(3, stat.getTasksCompleted());
            pstmt.setLong(4, stat.getTotalTimeSeconds());
            pstmt.setInt(5, stat.getXpEarned());
            pstmt.setDouble(6, stat.getProductivityScore());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update daily stat", e);
        }
    }
    
    public List<DailyStat> getWeeklyStats(int userId) {
        String sql = "SELECT * FROM daily_stats WHERE user_id = ? AND date >= date('now', '-7 days') ORDER BY date ASC";
        return getStats(userId, sql);
    }
    
    public List<DailyStat> getMonthlyStats(int userId) {
        String sql = "SELECT * FROM daily_stats WHERE user_id = ? AND date >= date('now', '-30 days') ORDER BY date ASC";
        return getStats(userId, sql);
    }
    
    public List<DailyStat> getYearlyStats(int userId) {
        String sql = "SELECT * FROM daily_stats WHERE user_id = ? AND date >= date('now', '-365 days') ORDER BY date ASC";
        return getStats(userId, sql);
    }
    
    private List<DailyStat> getStats(int userId, String sql) {
        List<DailyStat> stats = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                stats.add(mapToDailyStat(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get stats", e);
        }
        return stats;
    }
    
    private DailyStat mapToDailyStat(ResultSet rs) throws SQLException {
        DailyStat stat = new DailyStat();
        stat.setStatId(rs.getInt("stat_id"));
        stat.setUserId(rs.getInt("user_id"));
        stat.setDate(LocalDate.parse(rs.getString("date")));
        stat.setTasksCompleted(rs.getInt("tasks_completed"));
        stat.setTotalTimeSeconds(rs.getLong("total_time_seconds"));
        stat.setXpEarned(rs.getInt("xp_earned"));
        stat.setProductivityScore(rs.getDouble("productivity_score"));
        return stat;
    }
}
