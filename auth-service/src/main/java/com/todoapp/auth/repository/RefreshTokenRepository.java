package com.todoapp.auth.repository;

import com.todoapp.auth.entity.RefreshToken;
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
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all valid refresh tokens for a user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Find all refresh tokens for a user
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find all refresh tokens for a user ordered by creation date
     */
    List<RefreshToken> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Check if token exists and is valid
     */
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false AND rt.expiresAt > :now")
    boolean existsValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Revoke all tokens for a user
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllUserTokens(@Param("user") User user);

    /**
     * Revoke specific token
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    void revokeToken(@Param("token") String token);

    /**
     * Delete expired tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete revoked tokens older than specified date
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true AND rt.createdAt < :cutoffDate")
    void deleteRevokedTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find tokens by user and device info
     */
    List<RefreshToken> findByUserAndDeviceInfo(User user, String deviceInfo);

    /**
     * Find tokens by IP address
     */
    List<RefreshToken> findByIpAddress(String ipAddress);

    /**
     * Update last used timestamp
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.lastUsed = :lastUsed WHERE rt.token = :token")
    void updateLastUsed(@Param("token") String token, @Param("lastUsed") LocalDateTime lastUsed);

    /**
     * Find expired but not revoked tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now AND rt.revoked = false")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count valid tokens for user
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
    long countValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Find recently used tokens (for security monitoring)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.lastUsed > :since ORDER BY rt.lastUsed DESC")
    List<RefreshToken> findRecentlyUsedTokens(@Param("since") LocalDateTime since);

    /**
     * Find tokens created from suspicious IP (multiple users)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.ipAddress = :ipAddress GROUP BY rt.user HAVING COUNT(rt.user) > :threshold")
    List<RefreshToken> findSuspiciousTokensByIp(@Param("ipAddress") String ipAddress, @Param("threshold") long threshold);
}
