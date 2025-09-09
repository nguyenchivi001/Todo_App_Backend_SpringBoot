package com.todoapp.auth.service.impl;

import com.todoapp.auth.dto.*;
import com.todoapp.auth.entity.RefreshToken;
import com.todoapp.auth.entity.User;
import com.todoapp.auth.exception.InvalidCredentialsException;
import com.todoapp.auth.exception.TokenExpiredException;
import com.todoapp.auth.exception.UserNotFoundException;
import com.todoapp.auth.repository.RefreshTokenRepository;
import com.todoapp.auth.service.AuthService;
import com.todoapp.auth.service.JwtService;
import com.todoapp.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public AuthResponse register(RegisterRequest registerRequest, HttpServletRequest request) {
        User user = userService.createUser(registerRequest);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveRefreshToken(user, refreshToken, request);
        UserDto userDto = UserDto.fromUser(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiration() / 1000,
                userDto
        );
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        Optional<User> userOpt = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        User user = userOpt.get();

        if (user.isAccountLocked() && userService.shouldUnlockAccount(user)) {
            userService.unlockAccount(user.getId());
            user.setAccountLocked(false);
            user.resetLoginAttempts();
        }

        if (!userService.canUserLogin(user)) {
            if (user.isAccountLocked()) {
                throw new InvalidCredentialsException("Account is locked due to too many failed login attempts");
            } else {
                throw new InvalidCredentialsException("Account is disabled");
            }
        }

        if (!userService.validateCredentials(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())) {
            userService.handleFailedLogin(user);
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        userService.handleSuccessfulLogin(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveRefreshToken(user, refreshToken, request);

        UserDto userDto = UserDto.fromUser(user);
        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiration() / 1000,
                userDto
        );
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest refreshRequest) {
        String refreshTokenStr = refreshRequest.getRefreshToken();

        if (!jwtService.validateRefreshToken(refreshTokenStr)) {
            throw new TokenExpiredException("Invalid or expired refresh token");
        }

        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenStr);
        if (refreshTokenOpt.isEmpty() || !refreshTokenOpt.get().isValid()) {
            throw new TokenExpiredException("Refresh token not found or revoked");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        User user = refreshToken.getUser();

        if (!userService.canUserLogin(user)) {
            revokeAllUserTokens(user);
            throw new InvalidCredentialsException("User account is disabled");
        }

        refreshToken.updateLastUsed();
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtService.generateAccessToken(user);

        UserDto userDto = UserDto.fromUser(user);
        return new AuthResponse(
                newAccessToken,
                refreshTokenStr,
                jwtService.getAccessTokenExpiration() / 1000,
                userDto
        );
    }

    @Override
    public void logout(String accessToken, String refreshTokenStr) {
        if (accessToken != null && !accessToken.isEmpty()) {
            jwtService.blacklistToken(accessToken);
        }
        if (refreshTokenStr != null && !refreshTokenStr.isEmpty()) {
            refreshTokenRepository.revokeToken(refreshTokenStr);
        }
    }

    @Override
    public void logoutAllDevices(String username) {
        userService.findByUsernameOrEmail(username)
                .ifPresent(this::revokeAllUserTokens);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserProfile(String username) {
        return userService.findByUsername(username)
                .map(UserDto::fromUser)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    @Override
    public UserDto updateProfile(String username, String firstName, String lastName) {
        return userService.findByUsername(username)
                .map(user -> userService.updateProfile(user.getId(), firstName, lastName))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        userService.changePassword(user.getId(), currentPassword, newPassword);
        revokeAllUserTokens(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return jwtService.isValidToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> getUserActiveSessions(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        return refreshTokenRepository.findValidTokensByUser(user, LocalDateTime.now());
    }

    @Override
    public void revokeSession(String username, Long tokenId) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        refreshTokenRepository.findById(tokenId)
                .filter(token -> token.getUser().equals(user))
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void cleanupOldRevokedTokens(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        refreshTokenRepository.deleteRevokedTokensOlderThan(cutoffDate);
    }

    // Private helpers
    private void saveRefreshToken(User user, String tokenStr, HttpServletRequest request) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenStr);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(jwtService.getRefreshTokenExpiration() * 1_000_000));

        if (request != null) {
            refreshToken.setIpAddress(getClientIpAddress(request));
            refreshToken.setDeviceInfo(extractDeviceInfo(request));
        }

        refreshTokenRepository.save(refreshToken);
    }

    private void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }

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

    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && !userAgent.isEmpty()) {
            return userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent;
        }
        return "Unknown Device";
    }
}