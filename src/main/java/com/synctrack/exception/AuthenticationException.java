// AuthenticationException.java
package com.synctrack.exception;

/**
 * AuthenticationException - Custom exception for authentication-related errors in SyncTrack
 * 
 * This exception is thrown when authentication operations fail, including:
 * - Login failures (invalid credentials)
 * - Registration failures (duplicate users, invalid data)
 * - Session validation failures
 * - Token validation failures
 * - Account lockout events
 * - Permission denied errors
 * 
 * @author SyncTrack Team
 * @version 1.0
 */
public class AuthenticationException extends Exception {
    
    // ==================== CONSTANTS ====================
    
    /**
     * Default error codes for different authentication failure types
     */
    public static final int ERROR_CODE_GENERAL = 1000;
    public static final int ERROR_CODE_INVALID_CREDENTIALS = 1001;
    public static final int ERROR_CODE_USER_NOT_FOUND = 1002;
    public static final int ERROR_CODE_ACCOUNT_LOCKED = 1003;
    public static final int ERROR_CODE_ACCOUNT_DISABLED = 1004;
    public static final int ERROR_CODE_SESSION_EXPIRED = 1005;
    public static final int ERROR_CODE_INVALID_TOKEN = 1006;
    public static final int ERROR_CODE_PERMISSION_DENIED = 1007;
    public static final int ERROR_CODE_USERNAME_EXISTS = 1008;
    public static final int ERROR_CODE_EMAIL_EXISTS = 1009;
    public static final int ERROR_CODE_WEAK_PASSWORD = 1010;
    public static final int ERROR_CODE_MAX_LOGIN_ATTEMPTS = 1011;
    public static final int ERROR_CODE_TWO_FACTOR_REQUIRED = 1012;
    public static final int ERROR_CODE_TWO_FACTOR_INVALID = 1013;
    public static final int ERROR_CODE_PASSWORD_EXPIRED = 1014;
    public static final int ERROR_CODE_SUSPICIOUS_ACTIVITY = 1015;
    
    // ==================== FIELDS ====================
    
    private final int errorCode;
    private final String errorType;
    private final boolean isRetryable;
    private final int remainingAttempts;
    private final long lockoutDurationMinutes;
    private final String recommendedAction;
    private final Throwable cause;
    private final long timestamp;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Constructs a new AuthenticationException with the specified detail message
     * 
     * @param message the detail message
     */
    public AuthenticationException(String message) {
        super(message);
        this.errorCode = ERROR_CODE_GENERAL;
        this.errorType = "GENERAL_ERROR";
        this.isRetryable = true;
        this.remainingAttempts = -1;
        this.lockoutDurationMinutes = 0;
        this.recommendedAction = "Please try again or contact support";
        this.cause = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new AuthenticationException with the specified detail message and cause
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ERROR_CODE_GENERAL;
        this.errorType = "GENERAL_ERROR";
        this.isRetryable = true;
        this.remainingAttempts = -1;
        this.lockoutDurationMinutes = 0;
        this.recommendedAction = "Please try again or contact support";
        this.cause = cause;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new AuthenticationException with error code and message
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public AuthenticationException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = getErrorTypeFromCode(errorCode);
        this.isRetryable = isErrorRetryable(errorCode);
        this.remainingAttempts = -1;
        this.lockoutDurationMinutes = 0;
        this.recommendedAction = getRecommendedAction(errorCode);
        this.cause = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new AuthenticationException with full details
     * 
     * @param errorCode the error code
     * @param message the detail message
     * @param remainingAttempts remaining login attempts before lockout
     * @param lockoutDurationMinutes lockout duration in minutes
     */
    public AuthenticationException(int errorCode, String message, int remainingAttempts, long lockoutDurationMinutes) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = getErrorTypeFromCode(errorCode);
        this.isRetryable = remainingAttempts > 0;
        this.remainingAttempts = remainingAttempts;
        this.lockoutDurationMinutes = lockoutDurationMinutes;
        this.recommendedAction = getRecommendedAction(errorCode);
        this.cause = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new AuthenticationException with cause
     * 
     * @param errorCode the error code
     * @param message the detail message
     * @param cause the cause
     */
    public AuthenticationException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = getErrorTypeFromCode(errorCode);
        this.isRetryable = isErrorRetryable(errorCode);
        this.remainingAttempts = -1;
        this.lockoutDurationMinutes = 0;
        this.recommendedAction = getRecommendedAction(errorCode);
        this.cause = cause;
        this.timestamp = System.currentTimeMillis();
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Creates an exception for invalid credentials
     * 
     * @return AuthenticationException instance
     */
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(
            ERROR_CODE_INVALID_CREDENTIALS,
            "Invalid username or password. Please check your credentials and try again."
        );
    }
    
