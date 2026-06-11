// DatabaseConnection.java
package com.synctrack.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static DatabaseConnection instance;
    private BlockingQueue<Connection> connectionPool;
    private static final int POOL_SIZE = 5;
    private String databasePath = "synctrack.db";
    
    private DatabaseConnection() {
        initializeDatabase();
        initializeConnectionPool();
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        String url = "jdbc:sqlite:" + databasePath;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            
            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    salt TEXT NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    last_login DATETIME,
                    xp_total INTEGER DEFAULT 0,
                    level INTEGER DEFAULT 1,
                    current_streak INTEGER DEFAULT 0,
                    longest_streak INTEGER DEFAULT 0,
                    total_tasks_completed INTEGER DEFAULT 0,
                    total_time_seconds INTEGER DEFAULT 0,
                    theme_preference TEXT DEFAULT 'light'
                )
            """);
            
            // Tasks table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    task_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    category TEXT,
                    priority INTEGER DEFAULT 3,
                    difficulty TEXT DEFAULT 'Medium',
                    deadline DATETIME,
                    estimated_time INTEGER,
                    status TEXT DEFAULT 'pending',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    completed_at DATETIME,
                    actual_time_seconds INTEGER DEFAULT 0,
                    xp_earned INTEGER DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
            """);
            
            // Time logs table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS time_logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    task_id INTEGER NOT NULL,
                    start_time DATETIME NOT NULL,
                    end_time DATETIME,
                    duration_seconds INTEGER DEFAULT 0,
                    is_active BOOLEAN DEFAULT 1,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE
                )
            """);
            
            // Achievements table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS achievements (
                    achievement_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL,
                    description TEXT,
                    criteria_type TEXT,
                    criteria_value INTEGER NOT NULL,
                    difficulty_level TEXT,
                    xp_reward INTEGER NOT NULL,
                    icon_path TEXT
                )
            """);
            
            // User achievements table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_achievements (
                    user_achievement_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    achievement_id INTEGER NOT NULL,
                    unlocked_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    progress_current INTEGER DEFAULT 0,
                    is_completed BOOLEAN DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (achievement_id) REFERENCES achievements(achievement_id),
                    UNIQUE(user_id, achievement_id)
                )
            """);
            
            // Daily stats table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS daily_stats (
                    stat_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    date DATE NOT NULL,
                    tasks_completed INTEGER DEFAULT 0,
                    total_time_seconds INTEGER DEFAULT 0,
                    xp_earned INTEGER DEFAULT 0,
                    productivity_score REAL DEFAULT 0,
                    UNIQUE(user_id, date),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
            """);
            
            // Insert default achievements
            insertDefaultAchievements(stmt);
            
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private void insertDefaultAchievements(Statement stmt) throws SQLException {
        // Check if achievements exist
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM achievements");
        if (rs.getInt(1) == 0) {
            String[] achievements = {
                "INSERT INTO achievements (name, description, criteria_type, criteria_value, difficulty_level, xp_reward) VALUES ('First Task', 'Complete your first task', 'tasks_completed', 1, 'Bronze', 50)",
                "INSERT INTO achievements (name, description, criteria_type, criteria_value, difficulty_level, xp_reward) VALUES ('Getting Started', 'Complete 10 tasks', 'tasks_completed', 10, 'Bronze', 100)",
                "INSERT INTO achievements (name, description, criteria_type, criteria_value, difficulty_level, xp_reward) VALUES ('Productivity Master', 'Complete 100 tasks', 'tasks_completed', 100, 'Gold', 500)",
                "INSERT INTO achievements (name, description, criteria_type, criteria_value, difficulty_level, xp_reward) VALUES ('Weekly Warrior', 'Maintain a 7-day streak', 'streak_days', 7, 'Silver', 200)",
                "INSERT INTO achievements (name, description, criteria_type, criteria_value, difficulty_level, xp_reward) VALUES ('Monthly Champion', 'Maintain a 30-day streak', 'streak_days', 30, 'Gold', 1000)",
                "INSERT INTO achievements (name, description, criteria_type, criteria_value, difficulty_level, xp_reward) VALUES ('XP Collector', 'Earn 1000 XP', 'total_xp', 1000, 'Silver', 200)",
                "INSERT INTO achievements (name, description, criteria_type, criteria_value, difficulty_level, xp_reward) VALUES ('XP Master', 'Earn 5000 XP', 'total_xp', 5000, 'Gold', 500)",
                "INSERT INTO achievements (name, description, criteria_type, criteria_value, difficulty_level, xp_reward) VALUES ('Time Lord', 'Track 100 hours of work', 'time_spent', 100, 'Platinum', 1000)"
            };
            for (String ach : achievements) {
                stmt.execute(ach);
            }
        }
    }
    
    private void initializeConnectionPool() {
        connectionPool = new ArrayBlockingQueue<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
                connectionPool.offer(conn);
            } catch (SQLException e) {
                logger.error("Failed to create connection", e);
            }
        }
    }
    
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionPool.poll(5, TimeUnit.SECONDS);
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }
    
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    connectionPool.offer(conn);
                }
            } catch (SQLException e) {
                logger.error("Error releasing connection", e);
            }
        }
    }
}
