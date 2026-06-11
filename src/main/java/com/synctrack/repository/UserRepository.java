// UserRepository.java
package com.synctrack.repository;

import com.synctrack.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private DatabaseConnection dbConnection;
    
    public UserRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public User create(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, salt, created_at, theme_preference) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getSalt());
            pstmt.setString(5, user.getCreatedAt().toString());
            pstmt.setString(6, user.getThemePreference());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
            }
            
            logger.info("Created user: {}", user.getUsername());
            return user;
        } catch (SQLException e) {
            logger.error("Failed to create user", e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            logger.error("Failed to find user by username", e);
            return null;
        }
    }
    
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            logger.error("Failed to find user by email", e);
            return null;
        }
    }
    
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            logger.error("Failed to find user by id", e);
            return null;
        }
    }
    
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, xp_total = ?, level = ?, " +
                    "current_streak = ?, longest_streak = ?, total_tasks_completed = ?, " +
                    "total_time_seconds = ?, theme_preference = ?, last_login = ? WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setInt(3, user.getXpTotal());
            pstmt.setInt(4, user.getLevel());
            pstmt.setInt(5, user.getCurrentStreak());
            pstmt.setInt(6, user.getLongestStreak());
            pstmt.setInt(7, user.getTotalTasksCompleted());
            pstmt.setLong(8, user.getTotalTimeSeconds());
            pstmt.setString(9, user.getThemePreference());
            pstmt.setString(10, user.getLastLogin() != null ? user.getLastLogin().toString() : null);
            pstmt.setInt(11, user.getUserId());
            
            pstmt.executeUpdate();
            logger.debug("Updated user: {}", user.getUsername());
        } catch (SQLException e) {
            logger.error("Failed to update user", e);
            throw new RuntimeException("Failed to update user", e);
        }
    }
    
    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update last login", e);
        }
    }
    
    public void updateStreak(int userId, int newStreak) {
        String sql = "UPDATE users SET current_streak = ?, longest_streak = MAX(longest_streak, ?) WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newStreak);
            pstmt.setInt(2, newStreak);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update streak", e);
        }
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) user.setCreatedAt(LocalDateTime.parse(createdAt));
        
        String lastLogin = rs.getString("last_login");
        if (lastLogin != null) user.setLastLogin(LocalDateTime.parse(lastLogin));
        
        user.setXpTotal(rs.getInt("xp_total"));
        user.setLevel(rs.getInt("level"));
        user.setCurrentStreak(rs.getInt("current_streak"));
        user.setLongestStreak(rs.getInt("longest_streak"));
        user.setTotalTasksCompleted(rs.getInt("total_tasks_completed"));
        user.setTotalTimeSeconds(rs.getLong("total_time_seconds"));
        user.setThemePreference(rs.getString("theme_preference"));
        
        return user;
    }
}