    /**
     * Creates an exception for invalid credentials with remaining attempts
     * 
     * @param remainingAttempts number of attempts remaining before lockout
     * @return AuthenticationException instance
     */
    public static AuthenticationException invalidCredentials(int remainingAttempts) {
        String message = String.format(
            "Invalid username or password. You have %d attempt%s remaining.",
            remainingAttempts, remainingAttempts == 1 ? "" : "s"
        );
        return new AuthenticationException(ERROR_CODE_INVALID_CREDENTIALS, message, remainingAttempts, 0);
    }
    
    /**
     * Creates an exception for account lockout
     * 
     * @param lockoutMinutes number of minutes the account is locked
     * @return AuthenticationException instance
     */
    public static AuthenticationException accountLocked(long lockoutMinutes) {
        String message = String.format(
            "Account has been temporarily locked due to multiple failed attempts. Please try again in %d minute%s.",
            lockoutMinutes, lockoutMinutes == 1 ? "" : "s"
        );
        return new AuthenticationException(ERROR_CODE_ACCOUNT_LOCKED, message, 0, lockoutMinutes);
    }
    
    /**
     * Creates an exception for user not found
     * 
     * @param username the username that was not found
     * @return AuthenticationException instance
     */
    public static AuthenticationException userNotFound(String username) {
        return new AuthenticationException(
            ERROR_CODE_USER_NOT_FOUND,
            String.format("User '%s' was not found. Please check the username or register for a new account.", username)
        );
    }
    
    /**
     * Creates an exception for username already exists during registration
     * 
     * @param username the duplicate username
     * @return AuthenticationException instance
     */
    public static AuthenticationException usernameExists(String username) {
        return new AuthenticationException(
            ERROR_CODE_USERNAME_EXISTS,
            String.format("Username '%s' is already taken. Please choose a different username.", username)
        );
    }
    
    /**
     * Creates an exception for email already exists during registration
     * 
     * @param email the duplicate email
     * @return AuthenticationException instance
     */
    public static AuthenticationException emailExists(String email) {
        return new AuthenticationException(
            ERROR_CODE_EMAIL_EXISTS,
            String.format("Email '%s' is already registered. Please use a different email or try logging in.", email)
        );
    }
    
    /**
     * Creates an exception for weak password during registration
     * 
     * @param reason the reason why the password is weak
     * @return AuthenticationException instance
     */
    public static AuthenticationException weakPassword(String reason) {
        return new AuthenticationException(
            ERROR_CODE_WEAK_PASSWORD,
            String.format("Password is too weak: %s", reason)
        );
    }
    
    /**
     * Creates an exception for expired session
     * 
     * @return AuthenticationException instance
     */
    public static AuthenticationException sessionExpired() {
        return new AuthenticationException(
            ERROR_CODE_SESSION_EXPIRED,
            "Your session has expired. Please log in again to continue."
        );
    }
    
    /**
     * Creates an exception for invalid authentication token
     * 
     * @return AuthenticationException instance
     */
    public static AuthenticationException invalidToken() {
        return new AuthenticationException(
            ERROR_CODE_INVALID_TOKEN,
            "Invalid or expired authentication token. Please log in again."
        );
    }
    
    /**
     * Creates an exception for permission denied
     * 
     * @param action the action that was denied
     * @return AuthenticationException instance
     */
    public static AuthenticationException permissionDenied(String action) {
        return new AuthenticationException(
            ERROR_CODE_PERMISSION_DENIED,
            String.format("You don't have permission to %s. Please contact an administrator.", action)
        );
    }
    
