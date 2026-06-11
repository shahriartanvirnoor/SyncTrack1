// TaskValidationException.java
package com.synctrack.exception;

import com.synctrack.model.Task;
import java.time.LocalDateTime;
import java.util.*;

public class TaskValidationException extends Exception {
    
    // Error codes
    public static final int ERROR_CODE_GENERAL = 2000;
    public static final int ERROR_CODE_EMPTY_TITLE = 2001;
    public static final int ERROR_CODE_TITLE_TOO_LONG = 2002;
    public static final int ERROR_CODE_INVALID_PRIORITY = 2005;
    public static final int ERROR_CODE_INVALID_DIFFICULTY = 2006;
    public static final int ERROR_CODE_DEADLINE_IN_PAST = 2007;
    public static final int ERROR_CODE_INVALID_ESTIMATED_TIME = 2009;
    public static final int ERROR_CODE_TASK_ALREADY_COMPLETED = 2014;
    public static final int ERROR_CODE_TASK_NOT_FOUND = 2013;
    public static final int ERROR_CODE_INVALID_STATUS_TRANSITION = 2012;
    public static final int ERROR_CODE_PERMISSION_DENIED = 2020;
    public static final int ERROR_CODE_DUPLICATE_TASK = 2017;
    public static final int ERROR_CODE_MISSING_REQUIRED_FIELD = 2024;
    
    private final int errorCode;
    private final String fieldName;
    private final Object invalidValue;
    private final Task task;
    private final boolean isRecoverable;
    private final String suggestedFix;
    private final long timestamp;
    
    public TaskValidationException(String message) {
        super(message);
        this.errorCode = ERROR_CODE_GENERAL;
        this.fieldName = null;
        this.invalidValue = null;
        this.task = null;
        this.isRecoverable = true;
        this.suggestedFix = "Please correct the invalid data and try again.";
        this.timestamp = System.currentTimeMillis();
    }
    
