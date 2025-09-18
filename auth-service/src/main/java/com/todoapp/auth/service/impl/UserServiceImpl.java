package com.todoapp.auth.service.impl;

import com.todoapp.auth.dto.RegisterRequest;
import com.todoapp.auth.dto.UserDto;
import com.todoapp.auth.entity.User;
import com.todoapp.auth.exception.UserNotFoundException;
import com.todoapp.auth.repository.UserRepository;
import com.todoapp.auth.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${security.account-lockout-duration:3600000}") // 1 hour in milliseconds
    private long accountLockoutDuration;

    /**
     * Create new user
     */
    public User createUser(RegisterRequest registerRequest) {
        // Check if username or email already exists
        if (userRepository.existsByUsernameOrEmail(registerRequest.getUsername(), registerRequest.getEmail())) {
            throw new IllegalArgumentException("Username or email already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setLoginAttempts(0);

        return userRepository.save(user);
    }

    /**
     * Find user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by username or email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    /**
     * Get user DTO by ID
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = findById(id);
        return UserDto.fromUser(user);
    }

    /**
     * Update user's last login time
     */
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }

    /**
     * Handle successful login
     */
    public void handleSuccessfulLogin(User user) {
        // Reset login attempts and unlock account if needed
        user.resetLoginAttempts();
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Handle failed login attempt
     */
    public void handleFailedLogin(User user) {
        user.incrementLoginAttempts();

        // Lock account if max attempts reached
        if (user.getLoginAttempts() >= maxLoginAttempts) {
            user.setAccountLocked(true);
        }

        userRepository.save(user);
    }

    /**
     * Check if user account is locked and should be unlocked
     */
    public boolean shouldUnlockAccount(User user) {
        if (!user.isAccountLocked()) {
            return false;
        }

        LocalDateTime lastUpdate = user.getUpdatedAt();
        if (lastUpdate == null) {
            return false;
        }

        LocalDateTime unlockTime = lastUpdate.plusNanos(accountLockoutDuration * 1_000_000);
        return LocalDateTime.now().isAfter(unlockTime);
    }

    /**
     * Unlock user account
     */
    public void unlockAccount(Long userId) {
        userRepository.unlockUserAccount(userId);
    }

    /**
     * Lock user account
     */
    public void lockAccount(Long userId) {
        userRepository.lockUserAccount(userId);
    }

    /**
     * Enable or disable user account
     */
    public void updateUserStatus(Long userId, Boolean enabled) {
        userRepository.updateUserStatus(userId, enabled);
    }

    /**
     * Check if username exists
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Update user profile
     */
    public UserDto updateProfile(Long userId, String firstName, String lastName) {
        User user = findById(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user = userRepository.save(user);
        return UserDto.fromUser(user);
    }

    /**
     * Change user password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Reset user password (admin function)
     */
    public void resetPassword(Long userId, String newPassword) {
        User user = findById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.resetLoginAttempts(); // Reset attempts when password is reset
        userRepository.save(user);
    }

    /**
     * Get all users (admin function)
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Get active users
     */
    @Transactional(readOnly = true)
    public List<UserDto> getActiveUsers() {
        return userRepository.findByEnabledTrue().stream()
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Search users by username
     */
    @Transactional(readOnly = true)
    public List<UserDto> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username).stream()
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Search users by email
     */
    @Transactional(readOnly = true)
    public List<UserDto> searchUsersByEmail(String email) {
        return userRepository.findByEmailContainingIgnoreCase(email).stream()
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Get users registered today
     */
    @Transactional(readOnly = true)
    public long getUsersRegisteredToday() {
        return userRepository.countUsersRegisteredToday();
    }

    /**
     * Get total active users count
     */
    @Transactional(readOnly = true)
    public long getActiveUsersCount() {
        return userRepository.countActiveUsers();
    }

    /**
     * Get inactive users (haven't logged in for specified days)
     */
    @Transactional(readOnly = true)
    public List<UserDto> getInactiveUsers(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return userRepository.findInactiveUsers(cutoffDate).stream()
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Delete user account
     */
    public void deleteUser(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }

    /**
     * Validate user credentials
     */
    @Transactional(readOnly = true)
    public boolean validateCredentials(String usernameOrEmail, String password) {
        Optional<User> userOpt = findByUsernameOrEmail(usernameOrEmail);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * Check if user can login (not locked, enabled, etc.)
     */
    @Transactional(readOnly = true)
    public boolean canUserLogin(User user) {
        if (!user.isEnabled()) {
            return false;
        }

        if (user.isAccountLocked()) {
            // Check if account should be automatically unlocked
            return shouldUnlockAccount(user);
        }

        return true;
    }
}