    /**
     * Creates an exception for account disabled
     * 
     * @return AuthenticationException instance
     */
    public static AuthenticationException accountDisabled() {
        return new AuthenticationException(
            ERROR_CODE_ACCOUNT_DISABLED,
            "This account has been disabled. Please contact support for assistance."
        );
    }
    
    /**
     * Creates an exception for two-factor authentication required
     * 
     * @return AuthenticationException instance
     */
    public static AuthenticationException twoFactorRequired() {
        return new AuthenticationException(
            ERROR_CODE_TWO_FACTOR_REQUIRED,
            "Two-factor authentication is required for this account."
        );
    }
    
    /**
     * Creates an exception for invalid two-factor code
     * 
     * @return AuthenticationException instance
     */
    public static AuthenticationException invalidTwoFactorCode() {
        return new AuthenticationException(
            ERROR_CODE_TWO_FACTOR_INVALID,
            "Invalid two-factor authentication code. Please try again."
        );
    }
    
    /**
     * Creates an exception for expired password
     * 
     * @return AuthenticationException instance
     */
    public static AuthenticationException passwordExpired() {
        return new AuthenticationException(
            ERROR_CODE_PASSWORD_EXPIRED,
            "Your password has expired. Please reset your password to continue."
        );
    }
    
    /**
     * Creates an exception for suspicious activity detected
     * 
     * @return AuthenticationException instance
     */
    public static AuthenticationException suspiciousActivity() {
        return new AuthenticationException(
            ERROR_CODE_SUSPICIOUS_ACTIVITY,
            "Suspicious activity detected. Please verify your identity or contact support."
        );
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
            case ERROR_CODE_INVALID_CREDENTIALS: return "INVALID_CREDENTIALS";
            case ERROR_CODE_USER_NOT_FOUND: return "USER_NOT_FOUND";
            case ERROR_CODE_ACCOUNT_LOCKED: return "ACCOUNT_LOCKED";
            case ERROR_CODE_ACCOUNT_DISABLED: return "ACCOUNT_DISABLED";
            case ERROR_CODE_SESSION_EXPIRED: return "SESSION_EXPIRED";
            case ERROR_CODE_INVALID_TOKEN: return "INVALID_TOKEN";
            case ERROR_CODE_PERMISSION_DENIED: return "PERMISSION_DENIED";
            case ERROR_CODE_USERNAME_EXISTS: return "USERNAME_EXISTS";
            case ERROR_CODE_EMAIL_EXISTS: return "EMAIL_EXISTS";
            case ERROR_CODE_WEAK_PASSWORD: return "WEAK_PASSWORD";
            case ERROR_CODE_MAX_LOGIN_ATTEMPTS: return "MAX_LOGIN_ATTEMPTS";
            case ERROR_CODE_TWO_FACTOR_REQUIRED: return "TWO_FACTOR_REQUIRED";
            case ERROR_CODE_TWO_FACTOR_INVALID: return "TWO_FACTOR_INVALID";
            case ERROR_CODE_PASSWORD_EXPIRED: return "PASSWORD_EXPIRED";
            case ERROR_CODE_SUSPICIOUS_ACTIVITY: return "SUSPICIOUS_ACTIVITY";
            default: return "GENERAL_ERROR";
        }
    }
    
    /**
     * Determines if an error is retryable
     * 
     * @param code the error code
     * @return true if the error is retryable
     */
    private static boolean isErrorRetryable(int code) {
        switch (code) {
            case ERROR_CODE_INVALID_CREDENTIALS:
            case ERROR_CODE_TWO_FACTOR_INVALID:
                return true;
            case ERROR_CODE_ACCOUNT_LOCKED:
            case ERROR_CODE_ACCOUNT_DISABLED:
            case ERROR_CODE_PERMISSION_DENIED:
            case ERROR_CODE_USER_NOT_FOUND:
            default:
                return false;
        }
    }
    
    /**
     * Gets recommended action for an error code
     * 
     * @param code the error code
     * @return recommended action string
     */
    private static String getRecommendedAction(int code) {
        switch (code) {
            case ERROR_CODE_INVALID_CREDENTIALS:
                return "Verify your username and password. Check if Caps Lock is on.";
            case ERROR_CODE_USER_NOT_FOUND:
                return "Check the username or register for a new account.";
            case ERROR_CODE_ACCOUNT_LOCKED:
                return "Wait for the lockout period to end or reset your password.";
            case ERROR_CODE_ACCOUNT_DISABLED:
                return "Contact system administrator to re-enable your account.";
            case ERROR_CODE_SESSION_EXPIRED:
                return "Log in again to continue using the application.";
            case ERROR_CODE_INVALID_TOKEN:
                return "Clear browser cache and log in again.";
            case ERROR_CODE_PERMISSION_DENIED:
                return "Request necessary permissions from an administrator.";
            case ERROR_CODE_USERNAME_EXISTS:
                return "Choose a different username.";
            case ERROR_CODE_EMAIL_EXISTS:
                return "Use a different email or try logging in.";
            case ERROR_CODE_WEAK_PASSWORD:
                return "Use a stronger password with mixed case, numbers, and special characters.";
            case ERROR_CODE_TWO_FACTOR_REQUIRED:
                return "Set up two-factor authentication in your account settings.";
            case ERROR_CODE_TWO_FACTOR_INVALID:
                return "Check your authenticator app and try again.";
            case ERROR_CODE_PASSWORD_EXPIRED:
                return "Reset your password immediately.";
            case ERROR_CODE_SUSPICIOUS_ACTIVITY:
                return "Verify your identity or contact support.";
            default:
                return "Please try again or contact support if the problem persists.";
        }
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Gets the error code
     * 
     * @return the error code
     */
    public int getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the error type as a string
     * 
     * @return the error type
     */
    public String getErrorType() {
        return errorType;
    }
    
    /**
     * Checks if the error is retryable
     * 
     * @return true if retryable
     */
    public boolean isRetryable() {
        return isRetryable;
    }
    
    /**
     * Gets the number of remaining attempts before lockout
     * 
     * @return remaining attempts, or -1 if not applicable
     */
    public int getRemainingAttempts() {
        return remainingAttempts;
    }
    
    /**
     * Gets the lockout duration in minutes
     * 
     * @return lockout duration in minutes, or 0 if not applicable
     */
    public long getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
    
    /**
     * Gets the recommended action message
     * 
     * @return recommended action string
     */
    public String getRecommendedAction() {
        return recommendedAction;
    }
    
    /**
     * Gets the timestamp when the exception was created
     * 
     * @return timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the cause throwable
     * 
     * @return the cause, or null if none
     */
    public Throwable getCause() {
        return cause;
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Gets a user-friendly error message suitable for display in UI
     * 
     * @return user-friendly error message
     */
    public String getUserFriendlyMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage());
        
        if (remainingAttempts > 0) {
            sb.append("\n\n").append(remainingAttempts).append(" attempt(s) remaining.");
        }
        
        if (lockoutDurationMinutes > 0) {
            sb.append("\n\nAccount will be locked for ").append(lockoutDurationMinutes)
              .append(" minute(s).");
        }
        
        sb.append("\n\nRecommended action: ").append(recommendedAction);
        
        return sb.toString();
    }
    
    /**
     * Gets a formatted error message for logging
     * 
     * @return formatted log message
     */
    public String getLogMessage() {
        return String.format("[%s] Code: %d, Type: %s, Message: %s, Retryable: %b, Timestamp: %d",
            getClass().getSimpleName(),
            errorCode,
            errorType,
            getMessage(),
            isRetryable,
            timestamp
        );
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
     * Checks if this is a credentials-related error
     * 
     * @return true if credentials error
     */
    public boolean isCredentialsError() {
        return errorCode == ERROR_CODE_INVALID_CREDENTIALS ||
               errorCode == ERROR_CODE_PASSWORD_EXPIRED;
    }
    
    /**
     * Checks if this is an account-related error
     * 
     * @return true if account error
     */
    public boolean isAccountError() {
        return errorCode == ERROR_CODE_ACCOUNT_LOCKED ||
               errorCode == ERROR_CODE_ACCOUNT_DISABLED;
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
     * Checks if this is a session-related error
     * 
     * @return true if session error
     */
    public boolean isSessionError() {
        return errorCode == ERROR_CODE_SESSION_EXPIRED ||
               errorCode == ERROR_CODE_INVALID_TOKEN;
    }
    
    /**
     * Checks if this is a registration-related error
     * 
     * @return true if registration error
     */
    public boolean isRegistrationError() {
        return errorCode == ERROR_CODE_USERNAME_EXISTS ||
               errorCode == ERROR_CODE_EMAIL_EXISTS ||
               errorCode == ERROR_CODE_WEAK_PASSWORD;
    }
    
    // ==================== OVERRIDE METHODS ====================
    
    @Override
    public String toString() {
        return String.format("AuthenticationException{errorCode=%d, errorType='%s', message='%s', retryable=%b}",
            errorCode, errorType, getMessage(), isRetryable);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AuthenticationException that = (AuthenticationException) obj;
        return errorCode == that.errorCode && 
               timestamp == that.timestamp &&
               java.util.Objects.equals(getMessage(), that.getMessage());
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(errorCode, getMessage(), timestamp);
    }
}

// ==================== ADDITIONAL AUTHENTICATION EXCEPTION TYPES ====================

/**
 * TwoFactorAuthenticationException - Specific exception for 2FA failures
 */
class TwoFactorAuthenticationException extends AuthenticationException {
    public TwoFactorAuthenticationException(String message) {
        super(ERROR_CODE_TWO_FACTOR_INVALID, message);
    }
    
    public TwoFactorAuthenticationException(String message, boolean isInvalidCode) {
        super(isInvalidCode ? ERROR_CODE_TWO_FACTOR_INVALID : ERROR_CODE_TWO_FACTOR_REQUIRED, message);
    }
}

/**
 * SessionExpiredException - Specific exception for expired sessions
 */
class SessionExpiredException extends AuthenticationException {
    public SessionExpiredException() {
        super(ERROR_CODE_SESSION_EXPIRED, "Your session has expired. Please log in again.");
    }
    
    public SessionExpiredException(String message) {
        super(ERROR_CODE_SESSION_EXPIRED, message);
    }
}

/**
 * AccountLockedException - Specific exception for locked accounts
 */
class AccountLockedException extends AuthenticationException {
    private final long lockoutMinutes;
    
    public AccountLockedException(long lockoutMinutes) {
        super(ERROR_CODE_ACCOUNT_LOCKED, 
              String.format("Account locked. Try again in %d minutes.", lockoutMinutes),
              0, lockoutMinutes);
        this.lockoutMinutes = lockoutMinutes;
    }
    
    public long getLockoutMinutes() {
        return lockoutMinutes;
    }
}

/**
 * PermissionDeniedException - Specific exception for permission errors
 */
class PermissionDeniedException extends AuthenticationException {
    private final String requiredPermission;
    
    public PermissionDeniedException(String action, String requiredPermission) {
        super(ERROR_CODE_PERMISSION_DENIED, 
              String.format("Permission denied: Cannot %s", action));
        this.requiredPermission = requiredPermission;
    }
    
    public String getRequiredPermission() {
        return requiredPermission;
    }
}

/**
 * RegistrationException - Specific exception for registration errors
 */
class RegistrationException extends AuthenticationException {
    private final String fieldName;
    
    public RegistrationException(String fieldName, String message) {
        super(getErrorCodeForField(fieldName), message);
        this.fieldName = fieldName;
    }
    
    private static int getErrorCodeForField(String fieldName) {
        if ("username".equalsIgnoreCase(fieldName)) return ERROR_CODE_USERNAME_EXISTS;
        if ("email".equalsIgnoreCase(fieldName)) return ERROR_CODE_EMAIL_EXISTS;
        if ("password".equalsIgnoreCase(fieldName)) return ERROR_CODE_WEAK_PASSWORD;
        return ERROR_CODE_GENERAL;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}
