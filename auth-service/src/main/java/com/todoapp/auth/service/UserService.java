package com.todoapp.auth.service;

import com.todoapp.auth.dto.RegisterRequest;
import com.todoapp.auth.dto.UserDto;
import com.todoapp.auth.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(RegisterRequest registerRequest);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    User findById(Long id);

    UserDto getUserById(Long id);

    void updateLastLogin(Long userId);

    void handleSuccessfulLogin(User user);

    void handleFailedLogin(User user);

    boolean shouldUnlockAccount(User user);

    void unlockAccount(Long userId);

    void lockAccount(Long userId);

    void updateUserStatus(Long userId, Boolean enabled);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    UserDto updateProfile(Long userId, String firstName, String lastName);

    void changePassword(Long userId, String currentPassword, String newPassword);

    void resetPassword(Long userId, String newPassword);

    List<UserDto> getAllUsers();

    List<UserDto> getActiveUsers();

    List<UserDto> searchUsersByUsername(String username);

    List<UserDto> searchUsersByEmail(String email);

    long getUsersRegisteredToday();

    long getActiveUsersCount();

    List<UserDto> getInactiveUsers(int days);

    void deleteUser(Long userId);

    boolean validateCredentials(String usernameOrEmail, String password);

    boolean canUserLogin(User user);
}
