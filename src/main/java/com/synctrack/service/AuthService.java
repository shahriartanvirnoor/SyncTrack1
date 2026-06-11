package com.synctrack.service;

import com.synctrack.model.User;
import com.synctrack.repository.UserRepository;
import com.synctrack.util.PasswordHasher;

public class AuthService {
    private UserRepository userRepository;
    private PasswordHasher hasher;
    private User currentUser;
    
    public AuthService() {
        this.userRepository = new UserRepository();
        this.hasher = new PasswordHasher();
    }
    
    public User login(String username, String password) throws Exception {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new Exception("User not found");
        
        String hash = hasher.hashPassword(password, user.getSalt());
        if (!hash.equals(user.getPasswordHash())) throw new Exception("Invalid password");
        
        this.currentUser = user;
        userRepository.updateLastLogin(user.getUserId());
        return user;
    }
    
    public User register(String username, String email, String password) throws Exception {
        if (userRepository.findByUsername(username) != null) throw new Exception("Username exists");
        if (userRepository.findByEmail(email) != null) throw new Exception("Email exists");
        if (password.length() < 6) throw new Exception("Password too short");
        
        String salt = hasher.generateSalt();
        String hash = hasher.hashPassword(password, salt);
        
        User user = new User(username, email, hash, salt);
        return userRepository.create(user);
    }
    
    public void logout() {
        this.currentUser = null;
    }
    
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void updateUser(User user) {
        userRepository.update(user);
        this.currentUser = user;
    }
    
    public void changePassword(String newPassword) {
        String salt = hasher.generateSalt();
        String hash = hasher.hashPassword(newPassword, salt);
        currentUser.setPasswordHash(hash);
        currentUser.setSalt(salt);
        userRepository.update(currentUser);
    }
    
    public void resetProgress() {
        // Implementation would reset all user data
    }
}
