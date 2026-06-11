package com.synctrack.repository;

import com.synctrack.model.DailyStat;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatsRepository {
    private DatabaseConnection db = DatabaseConnection.getInstance();
    
    public DailyStat getDailyStat(int userId, LocalDate date) {
        String sql = "SELECT * FROM daily_stats WHERE user_id = ? AND date = ?";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
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
            DailyStat empty = new DailyStat();
            empty.setUserId(userId);
            empty.setDate(date);
            return empty;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void updateDailyStat(DailyStat stat) {
        String sql = "INSERT OR REPLACE INTO daily_stats (user_id, date, tasks_completed, total_time_seconds, xp_earned, productivity_score) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, stat.getUserId());
            pstmt.setString(2, stat.getDate().toString());
            pstmt.setInt(3, stat.getTasksCompleted());
            pstmt.setLong(4, stat.getTotalTimeSeconds());
            pstmt.setInt(5, stat.getXpEarned());
            pstmt.setDouble(6, stat.getProductivityScore());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<DailyStat> getWeeklyStats(int userId) {
        List<DailyStat> stats = new ArrayList<>();
        String sql = "SELECT * FROM daily_stats WHERE user_id = ? AND date >= date('now', '-7 days') ORDER BY date ASC";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                DailyStat stat = new DailyStat();
                stat.setDate(LocalDate.parse(rs.getString("date")));
                stat.setTasksCompleted(rs.getInt("tasks_completed"));
                stat.setTotalTimeSeconds(rs.getLong("total_time_seconds"));
                stat.setXpEarned(rs.getInt("xp_earned"));
                stat.setProductivityScore(rs.getDouble("productivity_score"));
                stats.add(stat);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return stats;
    }
}
