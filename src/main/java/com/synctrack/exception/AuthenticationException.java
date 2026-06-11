// AuthenticationException.java
package com.synctrack.exception;

public class AuthenticationException extends Exception {
    
    public static final int ERROR_CODE_GENERAL = 1000;
    public static final int ERROR_CODE_INVALID_CREDENTIALS = 1001;
    public static final int ERROR_CODE_USER_NOT_FOUND = 1002;
    public static final int ERROR_CODE_ACCOUNT_LOCKED = 1003;
    public static final int ERROR_CODE_USERNAME_EXISTS = 1008;
    public static final int ERROR_CODE_EMAIL_EXISTS = 1009;
    public static final int ERROR_CODE_WEAK_PASSWORD = 1010;
    
    private final int errorCode;
    private final int remainingAttempts;
    private final long lockoutDurationMinutes;
    private final long timestamp;
    
    public AuthenticationException(String message) {
        super(message);
        this.errorCode = ERROR_CODE_GENERAL;
        this.remainingAttempts = -1;
        this.lockoutDurationMinutes = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    public AuthenticationException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.remainingAttempts = -1;
        this.lockoutDurationMinutes = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    public AuthenticationException(int errorCode, String message, int remainingAttempts, long lockoutDurationMinutes) {
        super(message);
        this.errorCode = errorCode;
        this.remainingAttempts = remainingAttempts;
        this.lockoutDurationMinutes = lockoutDurationMinutes;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Factory methods
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(ERROR_CODE_INVALID_CREDENTIALS, "Invalid username or password");
    }
    
    public static AuthenticationException invalidCredentials(int remainingAttempts) {
        return new AuthenticationException(ERROR_CODE_INVALID_CREDENTIALS, 
            String.format("Invalid credentials. %d attempts remaining.", remainingAttempts),
            remainingAttempts, 0);
    }
    
    public static AuthenticationException accountLocked(long lockoutMinutes) {
        return new AuthenticationException(ERROR_CODE_ACCOUNT_LOCKED,
            String.format("Account locked. Try again in %d minutes.", lockoutMinutes),
            0, lockoutMinutes);
    }
    
    public static AuthenticationException userNotFound(String username) {
        return new AuthenticationException(ERROR_CODE_USER_NOT_FOUND, "User not found: " + username);
    }
    
    public static AuthenticationException usernameExists(String username) {
        return new AuthenticationException(ERROR_CODE_USERNAME_EXISTS, "Username already exists: " + username);
    }
    
    public static AuthenticationException emailExists(String email) {
        return new AuthenticationException(ERROR_CODE_EMAIL_EXISTS, "Email already registered: " + email);
    }
    
    public static AuthenticationException weakPassword(String reason) {
        return new AuthenticationException(ERROR_CODE_WEAK_PASSWORD, "Weak password: " + reason);
    }
    
    // Getters
    public int getErrorCode() { return errorCode; }
    public int getRemainingAttempts() { return remainingAttempts; }
    public long getLockoutDurationMinutes() { return lockoutDurationMinutes; }
    public long getTimestamp() { return timestamp; }
    
    public String getUserFriendlyMessage() {
        return getMessage();
    }
    
    public String getLogMessage() {
        return String.format("[AuthenticationException] Code: %d, Message: %s", errorCode, getMessage());
    }
    
    @Override
    public String toString() {
        return String.format("AuthenticationException{code=%d, message='%s'}", errorCode, getMessage());
    }
}
