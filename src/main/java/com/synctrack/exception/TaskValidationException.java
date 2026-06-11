// TaskValidationException.java
package com.synctrack.exception;

import com.synctrack.model.Task;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TaskValidationException - Custom exception for task validation errors in SyncTrack
 * 
 * This exception is thrown when task operations fail validation, including:
 * - Invalid task data (empty title, invalid dates)
 * - Constraint violations (duplicate tasks, circular dependencies)
 * - Business rule violations (deadline in past, invalid priority)
 * - State transition errors (completing already completed task)
 * - Permission errors (accessing another user's task)
 * 
 * @author SyncTrack Team
 * @version 1.0
 */
public class TaskValidationException extends Exception {
    
    // ==================== CONSTANTS ====================
    
    /**
     * Error codes for different validation failure types
     */
    public static final int ERROR_CODE_GENERAL = 2000;
    public static final int ERROR_CODE_EMPTY_TITLE = 2001;
    public static final int ERROR_CODE_TITLE_TOO_LONG = 2002;
    public static final int ERROR_CODE_DESCRIPTION_TOO_LONG = 2003;
    public static final int ERROR_CODE_INVALID_CATEGORY = 2004;
    public static final int ERROR_CODE_INVALID_PRIORITY = 2005;
    public static final int ERROR_CODE_INVALID_DIFFICULTY = 2006;
    public static final int ERROR_CODE_DEADLINE_IN_PAST = 2007;
    public static final int ERROR_CODE_DEADLINE_TOO_FAR = 2008;
    public static final int ERROR_CODE_INVALID_ESTIMATED_TIME = 2009;
    public static final int ERROR_CODE_ESTIMATED_TIME_TOO_HIGH = 2010;
    public static final int ERROR_CODE_INVALID_STATUS = 2011;
    public static final int ERROR_CODE_INVALID_STATUS_TRANSITION = 2012;
    public static final int ERROR_CODE_TASK_NOT_FOUND = 2013;
    public static final int ERROR_CODE_TASK_ALREADY_COMPLETED = 2014;
    public static final int ERROR_CODE_TASK_ALREADY_ARCHIVED = 2015;
    public static final int ERROR_CODE_CANNOT_DELETE_COMPLETED = 2016;
    public static final int ERROR_CODE_DUPLICATE_TASK = 2017;
    public static final int ERROR_CODE_CIRCULAR_DEPENDENCY = 2018;
    public static final int ERROR_CODE_MISSING_DEPENDENCY = 2019;
    public static final int ERROR_CODE_PERMISSION_DENIED = 2020;
    public static final int ERROR_CODE_MAX_TASKS_REACHED = 2021;
    public static final int ERROR_CODE_INVALID_DURATION = 2022;
    public static final int ERROR_CODE_NEGATIVE_TIME_LOGGED = 2023;
    public static final int ERROR_CODE_MISSING_REQUIRED_FIELD = 2024;
    public static final int ERROR_CODE_INVALID_CHARACTERS = 2025;
    public static final int ERROR_CODE_RESERVED_KEYWORDS = 2026;
    public static final int ERROR_CODE_DUPLICATE_DEPENDENCY = 2027;
    public static final int ERROR_CODE_SELF_DEPENDENCY = 2028;
    public static final int ERROR_CODE_DEPENDENCY_CHAIN_TOO_LONG = 2029;
    public static final int ERROR_CODE_TASK_OVERDUE = 2030;
    public static final int ERROR_CODE_CANNOT_UNCOMPLETE = 2031;
    
    // ==================== FIELDS ====================
    
    private final int errorCode;
    private final String errorType;
    private final String fieldName;
    private final Object invalidValue;
    private final Task task;
    private final List<ValidationError> validationErrors;
    private final boolean isRecoverable;
    private final String suggestedFix;
    private final Throwable cause;
    private final long timestamp;
    
    // ==================== NESTED CLASSES ====================
    
    /**
     * Represents a single validation error for detailed error reporting
     */
    public static class ValidationError {
        private final String field;
        private final String message;
        private final int errorCode;
        private final Object invalidValue;
        private final String suggestedFix;
        
        public ValidationError(String field, String message, int errorCode, Object invalidValue, String suggestedFix) {
            this.field = field;
            this.message = message;
            this.errorCode = errorCode;
            this.invalidValue = invalidValue;
            this.suggestedFix = suggestedFix;
        }
        
        public String getField() { return field; }
        public String getMessage() { return message; }
        public int getErrorCode() { return errorCode; }
        public Object getInvalidValue() { return invalidValue; }
        public String getSuggestedFix() { return suggestedFix; }
        
        @Override
        public String toString() {
            return String.format("ValidationError{field='%s', message='%s', errorCode=%d}", 
                field, message, errorCode);
        }
    }
    
    /**
     * Builder class for constructing detailed validation exceptions
     */
    public static class Builder {
        private int errorCode = ERROR_CODE_GENERAL;
        private String message;
        private String fieldName;
        private Object invalidValue;
        private Task task;
        private List<ValidationError> validationErrors = new ArrayList<>();
        private boolean isRecoverable = true;
        private String suggestedFix;
        private Throwable cause;
        
        public Builder errorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder field(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }
        
        public Builder invalidValue(Object invalidValue) {
            this.invalidValue = invalidValue;
            return this;
        }
        
        public Builder task(Task task) {
            this.task = task;
            return this;
        }
        
        public Builder addValidationError(ValidationError error) {
            this.validationErrors.add(error);
            return this;
        }
        
        public Builder addValidationError(String field, String message, int errorCode, Object invalidValue, String suggestedFix) {
            this.validationErrors.add(new ValidationError(field, message, errorCode, invalidValue, suggestedFix));
            return this;
        }
        
        public Builder recoverable(boolean recoverable) {
            this.isRecoverable = recoverable;
            return this;
        }
        
        public Builder suggestedFix(String suggestedFix) {
            this.suggestedFix = suggestedFix;
            return this;
        }
        
        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }
        
        public TaskValidationException build() {
            if (message == null && !validationErrors.isEmpty()) {
                message = validationErrors.stream()
                    .map(ValidationError::getMessage)
                    .collect(Collectors.joining("; "));
            }
            return new TaskValidationException(this);
        }
    }
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Constructs a new TaskValidationException with the specified detail message
     * 
     * @param message the detail message
     */
    public TaskValidationException(String message) {
        super(message);
        this.errorCode = ERROR_CODE_GENERAL;
        this.errorType = "GENERAL_VALIDATION_ERROR";
        this.fieldName = null;
        this.invalidValue = null;
        this.task = null;
        this.validationErrors = new ArrayList<>();
        this.isRecoverable = true;
        this.suggestedFix = "Please correct the invalid data and try again.";
        this.cause = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new TaskValidationException with error code and message
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public TaskValidationException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = getErrorTypeFromCode(errorCode);
        this.fieldName = getFieldNameFromCode(errorCode);
        this.invalidValue = null;
        this.task = null;
        this.validationErrors = new ArrayList<>();
        this.isRecoverable = isErrorRecoverable(errorCode);
        this.suggestedFix = getSuggestedFix(errorCode);
        this.cause = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new TaskValidationException with field-specific error
     * 
     * @param errorCode the error code
     * @param fieldName the field that caused the error
     * @param invalidValue the invalid value
     * @param message the detail message
     */
    public TaskValidationException(int errorCode, String fieldName, Object invalidValue, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = getErrorTypeFromCode(errorCode);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.task = null;
        this.validationErrors = new ArrayList<>();
        this.isRecoverable = isErrorRecoverable(errorCode);
        this.suggestedFix = getSuggestedFix(errorCode);
        this.cause = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new TaskValidationException with associated task
     * 
     * @param errorCode the error code
     * @param task the task that caused the error
     * @param message the detail message
     */
    public TaskValidationException(int errorCode, Task task, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = getErrorTypeFromCode(errorCode);
        this.fieldName = null;
        this.invalidValue = null;
        this.task = task;
        this.validationErrors = new ArrayList<>();
        this.isRecoverable = isErrorRecoverable(errorCode);
        this.suggestedFix = getSuggestedFix(errorCode);
        this.cause = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new TaskValidationException from builder
     * 
     * @param builder the builder instance
     */
    private TaskValidationException(Builder builder) {
        super(builder.message, builder.cause);
        this.errorCode = builder.errorCode;
        this.errorType = getErrorTypeFromCode(builder.errorCode);
        this.fieldName = builder.fieldName;
        this.invalidValue = builder.invalidValue;
        this.task = builder.task;
        this.validationErrors = builder.validationErrors;
        this.isRecoverable = builder.isRecoverable;
        this.suggestedFix = builder.suggestedFix != null ? builder.suggestedFix : getSuggestedFix(builder.errorCode);
        this.cause = builder.cause;
        this.timestamp = System.currentTimeMillis();
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Creates an exception for empty task title
     * 
     * @return TaskValidationException instance
     */
    public static TaskValidationException emptyTitle() {
        return new TaskValidationException(
            ERROR_CODE_EMPTY_TITLE,
            "title",
            null,
            "Task title cannot be empty. Please provide a meaningful title for your task."
        );
    }
    
    /**
     * Creates an exception for title that is too long
     * 
     * @param maxLength maximum allowed length
     * @param actualLength actual length
     * @return TaskValidationException instance
     */
    public static TaskValidationException titleTooLong(int maxLength, int actualLength) {
        return new TaskValidationException(
            ERROR_CODE_TITLE_TOO_LONG,
            "title",
            actualLength,
            String.format("Task title is too long (maximum %d characters). Current length: %d", maxLength, actualLength)
        );
    }
    
    /**
     * Creates an exception for deadline in the past
     * 
     * @param deadline the invalid deadline
     * @return TaskValidationException instance
     */
    public static TaskValidationException deadlineInPast(LocalDateTime deadline) {
        return new TaskValidationException(
            ERROR_CODE_DEADLINE_IN_PAST,
            "deadline",
            deadline,
            String.format("Deadline cannot be in the past. Selected deadline: %s", deadline)
        );
    }
    
    /**
     * Creates an exception for invalid priority value
     * 
     * @param priority the invalid priority value
     * @return TaskValidationException instance
     */
    public static TaskValidationException invalidPriority(int priority) {
        return new TaskValidationException(
            ERROR_CODE_INVALID_PRIORITY,
            "priority",
            priority,
            String.format("Invalid priority value: %d. Priority must be between 1 (highest) and 5 (lowest).", priority)
        );
    }
    
    /**
     * Creates an exception for invalid difficulty value
     * 
     * @param difficulty the invalid difficulty value
     * @return TaskValidationException instance
     */
    public static TaskValidationException invalidDifficulty(String difficulty) {
        return new TaskValidationException(
            ERROR_CODE_INVALID_DIFFICULTY,
            "difficulty",
            difficulty,
            String.format("Invalid difficulty value: '%s'. Allowed values: Easy, Medium, Hard, Expert", difficulty)
        );
    }
    
    /**
     * Creates an exception for invalid category
     * 
     * @param category the invalid category
     * @return TaskValidationException instance
     */
    public static TaskValidationException invalidCategory(String category) {
        return new TaskValidationException(
            ERROR_CODE_INVALID_CATEGORY,
            "category",
            category,
            String.format("Invalid category: '%s'. Please select from available categories.", category)
        );
    }
    
    /**
     * Creates an exception for invalid estimated time
     * 
     * @param estimatedTime the invalid estimated time
     * @return TaskValidationException instance
     */
    public static TaskValidationException invalidEstimatedTime(int estimatedTime) {
        return new TaskValidationException(
            ERROR_CODE_INVALID_ESTIMATED_TIME,
            "estimatedTime",
            estimatedTime,
            String.format("Invalid estimated time: %d minutes. Estimated time must be between 1 and 1440 minutes (24 hours).", estimatedTime)
        );
    }
    
    /**
     * Creates an exception for task already completed
     * 
     * @param task the already completed task
     * @return TaskValidationException instance
     */
    public static TaskValidationException taskAlreadyCompleted(Task task) {
        return new TaskValidationException(
            ERROR_CODE_TASK_ALREADY_COMPLETED,
            task,
            String.format("Task '%s' has already been completed and cannot be modified.", task.getTitle())
        );
    }
    
    /**
     * Creates an exception for task not found
     * 
     * @param taskId the task ID that wasn't found
     * @return TaskValidationException instance
     */
    public static TaskValidationException taskNotFound(int taskId) {
        return new TaskValidationException(
            ERROR_CODE_TASK_NOT_FOUND,
            String.format("Task with ID %d was not found.", taskId)
        );
    }
    
    /**
     * Creates an exception for invalid status transition
     * 
     * @param currentStatus current status
     * @param newStatus attempted new status
     * @return TaskValidationException instance
     */
    public static TaskValidationException invalidStatusTransition(String currentStatus, String newStatus) {
        return new TaskValidationException(
            ERROR_CODE_INVALID_STATUS_TRANSITION,
            "status",
            newStatus,
            String.format("Cannot transition from '%s' to '%s'. Invalid status change.", currentStatus, newStatus)
        );
    }
    
    /**
     * Creates an exception for permission denied
     * 
     * @param userId the user ID
     * @param taskId the task ID
     * @return TaskValidationException instance
     */
    public static TaskValidationException permissionDenied(int userId, int taskId) {
        return new TaskValidationException(
            ERROR_CODE_PERMISSION_DENIED,
            String.format("User %d does not have permission to access task %d", userId, taskId)
        );
    }
    
    /**
     * Creates an exception for circular dependency
     * 
     * @param taskId the task with circular dependency
     * @param dependentTaskId the dependent task
     * @return TaskValidationException instance
     */
    public static TaskValidationException circularDependency(int taskId, int dependentTaskId) {
        return new TaskValidationException(
            ERROR_CODE_CIRCULAR_DEPENDENCY,
            String.format("Circular dependency detected between task %d and task %d", taskId, dependentTaskId)
        );
    }
    
    /**
     * Creates an exception for self-dependency
     * 
     * @param taskId the task that depends on itself
     * @return TaskValidationException instance
     */
    public static TaskValidationException selfDependency(int taskId) {
        return new TaskValidationException(
            ERROR_CODE_SELF_DEPENDENCY,
            String.format("Task %d cannot depend on itself.", taskId)
        );
    }
    
    /**
     * Creates an exception for missing required field
     * 
     * @param fieldName the missing field name
     * @return TaskValidationException instance
     */
    public static TaskValidationException missingRequiredField(String fieldName) {
        return new TaskValidationException(
            ERROR_CODE_MISSING_REQUIRED_FIELD,
            fieldName,
            null,
            String.format("Required field '%s' is missing.", fieldName)
        );
    }
    
    /**
     * Creates an exception for maximum tasks reached
     * 
     * @param maxTasks maximum allowed tasks
     * @return TaskValidationException instance
     */
    public static TaskValidationException maxTasksReached(int maxTasks) {
        return new TaskValidationException(
            ERROR_CODE_MAX_TASKS_REACHED,
            String.format("Maximum number of tasks (%d) reached. Please archive or delete some tasks before creating new ones.", maxTasks)
        );
    }
    
    /**
     * Creates an exception for duplicate task
     * 
     * @param title the duplicate task title
     * @return TaskValidationException instance
     */
    public static TaskValidationException duplicateTask(String title) {
        return new TaskValidationException(
            ERROR_CODE_DUPLICATE_TASK,
            "title",
            title,
            String.format("A task with title '%s' already exists. Please use a different title.", title)
        );
    }
    
    /**
     * Creates an exception for task overdue
     * 
     * @param task the overdue task
     * @return TaskValidationException instance
     */
    public static TaskValidationException taskOverdue(Task task) {
        return new TaskValidationException(
            ERROR_CODE_TASK_OVERDUE,
            task,
            String.format("Task '%s' is overdue (deadline: %s). Please update the deadline or mark as completed.", 
                task.getTitle(), task.getDeadline())
        );
    }
    
    /**
     * Creates a builder for complex validation exceptions
     * 
     * @return Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Gets the error type string from error code
     * 
     * @param code the error code
     * @return the error type string
     */
    private static String getErrorTypeFromCode(int code) {
        switch (code) {
            case ERROR_CODE_EMPTY_TITLE: return "EMPTY_TITLE";
            case ERROR_CODE_TITLE_TOO_LONG: return "TITLE_TOO_LONG";
            case ERROR_CODE_DESCRIPTION_TOO_LONG: return "DESCRIPTION_TOO_LONG";
            case ERROR_CODE_INVALID_CATEGORY: return "INVALID_CATEGORY";
            case ERROR_CODE_INVALID_PRIORITY: return "INVALID_PRIORITY";
            case ERROR_CODE_INVALID_DIFFICULTY: return "INVALID_DIFFICULTY";
            case ERROR_CODE_DEADLINE_IN_PAST: return "DEADLINE_IN_PAST";
            case ERROR_CODE_DEADLINE_TOO_FAR: return "DEADLINE_TOO_FAR";
            case ERROR_CODE_INVALID_ESTIMATED_TIME: return "INVALID_ESTIMATED_TIME";
            case ERROR_CODE_ESTIMATED_TIME_TOO_HIGH: return "ESTIMATED_TIME_TOO_HIGH";
            case ERROR_CODE_INVALID_STATUS: return "INVALID_STATUS";
            case ERROR_CODE_INVALID_STATUS_TRANSITION: return "INVALID_STATUS_TRANSITION";
            case ERROR_CODE_TASK_NOT_FOUND: return "TASK_NOT_FOUND";
            case ERROR_CODE_TASK_ALREADY_COMPLETED: return "TASK_ALREADY_COMPLETED";
            case ERROR_CODE_TASK_ALREADY_ARCHIVED: return "TASK_ALREADY_ARCHIVED";
            case ERROR_CODE_CANNOT_DELETE_COMPLETED: return "CANNOT_DELETE_COMPLETED";
            case ERROR_CODE_DUPLICATE_TASK: return "DUPLICATE_TASK";
            case ERROR_CODE_CIRCULAR_DEPENDENCY: return "CIRCULAR_DEPENDENCY";
            case ERROR_CODE_MISSING_DEPENDENCY: return "MISSING_DEPENDENCY";
            case ERROR_CODE_PERMISSION_DENIED: return "PERMISSION_DENIED";
            case ERROR_CODE_MAX_TASKS_REACHED: return "MAX_TASKS_REACHED";
            case ERROR_CODE_INVALID_DURATION: return "INVALID_DURATION";
            case ERROR_CODE_NEGATIVE_TIME_LOGGED: return "NEGATIVE_TIME_LOGGED";
            case ERROR_CODE_MISSING_REQUIRED_FIELD: return "MISSING_REQUIRED_FIELD";
            case ERROR_CODE_INVALID_CHARACTERS: return "INVALID_CHARACTERS";
            case ERROR_CODE_RESERVED_KEYWORDS: return "RESERVED_KEYWORDS";
            case ERROR_CODE_DUPLICATE_DEPENDENCY: return "DUPLICATE_DEPENDENCY";
            case ERROR_CODE_SELF_DEPENDENCY: return "SELF_DEPENDENCY";
            case ERROR_CODE_DEPENDENCY_CHAIN_TOO_LONG: return "DEPENDENCY_CHAIN_TOO_LONG";
            case ERROR_CODE_TASK_OVERDUE: return "TASK_OVERDUE";
            case ERROR_CODE_CANNOT_UNCOMPLETE: return "CANNOT_UNCOMPLETE";
            default: return "GENERAL_VALIDATION_ERROR";
        }
    }
    
    /**
     * Gets the field name associated with the error code
     * 
     * @param code the error code
     * @return the field name
     */
    private static String getFieldNameFromCode(int code) {
        switch (code) {
            case ERROR_CODE_EMPTY_TITLE: return "title";
            case ERROR_CODE_TITLE_TOO_LONG: return "title";
            case ERROR_CODE_DESCRIPTION_TOO_LONG: return "description";
            case ERROR_CODE_INVALID_CATEGORY: return "category";
            case ERROR_CODE_INVALID_PRIORITY: return "priority";
            case ERROR_CODE_INVALID_DIFFICULTY: return "difficulty";
            case ERROR_CODE_DEADLINE_IN_PAST: return "deadline";
            case ERROR_CODE_DEADLINE_TOO_FAR: return "deadline";
            case ERROR_CODE_INVALID_ESTIMATED_TIME: return "estimatedTime";
            case ERROR_CODE_ESTIMATED_TIME_TOO_HIGH: return "estimatedTime";
            case ERROR_CODE_INVALID_STATUS: return "status";
            case ERROR_CODE_INVALID_STATUS_TRANSITION: return "status";
            case ERROR_CODE_DUPLICATE_TASK: return "title";
            case ERROR_CODE_MISSING_REQUIRED_FIELD: return "unknown";
            default: return null;
        }
    }
    
    /**
     * Determines if an error is recoverable
     * 
     * @param code the error code
     * @return true if the error is recoverable
     */
    private static boolean isErrorRecoverable(int code) {
        switch (code) {
            case ERROR_CODE_TASK_NOT_FOUND:
            case ERROR_CODE_TASK_ALREADY_COMPLETED:
            case ERROR_CODE_TASK_ALREADY_ARCHIVED:
            case ERROR_CODE_CANNOT_DELETE_COMPLETED:
            case ERROR_CODE_PERMISSION_DENIED:
            case ERROR_CODE_MAX_TASKS_REACHED:
            case ERROR_CODE_CIRCULAR_DEPENDENCY:
            case ERROR_CODE_SELF_DEPENDENCY:
                return false;
            default:
                return true;
        }
    }
    
    /**
     * Gets suggested fix for an error code
     * 
     * @param code the error code
     * @return suggested fix string
     */
    private static String getSuggestedFix(int code) {
        switch (code) {
            case ERROR_CODE_EMPTY_TITLE:
                return "Enter a descriptive title for your task (e.g., 'Complete project report').";
            case ERROR_CODE_TITLE_TOO_LONG:
                return "Use a shorter, more concise title. Consider moving details to the description field.";
            case ERROR_CODE_DESCRIPTION_TOO_LONG:
                return "Break down the description into smaller parts or use bullet points.";
            case ERROR_CODE_INVALID_CATEGORY:
                return "Select a category from the dropdown list or create a new one.";
            case ERROR_CODE_INVALID_PRIORITY:
                return "Choose a priority between 1 (Highest) and 5 (Lowest).";
            case ERROR_CODE_INVALID_DIFFICULTY:
                return "Select a difficulty level: Easy, Medium, Hard, or Expert.";
            case ERROR_CODE_DEADLINE_IN_PAST:
                return "Choose a future date for your deadline.";
            case ERROR_CODE_DEADLINE_TOO_FAR:
                return "Break down the task into smaller subtasks with closer deadlines.";
            case ERROR_CODE_INVALID_ESTIMATED_TIME:
                return "Enter an estimated time between 1 and 1440 minutes.";
            case ERROR_CODE_TASK_ALREADY_COMPLETED:
                return "Archive completed tasks instead of modifying them.";
            case ERROR_CODE_DUPLICATE_TASK:
                return "Use a more specific title or merge with existing task.";
            case ERROR_CODE_SELF_DEPENDENCY:
                return "Remove the self-dependency; tasks cannot depend on themselves.";
            case ERROR_CODE_MISSING_REQUIRED_FIELD:
                return "Fill in all required fields marked with *.";
            default:
                return "Review your input and try again.";
        }
    }
    
    // ==================== GETTERS ====================
    
    public int getErrorCode() { return errorCode; }
    public String getErrorType() { return errorType; }
    public String getFieldName() { return fieldName; }
    public Object getInvalidValue() { return invalidValue; }
    public Task getTask() { return task; }
    public List<ValidationError> getValidationErrors() { return Collections.unmodifiableList(validationErrors); }
    public boolean isRecoverable() { return isRecoverable; }
    public String getSuggestedFix() { return suggestedFix; }
    public long getTimestamp() { return timestamp; }
    public boolean hasMultipleErrors() { return !validationErrors.isEmpty(); }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Gets a user-friendly error message suitable for display in UI
     * 
     * @return user-friendly error message
     */
    public String getUserFriendlyMessage() {
        StringBuilder sb = new StringBuilder();
        
        if (hasMultipleErrors()) {
            sb.append("Please correct the following errors:\n\n");
            for (int i = 0; i < Math.min(validationErrors.size(), 5); i++) {
                ValidationError error = validationErrors.get(i);
                sb.append("• ").append(error.getMessage()).append("\n");
            }
            if (validationErrors.size() > 5) {
                sb.append("• And ").append(validationErrors.size() - 5).append(" more errors...\n");
            }
        } else {
            sb.append(getMessage());
        }
        
        sb.append("\n\n").append(suggestedFix);
        
        if (fieldName != null) {
            sb.append("\n\nField: ").append(fieldName);
        }
        
        if (invalidValue != null) {
            sb.append("\nInvalid value: ").append(invalidValue);
        }
        
        return sb.toString();
    }
    
    /**
     * Gets a formatted error message for logging
     * 
     * @return formatted log message
     */
    public String getLogMessage() {
        return String.format("[%s] Code: %d, Type: %s, Field: %s, Message: %s, Recoverable: %b, TaskId: %d, Timestamp: %d",
            getClass().getSimpleName(),
            errorCode,
            errorType,
            fieldName != null ? fieldName : "N/A",
            getMessage(),
            isRecoverable,
            task != null ? task.getTaskId() : -1,
            timestamp
        );
    }
    
    /**
     * Gets a JSON representation of the exception
     * 
     * @return JSON string
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"errorCode\":").append(errorCode).append(",");
        json.append("\"errorType\":\"").append(errorType).append("\",");
        json.append("\"message\":\"").append(escapeJson(getMessage())).append("\",");
        json.append("\"recoverable\":").append(isRecoverable).append(",");
        json.append("\"timestamp\":").append(timestamp);
        
        if (fieldName != null) {
            json.append(",\"field\":\"").append(fieldName).append("\"");
        }
        
        if (invalidValue != null) {
            json.append(",\"invalidValue\":\"").append(escapeJson(String.valueOf(invalidValue))).append("\"");
        }
        
        if (task != null) {
            json.append(",\"taskId\":").append(task.getTaskId());
        }
        
        if (!validationErrors.isEmpty()) {
            json.append(",\"validationErrors\":[");
            for (int i = 0; i < validationErrors.size(); i++) {
                ValidationError error = validationErrors.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"field\":\"").append(escapeJson(error.getField())).append("\",");
                json.append("\"message\":\"").append(escapeJson(error.getMessage())).append("\",");
                json.append("\"errorCode\":").append(error.getErrorCode());
                json.append("}");
            }
            json.append("]");
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Escapes special characters for JSON
     * 
     * @param str the string to escape
     * @return escaped string
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Checks if this exception represents a specific error code
     * 
     * @param code the error code to check
     * @return true if the error code matches
     */
    public boolean isErrorCode(int code) {
        return this.errorCode == code;
    }
    
    /**
     * Checks if this is a title-related error
     * 
     * @return true if title error
     */
    public boolean isTitleError() {
        return errorCode == ERROR_CODE_EMPTY_TITLE || 
               errorCode == ERROR_CODE_TITLE_TOO_LONG ||
               errorCode == ERROR_CODE_DUPLICATE_TASK;
    }
    
    /**
     * Checks if this is a date-related error
     * 
     * @return true if date error
     */
    public boolean isDateError() {
        return errorCode == ERROR_CODE_DEADLINE_IN_PAST ||
               errorCode == ERROR_CODE_DEADLINE_TOO_FAR;
    }
    
    /**
     * Checks if this is a permission-related error
     * 
     * @return true if permission error
     */
    public boolean isPermissionError() {
        return errorCode == ERROR_CODE_PERMISSION_DENIED;
    }
    
    /**
     * Checks if this is a state-related error
     * 
     * @return true if state error
     */
    public boolean isStateError() {
        return errorCode == ERROR_CODE_TASK_ALREADY_COMPLETED ||
               errorCode == ERROR_CODE_TASK_ALREADY_ARCHIVED ||
               errorCode == ERROR_CODE_INVALID_STATUS_TRANSITION ||
               errorCode == ERROR_CODE_TASK_OVERDUE;
    }
    
    /**
     * Checks if this is a dependency-related error
     * 
     * @return true if dependency error
     */
    public boolean isDependencyError() {
        return errorCode == ERROR_CODE_CIRCULAR_DEPENDENCY ||
               errorCode == ERROR_CODE_MISSING_DEPENDENCY ||
               errorCode == ERROR_CODE_DUPLICATE_DEPENDENCY ||
               errorCode == ERROR_CODE_SELF_DEPENDENCY ||
               errorCode == ERROR_CODE_DEPENDENCY_CHAIN_TOO_LONG;
    }
    
    // ==================== OVERRIDE METHODS ====================
    
    @Override
    public String toString() {
        return String.format("TaskValidationException{errorCode=%d, errorType='%s', field='%s', message='%s', recoverable=%b}",
            errorCode, errorType, fieldName != null ? fieldName : "N/A", getMessage(), isRecoverable);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TaskValidationException that = (TaskValidationException) obj;
        return errorCode == that.errorCode && 
               timestamp == that.timestamp &&
               Objects.equals(getMessage(), that.getMessage());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(errorCode, getMessage(), timestamp);
    }
}

// ==================== ADDITIONAL EXCEPTION TYPES ====================

/**
 * TaskNotFoundException - Specific exception for when a task is not found
 */
class TaskNotFoundException extends TaskValidationException {
    private final int taskId;
    
    public TaskNotFoundException(int taskId) {
        super(ERROR_CODE_TASK_NOT_FOUND, String.format("Task with ID %d was not found.", taskId));
        this.taskId = taskId;
    }
    
    public int getTaskId() { return taskId; }
}

/**
 * TaskAlreadyCompletedException - Specific exception for already completed tasks
 */
class TaskAlreadyCompletedException extends TaskValidationException {
    private final Task task;
    
    public TaskAlreadyCompletedException(Task task) {
        super(ERROR_CODE_TASK_ALREADY_COMPLETED, task, 
              String.format("Task '%s' has already been completed.", task.getTitle()));
        this.task = task;
    }
    
    public Task getTask() { return task; }
}

/**
 * InvalidStatusTransitionException - Specific exception for invalid status changes
 */
class InvalidStatusTransitionException extends TaskValidationException {
    private final String currentStatus;
    private final String attemptedStatus;
    
    public InvalidStatusTransitionException(String currentStatus, String attemptedStatus) {
        super(ERROR_CODE_INVALID_STATUS_TRANSITION, 
              String.format("Cannot change status from '%s' to '%s'", currentStatus, attemptedStatus));
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }
    
    public String getCurrentStatus() { return currentStatus; }
    public String getAttemptedStatus() { return attemptedStatus; }
}

/**
 * CircularDependencyException - Specific exception for circular task dependencies
 */
class CircularDependencyException extends TaskValidationException {
    private final List<Integer> dependencyChain;
    
    public CircularDependencyException(List<Integer> dependencyChain) {
        super(ERROR_CODE_CIRCULAR_DEPENDENCY, 
              String.format("Circular dependency detected: %s", dependencyChain));
        this.dependencyChain = new ArrayList<>(dependencyChain);
    }
    
    public List<Integer> getDependencyChain() { return Collections.unmodifiableList(dependencyChain); }
}

/**
 * TaskOverdueException - Specific exception for overdue tasks
 */
class TaskOverdueException extends TaskValidationException {
    private final Task task;
    private final long daysOverdue;
    
    public TaskOverdueException(Task task) {
        super(ERROR_CODE_TASK_OVERDUE, task, 
              String.format("Task '%s' is overdue by %d days", 
                  task.getTitle(), 
                  java.time.Duration.between(task.getDeadline(), java.time.LocalDateTime.now()).toDays()));
        this.task = task;
        this.daysOverdue = java.time.Duration.between(task.getDeadline(),
