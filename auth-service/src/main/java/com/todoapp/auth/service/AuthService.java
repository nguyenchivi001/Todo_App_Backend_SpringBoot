package com.todoapp.auth.service;

import com.todoapp.auth.dto.*;
import com.todoapp.auth.entity.RefreshToken;

import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest, jakarta.servlet.http.HttpServletRequest request);

    AuthResponse login(LoginRequest loginRequest, jakarta.servlet.http.HttpServletRequest request);

    AuthResponse refreshToken(RefreshTokenRequest refreshRequest);

    void logout(String accessToken, String refreshTokenStr);

    void logoutAllDevices(String username);

    UserDto getUserProfile(String username);

    UserDto updateProfile(String username, String firstName, String lastName);

    void changePassword(String username, String currentPassword, String newPassword);

    boolean validateToken(String token);

    List<RefreshToken> getUserActiveSessions(String username);

    void revokeSession(String username, Long tokenId);

    void cleanupExpiredTokens();

    void cleanupOldRevokedTokens(int daysOld);
}