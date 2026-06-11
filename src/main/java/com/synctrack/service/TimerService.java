// TimerService.java
package com.synctrack.service;

import com.synctrack.model.TimeLog;
import com.synctrack.repository.TimeLogRepository;
import com.synctrack.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class TimerService {
    private static TimerService instance;
    private final TimeLogRepository timeLogRepository;
    private final TaskRepository taskRepository;
    private TimeLog activeSession;
    private Timer updateTimer;
    private AtomicLong currentDuration = new AtomicLong(0);
    private boolean isRunning = false;
    private int currentTaskId;
    
    private TimerService() {
        this.timeLogRepository = new TimeLogRepository();
        this.taskRepository = new TaskRepository();
    }
    
    public static synchronized TimerService getInstance() {
        if (instance == null) {
            instance = new TimerService();
        }
        return instance;
    }
    
    public void startTimer(int taskId) {
        if (activeSession != null) {
            throw new IllegalStateException("Timer already running");
        }
        
        this.currentTaskId = taskId;
        activeSession = new TimeLog(taskId, LocalDateTime.now());
        timeLogRepository.save(activeSession);
        
        // Update task status
        taskRepository.updateStatus(taskId, "in_progress");
        
        startTimerUpdates();
        isRunning = true;
        
        System.out.println("Timer started for task: " + taskId);
    }
    
    public void pauseTimer() {
        if (!isRunning || activeSession == null) {
            return;
        }
        
        stopTimerUpdates();
        activeSession.setEndTime(LocalDateTime.now());
        activeSession.setDurationSeconds(currentDuration.get());
        timeLogRepository.update(activeSession);
        isRunning = false;
        
        System.out.println("Timer paused. Duration: " + currentDuration.get() + " seconds");
    }
    
    public void resumeTimer() {
        if (isRunning || activeSession == null) {
            return;
        }
        
        // Create new session for resumed work
        activeSession = new TimeLog(currentTaskId, LocalDateTime.now());
        timeLogRepository.save(activeSession);
        startTimerUpdates();
        isRunning = true;
        
        System.out.println("Timer resumed");
    }
    
    public long stopTimer() {
        if (activeSession == null) {
            return 0;
        }
        
        stopTimerUpdates();
        activeSession.setEndTime(LocalDateTime.now());
        activeSession.setDurationSeconds(currentDuration.get());
        timeLogRepository.update(activeSession);
        
        // Update task with actual time
        taskRepository.addActualTime(currentTaskId, currentDuration.get());
        taskRepository.updateStatus(currentTaskId, "pending");
        
        long duration = currentDuration.get();
        
        activeSession = null;
        isRunning = false;
        currentDuration.set(0);
        
        System.out.println("Timer stopped. Final duration: " + duration + " seconds");
        return duration;
    }
    
    public long getCurrentSessionDuration() {
        if (isRunning && activeSession != null) {
            return currentDuration.get();
        }
        return 0;
    }
    
    public boolean isTimerRunning() {
        return isRunning;
    }
    
    private void startTimerUpdates() {
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isRunning) {
                    currentDuration.incrementAndGet();
                }
            }
        }, 1000, 1000);
    }
    
    private void stopTimerUpdates() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }
}
