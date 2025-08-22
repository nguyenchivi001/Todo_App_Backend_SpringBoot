package com.todoapp.auth.repository;

import com.todoapp.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if username or email exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username OR u.email = :email")
    boolean existsByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    /**
     * Find all active users
     */
    List<User> findByEnabledTrue();

    /**
     * Find all locked users
     */
    List<User> findByAccountLockedTrue();

    /**
     * Find users created after specific date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find users who haven't logged in recently
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL OR u.lastLogin < :cutoffDate")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Update user's last login time
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

    /**
     * Update user's login attempts
     */
    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = :attempts WHERE u.id = :userId")
    void updateLoginAttempts(@Param("userId") Long userId, @Param("attempts") Integer attempts);

    /**
     * Lock user account
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = true WHERE u.id = :userId")
    void lockUserAccount(@Param("userId") Long userId);

    /**
     * Unlock user account and reset login attempts
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = false, u.loginAttempts = 0 WHERE u.id = :userId")
    void unlockUserAccount(@Param("userId") Long userId);

    /**
     * Enable/disable user account
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    void updateUserStatus(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    /**
     * Find users by partial username (for admin search)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<User> findByUsernameContainingIgnoreCase(@Param("username") String username);

    /**
     * Find users by partial email (for admin search)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> findByEmailContainingIgnoreCase(@Param("email") String email);

    /**
     * Count total active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();

    /**
     * Count users registered today
     */
    @Query("SELECT COUNT(u) FROM User u WHERE DATE(u.createdAt) = CURRENT_DATE")
    long countUsersRegisteredToday();
}