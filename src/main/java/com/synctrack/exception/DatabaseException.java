// DatabaseException.java
package com.synctrack.exception;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DatabaseException - Custom exception for database-related errors in SyncTrack
 * 
 * This exception is thrown when database operations fail, including:
 * - Connection failures
 * - Query execution errors
 * - Constraint violations
 * - Transaction failures
 * - Data integrity issues
 * - Deadlock situations
 * - Timeout errors
 * 
 * @author SyncTrack Team
 * @version 1.0
 */
public class DatabaseException extends Exception {
    
    // ==================== CONSTANTS ====================
    
    /**
     * Error codes for different database failure types
     */
    public static final int ERROR_CODE_GENERAL = 3000;
    public static final int ERROR_CODE_CONNECTION_FAILED = 3001;
    public static final int ERROR_CODE_CONNECTION_TIMEOUT = 3002;
    public static final int ERROR_CODE_QUERY_EXECUTION_FAILED = 3003;
    public static final int ERROR_CODE_SQL_SYNTAX_ERROR = 3004;
    public static final int ERROR_CODE_CONSTRAINT_VIOLATION = 3005;
    public static final int ERROR_CODE_UNIQUE_CONSTRAINT = 3006;
    public static final int ERROR_CODE_FOREIGN_KEY_VIOLATION = 3007;
    public static final int ERROR_CODE_NOT_NULL_VIOLATION = 3008;
    public static final int ERROR_CODE_CHECK_CONSTRAINT = 3009;
    public static final int ERROR_CODE_TRANSACTION_FAILED = 3010;
    public static final int ERROR_CODE_DEADLOCK_DETECTED = 3011;
    public static final int ERROR_CODE_LOCK_TIMEOUT = 3012;
    public static final int ERROR_CODE_DATA_INTEGRITY = 3013;
    public static final int ERROR_CODE_DATA_CORRUPTION = 3014;
    public static final int ERROR_CODE_DATABASE_NOT_FOUND = 3015;
    public static final int ERROR_CODE_TABLE_NOT_FOUND = 3016;
    public static final int ERROR_CODE_COLUMN_NOT_FOUND = 3017;
    public static final int ERROR_CODE_INDEX_NOT_FOUND = 3018;
    public static final int ERROR_CODE_PERMISSION_DENIED = 3019;
    public static final int ERROR_CODE_DISK_FULL = 3020;
    public static final int ERROR_CODE_OUT_OF_MEMORY = 3021;
    public static final int ERROR_CODE_BUSY = 3022;
    public static final int ERROR_CODE_BACKUP_IN_PROGRESS = 3023;
    public static final int ERROR_CODE_RECOVERY_IN_PROGRESS = 3024;
    public static final int ERROR_CODE_VERSION_MISMATCH = 3025;
    public static final int ERROR_CODE_MIGRATION_FAILED = 3026;
    public static final int ERROR_CODE_BATCH_FAILED = 3027;
    public static final int ERROR_CODE_PREPARED_STATEMENT_FAILED = 3028;
    public static final int ERROR_CODE_RESULT_SET_FAILED = 3029;
    public static final int ERROR_CODE_CONNECTION_POOL_EXHAUSTED = 3030;
    public static final int ERROR_CODE_INVALID_CONNECTION = 3031;
    public static final int ERROR_CODE_CLOSED_CONNECTION = 3032;
    
