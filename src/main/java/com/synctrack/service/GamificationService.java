// GamificationService.java
package com.synctrack.repository;

import com.synctrack.model.Achievement;
import com.synctrack.model.UserAchievement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AchievementRepository {
    private DatabaseConnection dbConnection;
    
    public AchievementRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public List<Achievement> findAll() {
        String sql = "SELECT * FROM achievements ORDER BY criteria_value ASC";
        List<Achievement> achievements = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                achievements.add(mapToAchievement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return achievements;
    }
    
    public Achievement findById(int id) {
        String sql = "SELECT * FROM achievements WHERE achievement_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapToAchievement(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<UserAchievement> getUserAchievements(int userId) {
        String sql = "SELECT * FROM user_achievements WHERE user_id = ?";
        List<UserAchievement> userAchievements = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
                if (unlockedAt != null) {
                    ua.setUnlockedAt(java.time.LocalDateTime.parse(unlockedAt));
                }
                
                userAchievements.add(ua);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userAchievements;
    }
    
    public void saveUserAchievement(UserAchievement userAchievement) {
        String sql = "INSERT OR REPLACE INTO user_achievements (user_id, achievement_id, progress_current, is_completed, unlocked_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userAchievement.getUserId());
            pstmt.setInt(2, userAchievement.getAchievementId());
            pstmt.setInt(3, userAchievement.getProgressCurrent());
            pstmt.setBoolean(4, userAchievement.isCompleted());
            pstmt.setString(5, userAchievement.getUnlockedAt() != null ? 
                userAchievement.getUnlockedAt().toString() : null);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public int countTasksTodayByCategory(int userId, String category) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ? AND category = ? AND date(created_at) = date('now')";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, category);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public int getTodayXp(int userId) {
        String sql = "SELECT COALESCE(SUM(xp_earned), 0) FROM tasks WHERE user_id = ? AND date(completed_at) = date('now')";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private Achievement mapToAchievement(ResultSet rs) throws SQLException {
        Achievement achievement = new Achievement();
        achievement.setAchievementId(rs.getInt("achievement_id"));
        achievement.setName(rs.getString("name"));
        achievement.setDescription(rs.getString("description"));
        achievement.setCriteriaType(rs.getString("criteria_type"));
        achievement.setCriteriaValue(rs.getInt("criteria_value"));
        achievement.setDifficultyLevel(rs.getString("difficulty_level"));
        achievement.setXpReward(rs.getInt("xp_reward"));
        achievement.setIconPath(rs.getString("icon_path"));
        return achievement;
    }
}
