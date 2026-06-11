package com.synctrack.repository;

import com.synctrack.model.Achievement;
import com.synctrack.model.UserAchievement;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AchievementRepository {
    private DatabaseConnection db = DatabaseConnection.getInstance();
    
    public List<Achievement> findAll() {
        List<Achievement> achievements = new ArrayList<>();
        String sql = "SELECT * FROM achievements";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Achievement a = new Achievement();
                a.setAchievementId(rs.getInt("achievement_id"));
                a.setName(rs.getString("name"));
                a.setDescription(rs.getString("description"));
                a.setCriteriaType(rs.getString("criteria_type"));
                a.setCriteriaValue(rs.getInt("criteria_value"));
                a.setDifficultyLevel(rs.getString("difficulty_level"));
                a.setXpReward(rs.getInt("xp_reward"));
                achievements.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return achievements;
    }
    
    public List<UserAchievement> getUserAchievements(int userId) {
        List<UserAchievement> list = new ArrayList<>();
        String sql = "SELECT * FROM user_achievements WHERE user_id = ?";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                UserAchievement ua = new UserAchievement();
                ua.setUserAchievementId(rs.getInt("user_achievement_id"));
                ua.setUserId(rs.getInt("user_id"));
                ua.setAchievementId(rs.getInt("achievement_id"));
                ua.setProgressCurrent(rs.getInt("progress_current"));
                ua.setCompleted(rs.getBoolean("is_completed"));
                String unlockedAt = rs.getString("unlocked_at");
                if (unlockedAt != null) ua.setUnlockedAt(LocalDateTime.parse(unlockedAt));
                list.add(ua);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    
    public void saveUserAchievement(UserAchievement ua) {
        String sql = "INSERT OR REPLACE INTO user_achievements (user_id, achievement_id, progress_current, is_completed, unlocked_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, ua.getUserId());
            pstmt.setInt(2, ua.getAchievementId());
            pstmt.setInt(3, ua.getProgressCurrent());
            pstmt.setBoolean(4, ua.isCompleted());
            pstmt.setString(5, ua.getUnlockedAt() != null ? ua.getUnlockedAt().toString() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public int getTodayXp(int userId) {
        String sql = "SELECT COALESCE(SUM(xp_earned), 0) FROM tasks WHERE user_id = ? AND date(completed_at) = date('now')";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }
}
