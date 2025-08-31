package com.todoapp.auth.service;

import com.todoapp.auth.dto.*;
import com.todoapp.auth.entity.RefreshToken;
import com.todoapp.auth.entity.User;
import com.todoapp.auth.exception.InvalidCredentialsException;
import com.todoapp.auth.exception.TokenExpiredException;
import com.todoapp.auth.exception.UserNotFoundException;
import com.todoapp.auth.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Getter
@Setter
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserService userService, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Register new user
     */
    public AuthResponse register(RegisterRequest registerRequest, HttpServletRequest request) {
        // Create user
        User user = userService.createUser(registerRequest);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token to database
        saveRefreshToken(user, refreshToken, request);

        // Create response
        UserDto userDto = UserDto.fromUser(user);
        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiration() / 1000,
                userDto
        );
    }

    /**
     * Login user
     */
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        // Find user
        Optional<User> userOpt = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());

        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        User user = userOpt.get();

        // Check if account should be unlocked
        if (user.isAccountLocked() && userService.shouldUnlockAccount(user)) {
            userService.unlockAccount(user.getId());
            user.setAccountLocked(false);
            user.resetLoginAttempts();
        }

        // Check if user can login
        if (!userService.canUserLogin(user)) {
            if (user.isAccountLocked()) {
                throw new InvalidCredentialsException("Account is locked due to too many failed login attempts");
            } else {
                throw new InvalidCredentialsException("Account is disabled");
            }
        }

        // Validate password
        if (!userService.validateCredentials(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())) {
            // Handle failed login
            userService.handleFailedLogin(user);
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        // Handle successful login
        userService.handleSuccessfulLogin(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token to database
        saveRefreshToken(user, refreshToken, request);

        // Create response
        UserDto userDto = UserDto.fromUser(user);
        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiration() / 1000,
                userDto
        );
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(RefreshTokenRequest refreshRequest) {
        String refreshTokenStr = refreshRequest.getRefreshToken();

        // Validate refresh token format
        if (!jwtService.validateRefreshToken(refreshTokenStr)) {
            throw new TokenExpiredException("Invalid or expired refresh token");
        }

        // Find refresh token in database
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenStr);

        if (refreshTokenOpt.isEmpty() || !refreshTokenOpt.get().isValid()) {
            throw new TokenExpiredException("Refresh token not found or revoked");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        User user = refreshToken.getUser();

        // Check if user is still active
        if (!userService.canUserLogin(user)) {
            // Revoke all tokens for this user
            revokeAllUserTokens(user);
            throw new InvalidCredentialsException("User account is disabled");
        }

        // Update last used timestamp
        refreshToken.updateLastUsed();
        refreshTokenRepository.save(refreshToken);

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        // Create response (reuse existing refresh token)
        UserDto userDto = UserDto.fromUser(user);
        return new AuthResponse(
                newAccessToken,
                refreshTokenStr,
                jwtService.getAccessTokenExpiration() / 1000,
                userDto
        );
    }

    /**
     * Logout user (blacklist access token and revoke refresh token)
     */
    public void logout(String accessToken, String refreshTokenStr) {
        // Blacklist access token
        if (accessToken != null && !accessToken.isEmpty()) {
            jwtService.blacklistToken(accessToken);
        }

        // Revoke refresh token
        if (refreshTokenStr != null && !refreshTokenStr.isEmpty()) {
            refreshTokenRepository.revokeToken(refreshTokenStr);
        }
    }

    /**
     * Logout all devices (revoke all refresh tokens for user)
     */
    public void logoutAllDevices(String username) {
        Optional<User> userOpt = userService.findByUsernameOrEmail(username);
        if (userOpt.isPresent()) {
            revokeAllUserTokens(userOpt.get());
        }
    }

    /**
     * Get user profile
     */
    @Transactional(readOnly = true)
    public UserDto getUserProfile(String username) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + username);
        }

        return UserDto.fromUser(userOpt.get());
    }

    /**
     * Update user profile
     */
    public UserDto updateProfile(String username, String firstName, String lastName) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + username);
        }

        return userService.updateProfile(userOpt.get().getId(), firstName, lastName);
    }

    /**
     * Change password
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + username);
        }

        userService.changePassword(userOpt.get().getId(), currentPassword, newPassword);

        // Revoke all tokens to force re-login
        revokeAllUserTokens(userOpt.get());
    }

    /**
     * Validate token
     */
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return jwtService.isValidToken(token);
    }

    /**
     * Get user active sessions (refresh tokens)
     */
    @Transactional(readOnly = true)
    public List<RefreshToken> getUserActiveSessions(String username) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + username);
        }

        return refreshTokenRepository.findValidTokensByUser(userOpt.get(), LocalDateTime.now());
    }

    /**
     * Revoke specific session
     */
    public void revokeSession(String username, Long tokenId) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + username);
        }

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findById(tokenId);
        if (tokenOpt.isPresent() && tokenOpt.get().getUser().equals(userOpt.get())) {
            tokenOpt.get().revoke();
            refreshTokenRepository.save(tokenOpt.get());
        }
    }

    /**
     * Clean up expired tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * Clean up old revoked tokens
     */
    @Transactional
    public void cleanupOldRevokedTokens(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        refreshTokenRepository.deleteRevokedTokensOlderThan(cutoffDate);
    }

    // Private helper methods

    /**
     * Save refresh token to database
     */
    private void saveRefreshToken(User user, String tokenStr, HttpServletRequest request) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenStr);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(jwtService.getRefreshTokenExpiration() * 1_000_000));

        // Extract device and IP information
        if (request != null) {
            refreshToken.setIpAddress(getClientIpAddress(request));
            refreshToken.setDeviceInfo(extractDeviceInfo(request));
        }

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revoke all refresh tokens for user
     */
    private void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    /**
     * Extract client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Extract device information from request
     */
    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && !userAgent.isEmpty()) {
            return userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent;
        }
        return "Unknown Device";
    }
}