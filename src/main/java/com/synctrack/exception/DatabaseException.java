// DatabaseException.java
package com.synctrack.exception;

import java.sql.SQLException;

public class DatabaseException extends Exception {
    
    // Error codes
    public static final int ERROR_CODE_GENERAL = 3000;
    public static final int ERROR_CODE_CONNECTION_FAILED = 3001;
    public static final int ERROR_CODE_QUERY_EXECUTION_FAILED = 3003;
    public static final int ERROR_CODE_CONSTRAINT_VIOLATION = 3005;
    public static final int ERROR_CODE_UNIQUE_CONSTRAINT = 3006;
    public static final int ERROR_CODE_FOREIGN_KEY_VIOLATION = 3007;
    public static final int ERROR_CODE_TABLE_NOT_FOUND = 3016;
    public static final int ERROR_CODE_PERMISSION_DENIED = 3019;
    public static final int ERROR_CODE_DISK_FULL = 3020;
    
    private final int errorCode;
    private final String sqlState;
    private final int vendorCode;
    private final String tableName;
    private final boolean isRetryable;
    private final String suggestedAction;
    private final long timestamp;
    
    public DatabaseException(String message) {
        super(message);
        this.errorCode = ERROR_CODE_GENERAL;
        this.sqlState = null;
        this.vendorCode = 0;
        this.tableName = null;
        this.isRetryable = true;
        this.suggestedAction = "Check database logs and retry.";
        this.timestamp = System.currentTimeMillis();
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ERROR_CODE_GENERAL;
        this.sqlState = cause instanceof SQLException ? ((SQLException) cause).getSQLState() : null;
        this.vendorCode = cause instanceof SQLException ? ((SQLException) cause).getErrorCode() : 0;
        this.tableName = null;
        this.isRetryable = true;
        this.suggestedAction = "Check database logs and retry.";
        this.timestamp = System.currentTimeMillis();
    }
    
    public DatabaseException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.sqlState = null;
        this.vendorCode = 0;
        this.tableName = null;
        this.isRetryable = isErrorRetryable(errorCode);
        this.suggestedAction = getSuggestedAction(errorCode);
        this.timestamp = System.currentTimeMillis();
    }
    
    public DatabaseException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.sqlState = cause instanceof SQLException ? ((SQLException) cause).getSQLState() : null;
        this.vendorCode = cause instanceof SQLException ? ((SQLException) cause).getErrorCode() : 0;
        this.tableName = null;
        this.isRetryable = isErrorRetryable(errorCode);
        this.suggestedAction = getSuggestedAction(errorCode);
        this.timestamp = System.currentTimeMillis();
    }
    
    // Factory methods
    public static DatabaseException connectionFailed(Throwable cause) {
        return new DatabaseException(ERROR_CODE_CONNECTION_FAILED, 
            "Failed to connect to database", cause);
    }
    
    public static DatabaseException uniqueConstraintViolation(String tableName, String constraintName, Object duplicateValue) {
        DatabaseException ex = new DatabaseException(ERROR_CODE_UNIQUE_CONSTRAINT,
            String.format("Duplicate value '%s' violates unique constraint", duplicateValue));
        ex.tableName = tableName;
        return ex;
    }
    
    public static DatabaseException foreignKeyViolation(String tableName, String constraintName) {
        return new DatabaseException(ERROR_CODE_FOREIGN_KEY_VIOLATION,
            String.format("Foreign key violation on table %s", tableName));
    }
    
    public static DatabaseException tableNotFound(String tableName) {
        return new DatabaseException(ERROR_CODE_TABLE_NOT_FOUND,
            String.format("Table '%s' not found", tableName));
    }
    
    public static DatabaseException fromSQLException(SQLException sqlEx, String operation) {
        int errorCode = mapSQLExceptionToErrorCode(sqlEx);
        return new DatabaseException(errorCode, 
            String.format("Database %s failed: %s", operation, sqlEx.getMessage()), sqlEx);
    }
    
    private static int mapSQLExceptionToErrorCode(SQLException sqlEx) {
        String sqlState = sqlEx.getSQLState();
        int vendorCode = sqlEx.getErrorCode();
        
        if (vendorCode == 19) return ERROR_CODE_CONSTRAINT_VIOLATION;
        if (sqlState != null && sqlState.startsWith("08")) return ERROR_CODE_CONNECTION_FAILED;
        if (sqlState != null && sqlState.startsWith("23")) return ERROR_CODE_CONSTRAINT_VIOLATION;
        if (sqlState != null && sqlState.startsWith("42")) return ERROR_CODE_TABLE_NOT_FOUND;
        
        return ERROR_CODE_GENERAL;
    }
    
    private static boolean isErrorRetryable(int code) {
        return code == ERROR_CODE_CONNECTION_FAILED;
    }
    
    private static String getSuggestedAction(int code) {
        switch (code) {
            case ERROR_CODE_CONNECTION_FAILED:
                return "Check database connection settings.";
            case ERROR_CODE_UNIQUE_CONSTRAINT:
                return "Ensure all values are unique.";
            case ERROR_CODE_FOREIGN_KEY_VIOLATION:
                return "Ensure referenced record exists.";
            case ERROR_CODE_TABLE_NOT_FOUND:
                return "Run database migrations.";
            case ERROR_CODE_DISK_FULL:
                return "Free up disk space.";
            default:
                return "Check database logs for details.";
        }
    }
    
    // Getters
    public int getErrorCode() { return errorCode; }
    public String getSqlState() { return sqlState; }
    public int getVendorCode() { return vendorCode; }
    public String getTableName() { return tableName; }
    public boolean isRetryable() { return isRetryable; }
    public String getSuggestedAction() { return suggestedAction; }
    public long getTimestamp() { return timestamp; }
    
    public String getUserFriendlyMessage() {
        return String.format("Database Error: %s\n\nSuggested Action: %s", 
            getMessage(), suggestedAction);
    }
    
    public String getLogMessage() {
        return String.format("[DatabaseException] Code: %d, Message: %s, SQLState: %s, VendorCode: %d",
            errorCode, getMessage(), sqlState != null ? sqlState : "N/A", vendorCode);
    }
    
    @Override
    public String toString() {
        return String.format("DatabaseException{code=%d, message='%s'}", errorCode, getMessage());
    }
}