    // SQLite specific error codes
    public static final int SQLITE_ERROR = 1;
    public static final int SQLITE_INTERNAL = 2;
    public static final int SQLITE_PERM = 3;
    public static final int SQLITE_ABORT = 4;
    public static final int SQLITE_BUSY = 5;
    public static final int SQLITE_LOCKED = 6;
    public static final int SQLITE_NOMEM = 7;
    public static final int SQLITE_READONLY = 8;
    public static final int SQLITE_INTERRUPT = 9;
    public static final int SQLITE_IOERR = 10;
    public static final int SQLITE_CORRUPT = 11;
    public static final int SQLITE_NOTFOUND = 12;
    public static final int SQLITE_FULL = 13;
    public static final int SQLITE_CANTOPEN = 14;
    public static final int SQLITE_PROTOCOL = 15;
    public static final int SQLITE_EMPTY = 16;
    public static final int SQLITE_SCHEMA = 17;
    public static final int SQLITE_TOOBIG = 18;
    public static final int SQLITE_CONSTRAINT = 19;
    public static final int SQLITE_MISMATCH = 20;
    public static final int SQLITE_MISUSE = 21;
    public static final int SQLITE_NOLFS = 22;
    public static final int SQLITE_AUTH = 23;
    public static final int SQLITE_FORMAT = 24;
    public static final int SQLITE_RANGE = 25;
    public static final int SQLITE_NOTADB = 26;
    public static final int SQLITE_NOTICE = 27;
    public static final int SQLITE_WARNING = 28;
    public static final int SQLITE_ROW = 100;
    public static final int SQLITE_DONE = 101;
    
    // ==================== FIELDS ====================
    
    private final int errorCode;
    private final String sqlState;
    private final int vendorCode;
    private final String sqlQuery;
    private final Object[] queryParameters;
    private final String tableName;
    private final String columnName;
    private final String constraintName;
    private final boolean isRetryable;
    private final boolean isTransient;
    private final int retryDelaySeconds;
    private final int maxRetries;
    private final String suggestedAction;
    private final Map<String, Object> context;
    private final List<DatabaseErrorDetail> errorDetails;
    private final Throwable cause;
    private final long timestamp;
    
    // ==================== NESTED CLASSES ====================
    
    /**
     * Represents detailed database error information
     */
    public static class DatabaseErrorDetail {
        private final String component;
        private final String message;
        private final int errorCode;
        private final String stackTrace;
        
        public DatabaseErrorDetail(String component, String message, int errorCode, String stackTrace) {
            this.component = component;
            this.message = message;
            this.errorCode = errorCode;
            this.stackTrace = stackTrace;
        }
        
        public String getComponent() { return component; }
        public String getMessage() { return message; }
        public int getErrorCode() { return errorCode; }
        public String getStackTrace() { return stackTrace; }
        
        @Override
        public String toString() {
            return String.format("[%s] Code %d: %s", component, errorCode, message);
        }
    }
    
    /**
     * Builder class for constructing detailed database exceptions
     */
    public static class Builder {
        private int errorCode = ERROR_CODE_GENERAL;
        private String message;
        private String sqlState;
        private int vendorCode;
        private String sqlQuery;
        private Object[] queryParameters;
        private String tableName;
        private String columnName;
        private String constraintName;
        private boolean isRetryable = true;
        private boolean isTransient = false;
        private int retryDelaySeconds = 5;
        private int maxRetries = 3;
        private String suggestedAction;
        private Map<String, Object> context = new HashMap<>();
        private List<DatabaseErrorDetail> errorDetails = new ArrayList<>();
        private Throwable cause;
        
        public Builder errorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder sqlState(String sqlState) {
            this.sqlState = sqlState;
            return this;
        }
        
        public Builder vendorCode(int vendorCode) {
            this.vendorCode = vendorCode;
            return this;
        }
        
        public Builder sqlQuery(String sqlQuery) {
            this.sqlQuery = sqlQuery;
            return this;
        }
        
        public Builder queryParameters(Object[] parameters) {
            this.queryParameters = parameters;
            return this;
        }
        
        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        
        public Builder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }
        
        public Builder constraintName(String constraintName) {
            this.constraintName = constraintName;
            return this;
        }
        
        public Builder retryable(boolean retryable) {
            this.isRetryable = retryable;
            return this;
        }
        
        public Builder transient_(boolean isTransient) {
            this.isTransient = isTransient;
            return this;
        }
        
        public Builder retryDelaySeconds(int seconds) {
            this.retryDelaySeconds = seconds;
            return this;
        }
        
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder suggestedAction(String suggestedAction) {
            this.suggestedAction = suggestedAction;
            return this;
        }
        
        public Builder addContext(String key, Object value) {
            this.context.put(key, value);
            return this;
        }
        