    public TaskValidationException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.fieldName = null;
        this.invalidValue = null;
        this.task = null;
        this.isRecoverable = isErrorRecoverable(errorCode);
        this.suggestedFix = getSuggestedFix(errorCode);
        this.timestamp = System.currentTimeMillis();
    }
    
    public TaskValidationException(int errorCode, String fieldName, Object invalidValue, String message) {
        super(message);
        this.errorCode = errorCode;
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.task = null;
        this.isRecoverable = isErrorRecoverable(errorCode);
        this.suggestedFix = getSuggestedFix(errorCode);
        this.timestamp = System.currentTimeMillis();
    }
    
    public TaskValidationException(int errorCode, Task task, String message) {
        super(message);
        this.errorCode = errorCode;
        this.fieldName = null;
        this.invalidValue = null;
        this.task = task;
        this.isRecoverable = isErrorRecoverable(errorCode);
        this.suggestedFix = getSuggestedFix(errorCode);
        this.timestamp = System.currentTimeMillis();
    }
    
    // Factory methods
    public static TaskValidationException emptyTitle() {
        return new TaskValidationException(ERROR_CODE_EMPTY_TITLE, "title", null, "Task title cannot be empty.");
    }
    
    public static TaskValidationException titleTooLong(int maxLength, int actualLength) {
        return new TaskValidationException(ERROR_CODE_TITLE_TOO_LONG, "title", actualLength, 
            String.format("Title too long (max %d characters)", maxLength));
    }
    
    public static TaskValidationException invalidPriority(int priority) {
        return new TaskValidationException(ERROR_CODE_INVALID_PRIORITY, "priority", priority, 
            "Priority must be between 1 and 5");
    }
    
    public static TaskValidationException invalidDifficulty(String difficulty) {
        return new TaskValidationException(ERROR_CODE_INVALID_DIFFICULTY, "difficulty", difficulty, 
            "Difficulty must be Easy, Medium, Hard, or Expert");
    }
    
    public static TaskValidationException deadlineInPast(LocalDateTime deadline) {
        return new TaskValidationException(ERROR_CODE_DEADLINE_IN_PAST, "deadline", deadline, 
            "Deadline cannot be in the past");
    }
    
    public static TaskValidationException invalidEstimatedTime(int estimatedTime) {
        return new TaskValidationException(ERROR_CODE_INVALID_ESTIMATED_TIME, "estimatedTime", estimatedTime, 
            "Estimated time must be between 1 and 1440 minutes");
    }
    
    public static TaskValidationException taskAlreadyCompleted(Task task) {
        return new TaskValidationException(ERROR_CODE_TASK_ALREADY_COMPLETED, task, 
            "Task already completed");
    }
    
    public static TaskValidationException taskNotFound(int taskId) {
        return new TaskValidationException(ERROR_CODE_TASK_NOT_FOUND, 
            "Task not found: " + taskId);
    }
    
    public static TaskValidationException invalidStatusTransition(String currentStatus, String newStatus) {
        return new TaskValidationException(ERROR_CODE_INVALID_STATUS_TRANSITION, "status", newStatus,
            String.format("Cannot change from '%s' to '%s'", currentStatus, newStatus));
    }
    
    public static TaskValidationException permissionDenied(int userId, int taskId) {
        return new TaskValidationException(ERROR_CODE_PERMISSION_DENIED, 
            String.format("User %d cannot access task %d", userId, taskId));
    }
    
    public static TaskValidationException duplicateTask(String title) {
        return new TaskValidationException(ERROR_CODE_DUPLICATE_TASK, "title", title, 
            "A task with this title already exists");
    }
    
    public static TaskValidationException missingRequiredField(String fieldName) {
        return new TaskValidationException(ERROR_CODE_MISSING_REQUIRED_FIELD, fieldName, null, 
            "Required field missing: " + fieldName);
    }
    
    private static boolean isErrorRecoverable(int code) {
        switch (code) {
            case ERROR_CODE_TASK_NOT_FOUND:
            case ERROR_CODE_TASK_ALREADY_COMPLETED:
            case ERROR_CODE_PERMISSION_DENIED:
                return false;
            default:
                return true;
        }
    }
    
    private static String getSuggestedFix(int code) {
        switch (code) {
            case ERROR_CODE_EMPTY_TITLE:
                return "Enter a descriptive title for your task.";
            case ERROR_CODE_TITLE_TOO_LONG:
                return "Use a shorter, more concise title.";
            case ERROR_CODE_INVALID_PRIORITY:
                return "Choose a priority between 1 (Highest) and 5 (Lowest).";
            case ERROR_CODE_INVALID_DIFFICULTY:
                return "Select Easy, Medium, Hard, or Expert.";
            case ERROR_CODE_DEADLINE_IN_PAST:
                return "Choose a future date for your deadline.";
            case ERROR_CODE_INVALID_ESTIMATED_TIME:
                return "Enter a time between 1 and 1440 minutes.";
            case ERROR_CODE_TASK_ALREADY_COMPLETED:
                return "Completed tasks cannot be modified.";
            case ERROR_CODE_DUPLICATE_TASK:
                return "Use a different title or delete the existing task.";
            case ERROR_CODE_MISSING_REQUIRED_FIELD:
                return "Fill in all required fields marked with *.";
            default:
                return "Review your input and try again.";
        }
    }
    
    // Getters
    public int getErrorCode() { return errorCode; }
    public String getFieldName() { return fieldName; }
    public Object getInvalidValue() { return invalidValue; }
    public Task getTask() { return task; }
    public boolean isRecoverable() { return isRecoverable; }
    public String getSuggestedFix() { return suggestedFix; }
    public long getTimestamp() { return timestamp; }
    
    public String getUserFriendlyMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage());
        sb.append("\n\n").append(suggestedFix);
        if (fieldName != null) {
            sb.append("\nField: ").append(fieldName);
        }
        return sb.toString();
    }
    
    public String getLogMessage() {
        return String.format("[TaskValidationException] Code: %d, Message: %s, Field: %s, Recoverable: %b",
            errorCode, getMessage(), fieldName != null ? fieldName : "N/A", isRecoverable);
    }
    
    @Override
    public String toString() {
        return String.format("TaskValidationException{code=%d, message='%s', recoverable=%b}",
            errorCode, getMessage(), isRecoverable);
    }
}
