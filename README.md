#SyncTrack1
A reward-based task analytics and performance monitoring application
##Project Structure
```text
SyncTrack/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/synctrack/
│   │   │   ├── SyncTrackApp.java
│   │   │   ├── controller/
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── TaskController.java
│   │   │   │   ├── TimerController.java
│   │   │   │   ├── AnalyticsController.java
│   │   │   │   ├── AchievementsController.java
│   │   │   │   └── SettingsController.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Task.java
│   │   │   │   ├── TimeLog.java
│   │   │   │   ├── Achievement.java
│   │   │   │   ├── UserAchievement.java
│   │   │   │   ├── Reward.java
│   │   │   │   └── DailyStat.java
│   │   │   ├── repository/
│   │   │   │   ├── DatabaseConnection.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── TaskRepository.java
│   │   │   │   ├── TimeLogRepository.java
│   │   │   │   ├── AchievementRepository.java
│   │   │   │   └── StatsRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── TaskService.java
│   │   │   │   ├── TimerService.java
│   │   │   │   ├── AnalyticsService.java
│   │   │   │   ├── GamificationService.java
│   │   │   │   └── RewardService.java
│   │   │   ├── util/
│   │   │   │   ├── PasswordHasher.java
│   │   │   │   ├── DateUtils.java
│   │   │   │   ├── NotificationUtil.java
│   │   │   │   └── ChartUtil.java
│   │   │   └── exception/
│   │   │       ├── AuthenticationException.java
│   │   │       ├── TaskValidationException.java
│   │   │       └── DatabaseException.java
│   │   └── resources/
│   │       ├── css/
│   │       │   ├── light-theme.css
│   │       │   └── dark-theme.css
│   │       └── images/
│   │           └── app-icon.png
│   └── test/
│       └── java/com/synctrack/
│           ├── service/
│           └── repository/