        public Builder addErrorDetail(DatabaseErrorDetail detail) {
            this.errorDetails.add(detail);
            return this;
        }
        
        public Builder cause(Throwable cause) {
            this.cause = cause;
            if (cause instanceof SQLException) {
                SQLException sqlEx = (SQLException) cause;
                if (sqlState == null) this.sqlState = sqlEx.getSQLState();
                if (vendorCode == 0) this.vendorCode = sqlEx.getErrorCode();
            }
            return this;
        }
        
        public DatabaseException build() {
            if (message == null && cause != null) {
                message = cause.getMessage();
            }
            if (suggestedAction == null) {
                suggestedAction = getDefaultSuggestedAction(errorCode);
            }
            return new DatabaseException(this);
        }
        
        private String getDefaultSuggestedAction(int code) {
            switch (code) {
                case ERROR_CODE_CONNECTION_FAILED:
                    return "Check database connection settings and ensure database server is running.";
                case ERROR_CODE_CONNECTION_POOL_EXHAUSTED:
                    return "Increase connection pool size or reduce concurrent operations.";
                case ERROR_CODE_DEADLOCK_DETECTED:
                case ERROR_CODE_LOCK_TIMEOUT:
                    return "Retry the operation after a short delay.";
                case ERROR_CODE_DISK_FULL:
                    return "Free up disk space or move database to a different location.";
                case ERROR_CODE_PERMISSION_DENIED:
                    return "Check file permissions for the database file.";
                case ERROR_CODE_CONSTRAINT_VIOLATION:
                case ERROR_CODE_UNIQUE_CONSTRAINT:
                    return "Ensure data doesn't violate unique constraints.";
                case ERROR_CODE_FOREIGN_KEY_VIOLATION:
                    return "Ensure referenced record exists before inserting.";
                case ERROR_CODE_DATABASE_NOT_FOUND:
                    return "Verify database file exists at the specified path.";
                case ERROR_CODE_VERSION_MISMATCH:
                    return "Run database migrations to update schema version.";
                default:
                    return "Check database logs for more details and contact support if issue persists.";
            }
        }
    }
    
    // ==================== CONSTRUCTORS ====================
    
    public DatabaseException(String message) {
        super(message);
        this.errorCode = ERROR_CODE_GENERAL;
        this.sqlState = null;
        this.vendorCode = 0;
        this.sqlQuery = null;
        this.queryParameters = null;
        this.tableName = null;
        this.columnName = null;
        this.constraintName = null;
        this.isRetryable = true;
        this.isTransient = false;
        this.retryDelaySeconds = 5;
        this.maxRetries = 3;
        this.suggestedAction = "Check database logs and retry the operation.";
        this.context = new HashMap<>();
        this.errorDetails = new ArrayList<>();
        this.cause = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ERROR_CODE_GENERAL;
        this.sqlState = cause instanceof SQLException ? ((SQLException) cause).getSQLState() : null;
        this.vendorCode = cause instanceof SQLException ? ((SQLException) cause).getErrorCode() : 0;
        this.sqlQuery = null;
        this.queryParameters = null;
        this.tableName = null;
        this.columnName = null;
        this.constraintName = null;
        this.isRetryable = true;
        this.isTransient = false;
        this.retryDelaySeconds = 5;
        this.maxRetries = 3;
        this.suggestedAction = "Check database logs and retry the operation.";
        this.context = new HashMap<>();
        this.errorDetails = new ArrayList<>();
        this.cause = cause;
        this.timestamp = System.currentTimeMillis();
    }
    
    public DatabaseException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.sqlState = null;
        this.vendorCode = 0;
        this.sqlQuery = null;
        this.queryParameters = null;
        this.tableName = null;
        this.columnName = null;
        this.constraintName = null;
        this.isRetryable = isErrorRetryable(errorCode);
        this.isTransient = isErrorTransient(errorCode);
        this.retryDelaySeconds = getRetryDelaySeconds(errorCode);
        this.maxRetries = getMaxRetries(errorCode);
        this.suggestedAction = getSuggestedAction(errorCode);
        this.context = new HashMap<>();
        this.errorDetails = new ArrayList<>();
        this.cause = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public DatabaseException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.sqlState = cause instanceof SQLException ? ((SQLException) cause).getSQLState() : null;
        this.vendorCode = cause instanceof SQLException ? ((SQLException) cause).getErrorCode() : 0;
        this.sqlQuery = null;
        this.queryParameters = null;
        this.tableName = null;
        this.columnName = null;
        this.constraintName = null;
        this.isRetryable = isErrorRetryable(errorCode);
        this.isTransient = isErrorTransient(errorCode);
        this.retryDelaySeconds = getRetryDelaySeconds(errorCode);
        this.maxRetries = getMaxRetries(errorCode);
        this.suggestedAction = getSuggestedAction(errorCode);
        this.context = new HashMap<>();
        this.errorDetails = new ArrayList<>();
        this.cause = cause;
        this.timestamp = System.currentTimeMillis();
    }
    
    private DatabaseException(Builder builder) {
        super(builder.message, builder.cause);
        this.errorCode = builder.errorCode;
        this.sqlState = builder.sqlState;
        this.vendorCode = builder.vendorCode;
        this.sqlQuery = builder.sqlQuery;
        this.queryParameters = builder.queryParameters;
        this.tableName = builder.tableName;
        this.columnName = builder.columnName;
        this.constraintName = builder.constraintName;
        this.isRetryable = builder.isRetryable;
        this.isTransient = builder.isTransient;
        this.retryDelaySeconds = builder.retryDelaySeconds;
        this.maxRetries = builder.maxRetries;
        this.suggestedAction = builder.suggestedAction;
        this.context = Collections.unmodifiableMap(builder.context);
        this.errorDetails = Collections.unmodifiableList(builder.errorDetails);
        this.cause = builder.cause;
        this.timestamp = System.currentTimeMillis();
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Creates exception for connection failure
     */
    public static DatabaseException connectionFailed(Throwable cause) {
        return new Builder()
            .errorCode(ERROR_CODE_CONNECTION_FAILED)
            .message("Failed to establish database connection")
            .suggestedAction("Check database server status and connection parameters")
            .cause(cause)
            .retryable(true)
            .transient_(true)
            .retryDelaySeconds(10)
            .build();
    }
    
    /**
     * Creates exception for connection timeout
     */
    public static DatabaseException connectionTimeout(int timeoutSeconds) {
        return new Builder()
            .errorCode(ERROR_CODE_CONNECTION_TIMEOUT)
            .message(String.format("Database connection timeout after %d seconds", timeoutSeconds))
            .suggestedAction("Increase connection timeout or check network connectivity")
            .retryable(true)
            .transient_(true)
            .retryDelaySeconds(5)
            .build();
    }
    
    /**
     * Creates exception for SQL syntax error
     */
    public static DatabaseException sqlSyntaxError(String sql, Throwable cause) {
        return new Builder()
            .errorCode(ERROR_CODE_SQL_SYNTAX_ERROR)
            .message("SQL syntax error in query")
            .sqlQuery(sql)
            .suggestedAction("Review SQL syntax and correct the query")
            .cause(cause)
            .retryable(false)
            .build();
    }
    
    /**
     * Creates exception for unique constraint violation
     */
    public static DatabaseException uniqueConstraintViolation(String tableName, String constraintName, Object duplicateValue) {
        return new Builder()
            .errorCode(ERROR_CODE_UNIQUE_CONSTRAINT)
            .message(String.format("Duplicate value '%s' violates unique constraint '%s' on table '%s'", 
                duplicateValue, constraintName, tableName))
            .tableName(tableName)
            .constraintName(constraintName)
            .addContext("duplicateValue", duplicateValue)
            .suggestedAction("Ensure all values are unique before inserting")
            .retryable(false)
            .build();
    }
    
    /**
     * Creates exception for foreign key violation
     */
    public static DatabaseException foreignKeyViolation(String tableName, String constraintName, Object invalidReference) {
        return new Builder()
            .errorCode(ERROR_CODE_FOREIGN_KEY_VIOLATION)
            .message(String.format("Foreign key violation: referenced value '%s' does not exist in parent table", invalidReference))
            .tableName(tableName)
            .constraintName(constraintName)
            .addContext("invalidReference", invalidReference)
            .suggestedAction("Ensure referenced record exists before inserting")
            .retryable(false)
            .build();
    }
    
    /**
     * Creates exception for not null violation
     */
    public static DatabaseException notNullViolation(String tableName, String columnName) {
        return new Builder()
            .errorCode(ERROR_CODE_NOT_NULL_VIOLATION)
            .message(String.format("NOT NULL constraint violated on column '%s' in table '%s'", columnName, tableName))
            .tableName(tableName)
            .columnName(columnName)
            .suggestedAction("Provide a value for the required field")
            .retryable(false)
            .build();
    }
    
    /**
     * Creates exception for deadlock detected
     */
    public static DatabaseException deadlockDetected() {
        return new Builder()
            .errorCode(ERROR_CODE_DEADLOCK_DETECTED)
            .message("Database deadlock detected. Transaction aborted.")
            .suggestedAction("Retry the transaction after a short delay")
            .retryable(true)
            .transient_(true)
            .retryDelaySeconds(2)
            .maxRetries(5)
            .build();
    }
    
    /**
     * Creates exception for disk full
     */
    public static DatabaseException diskFull() {
        return new Builder()
            .errorCode(ERROR_CODE_DISK_FULL)
            .message("Database operation failed due to insufficient disk space")
            .suggestedAction("Free up disk space or move database to a different location")
            .retryable(false)
            .build();
    }
    
    /**
     * Creates exception for connection pool exhausted
     */
    public static DatabaseException connectionPoolExhausted(int poolSize, int activeConnections) {
        return new Builder()
            .errorCode(ERROR_CODE_CONNECTION_POOL_EXHAUSTED)
            .message(String.format("Connection pool exhausted: %d/%d connections active", activeConnections, poolSize))
            .addContext("poolSize", poolSize)
            .addContext("activeConnections", activeConnections)
            .suggestedAction("Increase pool size or wait for connections to be released")
            .retryable(true)
            .transient_(true)
            .retryDelaySeconds(5)
            .maxRetries(10)
            .build();
    }
    
    /**
     * Creates exception for database migration failure
     */
    public static DatabaseException migrationFailed(String version, Throwable cause) {
        return new Builder()
            .errorCode(ERROR_CODE_MIGRATION_FAILED)
            .message(String.format("Database migration to version %s failed", version))
            .addContext("targetVersion", version)
            .suggestedAction("Check migration scripts and database state")
            .cause(cause)
            .retryable(false)
            .build();
    }
    
    /**
     * Creates exception from SQLException
     */
    public static DatabaseException fromSQLException(SQLException sqlEx, String operation) {
        int errorCode = mapSQLExceptionToErrorCode(sqlEx);
        return new Builder()
            .errorCode(errorCode)
            .message(String.format("Database %s failed: %s", operation, sqlEx.getMessage()))
            .sqlState(sqlEx.getSQLState())
            .vendorCode(sqlEx.getErrorCode())
            .suggestedAction(getSuggestedAction(errorCode))
            .cause(sqlEx)
            .retryable(isErrorRetryable(errorCode))
            .transient_(isErrorTransient(errorCode))
            .build();
    }
    
    /**
     * Maps SQLException to our error codes
     */
    private static int mapSQLExceptionToErrorCode(SQLException sqlEx) {
        String sqlState = sqlEx.getSQLState();
        int vendorCode = sqlEx.getErrorCode();
        
        // SQLite specific error codes
        if (vendorCode == SQLITE_CONSTRAINT) {
            return ERROR_CODE_CONSTRAINT_VIOLATION;
        }
        if (vendorCode == SQLITE_BUSY || vendorCode == SQLITE_LOCKED) {
            return ERROR_CODE_BUSY;
        }
        if (vendorCode == SQLITE_FULL) {
            return ERROR_CODE_DISK_FULL;
        }
        if (vendorCode == SQLITE_CORRUPT) {
            return ERROR_CODE_DATA_CORRUPTION;
        }
        if (vendorCode == SQLITE_CANTOPEN) {
            return ERROR_CODE_DATABASE_NOT_FOUND;
        }
        if (vendorCode == SQLITE_PERM) {
            return ERROR_CODE_PERMISSION_DENIED;
        }
        
        // General SQL states
        if (sqlState != null) {
            if (sqlState.startsWith("08")) return ERROR_CODE_CONNECTION_FAILED;
            if (sqlState.startsWith("23")) return ERROR_CODE_CONSTRAINT_VIOLATION;
            if (sqlState.startsWith("40")) return ERROR_CODE_TRANSACTION_FAILED;
            if (sqlState.startsWith("42")) return ERROR_CODE_SQL_SYNTAX_ERROR;
        }
        
        return ERROR_CODE_GENERAL;
    }
    
    // ==================== HELPER METHODS ====================
    
    private static boolean isErrorRetryable(int code) {
        switch (code) {
            case ERROR_CODE_CONNECTION_FAILED:
            case ERROR_CODE_CONNECTION_TIMEOUT:
            case ERROR_CODE_DEADLOCK_DETECTED:
            case ERROR_CODE_LOCK_TIMEOUT:
            case ERROR_CODE_BUSY:
            case ERROR_CODE_BACKUP_IN_PROGRESS:
            case ERROR_CODE_RECOVERY_IN_PROGRESS:
            case ERROR_CODE_CONNECTION_POOL_EXHAUSTED:
                return true;
            default:
                return false;
        }
    }
    
    private static boolean isErrorTransient(int code) {
        switch (code) {
            case ERROR_CODE_CONNECTION_FAILED:
            case ERROR_CODE_CONNECTION_TIMEOUT:
            case ERROR_CODE_DEADLOCK_DETECTED:
            case ERROR_CODE_LOCK_TIMEOUT:
            case ERROR_CODE_BUSY:
            case ERROR_CODE_OUT_OF_MEMORY:
                return true;
            default:
                return false;
        }
    }
    
    private static int getRetryDelaySeconds(int code) {
        switch (code) {
            case ERROR_CODE_DEADLOCK_DETECTED:
                return 2;
            case ERROR_CODE_BUSY:
            case ERROR_CODE_LOCK_TIMEOUT:
                return 3;
            case ERROR_CODE_CONNECTION_FAILED:
                return 10;
            default:
                return 5;
        }
    }
    
    private static int getMaxRetries(int code) {
        switch (code) {
            case ERROR_CODE_DEADLOCK_DETECTED:
                return 5;
            case ERROR_CODE_BUSY:
            case ERROR_CODE_LOCK_TIMEOUT:
                return 3;
            case ERROR_CODE_CONNECTION_FAILED:
                return 10;
            default:
                return 3;
        }
    }
    
    private static String getSuggestedAction(int code) {
        switch (code) {
            case ERROR_CODE_CONNECTION_FAILED:
                return "Check database connection settings and ensure database server is running.";
            case ERROR_CODE_CONNECTION_TIMEOUT:
                return "Check network connectivity and increase connection timeout if needed.";
            case ERROR_CODE_SQL_SYNTAX_ERROR:
                return "Review SQL syntax and correct the query.";
            case ERROR_CODE_CONSTRAINT_VIOLATION:
            case ERROR_CODE_UNIQUE_CONSTRAINT:
                return "Ensure data doesn't violate constraints before executing the operation.";
            case ERROR_CODE_FOREIGN_KEY_VIOLATION:
                return "Ensure referenced records exist before inserting or updating.";
            case ERROR_CODE_DEADLOCK_DETECTED:
                return "Retry the transaction after a short delay.";
            case ERROR_CODE_DISK_FULL:
                return "Free up disk space or move database to a different location.";
            case ERROR_CODE_PERMISSION_DENIED:
                return "Check file permissions for the database file.";
            case ERROR_CODE_DATA_CORRUPTION:
                return "Restore database from backup or run recovery procedures.";
            case ERROR_CODE_CONNECTION_POOL_EXHAUSTED:
                return "Increase pool size or wait for connections to be released.";
            default:
                return "Check database logs for more details.";
        }
    }
    
    // ==================== GETTERS ====================
    
    public int getErrorCode() { return errorCode; }
    public String getSqlState() { return sqlState; }
    public int getVendorCode() { return vendorCode; }
    public String getSqlQuery() { return sqlQuery; }
    public Object[] getQueryParameters() { return queryParameters != null ? queryParameters.clone() : null; }
    public String getTableName() { return tableName; }
    public String getColumnName() { return columnName; }
    public String getConstraintName() { return constraintName; }
    public boolean isRetryable() { return isRetryable; }
    public boolean isTransient() { return isTransient; }
    public int getRetryDelaySeconds() { return retryDelaySeconds; }
    public int getMaxRetries() { return maxRetries; }
    public String getSuggestedAction() { return suggestedAction; }
    public Map<String, Object> getContext() { return Collections.unmodifiableMap(context); }
    public List<DatabaseErrorDetail> getErrorDetails() { return Collections.unmodifiableList(errorDetails); }
    public long getTimestamp() { return timestamp; }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Gets a user-friendly error message suitable for display in UI
     */
    public String getUserFriendlyMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Database Error\n\n");
        sb.append(getMessage()).append("\n\n");
        
        if (tableName != null) {
            sb.append("Table: ").append(tableName).append("\n");
        }
        if (constraintName != null) {
            sb.append("Constraint: ").append(constraintName).append("\n");
        }
        
        sb.append("\nSuggested Action: ").append(suggestedAction);
        
        if (isRetryable) {
            sb.append("\n\nThis operation can be retried.");
            if (retryDelaySeconds > 0) {
                sb.append(" Please wait ").append(retryDelaySeconds).append(" seconds before retrying.");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Gets a formatted error message for logging
     */
    public String getLogMessage() {
        return String.format("[%s] Code: %d, SQLState: %s, VendorCode: %d, Message: %s, Table: %s, Constraint: %s, Retryable: %b, Timestamp: %d",
            getClass().getSimpleName(),
            errorCode,
            sqlState != null ? sqlState : "N/A",
            vendorCode,
            getMessage(),
            tableName != null ? tableName : "N/A",
            constraintName != null ? constraintName : "N/A",
            isRetryable,
            timestamp
        );
    }
    
    /**
     * Gets SQL query with parameters substituted (for debugging)
     */
    public String getFormattedSqlQuery() {
        if (sqlQuery == null) return null;
        if (queryParameters == null || queryParameters.length == 0) return sqlQuery;
        
        String formatted = sqlQuery;
        for (Object param : queryParameters) {
            String paramStr = param != null ? param.toString() : "NULL";
            paramStr = "'" + paramStr.replace("'", "''") + "'";
            formatted = formatted.replaceFirst("\\?", paramStr);
        }
        return formatted;
    }
    
    /**
     * Gets a JSON representation of the exception
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"errorCode\":").append(errorCode).append(",");
        json.append("\"message\":\"").append(escapeJson(getMessage())).append("\",");
        json.append("\"retryable\":").append(isRetryable).append(",");
        json.append("\"transient\":").append(isTransient).append(",");
        json.append("\"timestamp\":").append(timestamp);
        
        if (sqlState != null) {
            json.append(",\"sqlState\":\"").append(sqlState).append("\"");
        }
        if (vendorCode != 0) {
            json.append(",\"vendorCode\":").append(vendorCode);
        }
        if (tableName != null) {
            json.append(",\"table\":\"").append(escapeJson(tableName)).append("\"");
        }
        if (constraintName != null) {
            json.append(",\"constraint\":\"").append(escapeJson(constraintName)).append("\"");
        }
        if (suggestedAction != null) {
            json.append(",\"suggestedAction\":\"").append(escapeJson(suggestedAction)).append("\"");
        }
        
        if (!context.isEmpty()) {
            json.append(",\"context\":{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                   .append(escapeJson(String.valueOf(entry.getValue()))).append("\"");
                first = false;
            }
            json.append("}");
        }
        
        json.append("}");
        return json.toString();
    }
    
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
     */
    public boolean isErrorCode(int code) {
        return this.errorCode == code;
    }
    
    /**
     * Checks if this is a connection-related error
     */
    public boolean isConnectionError() {
        return errorCode == ERROR_CODE_CONNECTION_FAILED ||
               errorCode == ERROR_CODE_CONNECTION_TIMEOUT ||
               errorCode == ERROR_CODE_CONNECTION_POOL_EXHAUSTED ||
               errorCode == ERROR_CODE_INVALID_CONNECTION ||
               errorCode == ERROR_CODE_CLOSED_CONNECTION;
    }
    
    /**
     * Checks if this is a constraint violation error
     */
    public boolean isConstraintViolation() {
        return errorCode == ERROR_CODE_CONSTRAINT_VIOLATION ||
               errorCode == ERROR_CODE_UNIQUE_CONSTRAINT ||
               errorCode == ERROR_CODE_FOREIGN_KEY_VIOLATION ||
               errorCode == ERROR_CODE_NOT_NULL_VIOLATION ||
               errorCode == ERROR_CODE_CHECK_CONSTRAINT;
    }
    
    /**
     * Checks if this is a data integrity error
     */
    public boolean isDataIntegrityError() {
        return errorCode == ERROR_CODE_DATA_INTEGRITY ||
               errorCode == ERROR_CODE_DATA_CORRUPTION;
    }
    
    // ==================== OVERRIDE METHODS ====================
    
    @Override
    public String toString() {
        return String.format("DatabaseException{code=%d, message='%s', retryable=%b, sqlState=%s, vendorCode=%d}",
            errorCode, getMessage(), isRetryable, sqlState != null ? sqlState : "N/A", vendorCode);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DatabaseException that = (DatabaseException) obj;
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
 * ConnectionException - Specific exception for database connection issues
 */
class ConnectionException extends DatabaseException {
    private final String url;
    private final String username;
    
    public ConnectionException(String url, String username, Throwable cause) {
        super(ERROR_CODE_CONNECTION_FAILED, 
              String.format("Failed to connect to database at %s as user %s", url, username), 
              cause);
        this.url = url;
        this.username = username;
    }
    
    public String getUrl() { return url; }
    public String getUsername() { return username; }
}

/**
 * ConstraintViolationException - Specific exception for constraint violations
 */
class ConstraintViolationException extends DatabaseException {
    private final String constraintType;
    
    public ConstraintViolationException(String tableName, String constraintName, String constraintType, Object invalidValue) {
        super(ERROR_CODE_CONSTRAINT_VIOLATION,
              String.format("Constraint violation on table %s: %s (value: %s)", 
                  tableName, constraintName, invalidValue));
        this.constraintType = constraintType;
    }
    
    public String getConstraintType() { return constraintType; }
}

/**
 * DeadlockException - Specific exception for database deadlocks
 */
class DeadlockException extends DatabaseException {
    private final List<String> involvedTables;
    
    public DeadlockException(List<String> involvedTables) {
        super(ERROR_CODE_DEADLOCK_DETECTED, 
              String.format("Deadlock detected involving tables: %s", involvedTables));
        this.involvedTables = new ArrayList<>(involvedTables);
    }
    
    public List<String> getInvolvedTables() { return Collections.unmodifiableList(involvedTables); }
}

/**
 * MigrationException - Specific exception for database migration failures
 */
class MigrationException extends DatabaseException {
    private final String fromVersion;
    private final String toVersion;
    private final String failedScript;
    
    public MigrationException(String fromVersion, String toVersion, String failedScript, Throwable cause) {
        super(ERROR_CODE_MIGRATION_FAILED,
              String.format("Migration from %s to %s failed on script: %s", fromVersion, toVersion, failedScript),
              cause);
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.failedScript = failedScript;
    }
    
    public String getFromVersion() { return fromVersion; }
    public String getToVersion() { return toVersion; }
    public String getFailedScript() { return failedScript; }
}
