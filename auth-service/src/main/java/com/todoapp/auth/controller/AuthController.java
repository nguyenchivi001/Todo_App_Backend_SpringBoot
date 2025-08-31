package com.todoapp.auth.controller;

import com.todoapp.auth.dto.*;
import com.todoapp.auth.entity.RefreshToken;
import com.todoapp.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {

        AuthResponse response = authService.register(registerRequest, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        AuthResponse response = authService.login(loginRequest, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        AuthResponse response = authService.refreshToken(refreshRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, String> body) {

        // Extract access token from Authorization header
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        // Extract refresh token from request body
        String refreshToken = null;
        if (body != null && body.containsKey("refresh_token")) {
            refreshToken = body.get("refresh_token");
        }

        authService.logout(accessToken, refreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully logged out");
        return ResponseEntity.ok(response);
    }

    /**
     * Logout from all devices
     */
    @PostMapping("/logout-all")
    public ResponseEntity<Map<String, String>> logoutAll(Principal principal) {
        authService.logoutAllDevices(principal.getName());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully logged out from all devices");
        return ResponseEntity.ok(response);
    }

    /**
     * Get user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(Principal principal) {
        UserDto userProfile = authService.getUserProfile(principal.getName());
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            Principal principal,
            @RequestBody Map<String, String> updates) {

        String firstName = updates.get("firstName");
        String lastName = updates.get("lastName");

        UserDto updatedProfile = authService.updateProfile(principal.getName(), firstName, lastName);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Principal principal,
            @RequestBody Map<String, String> passwordData) {

        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Both currentPassword and newPassword are required");
            return ResponseEntity.badRequest().body(error);
        }

        authService.changePassword(principal.getName(), currentPassword, newPassword);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully. Please log in again.");
        return ResponseEntity.ok(response);
    }

    /**
     * Validate token
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> tokenData) {
        String token = tokenData.get("token");

        if (token == null || token.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", "Token is required");
            return ResponseEntity.badRequest().body(error);
        }

        boolean isValid = authService.validateToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);

        if (!isValid) {
            response.put("error", "Invalid or expired token");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get user active sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<RefreshToken>> getActiveSessions(Principal principal) {
        List<RefreshToken> sessions = authService.getUserActiveSessions(principal.getName());
        return ResponseEntity.ok(sessions);
    }

    /**
     * Revoke specific session
     */
    @DeleteMapping("/sessions/{tokenId}")
    public ResponseEntity<Map<String, String>> revokeSession(
            Principal principal,
            @PathVariable Long tokenId) {

        authService.revokeSession(principal.getName(), tokenId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Session revoked successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "auth-service");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }

    /**
     * Get service info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "auth-service");
        info.put("version", "1.0.0");
        info.put("description", "Authentication and Authorization Service");
        info.put("endpoints", Map.of(
                "register", "POST /api/auth/register",
                "login", "POST /api/auth/login",
                "refresh", "POST /api/auth/refresh",
                "logout", "POST /api/auth/logout",
                "profile", "GET /api/auth/profile",
                "change-password", "POST /api/auth/change-password",
                "validate", "POST /api/auth/validate"
        ));
        return ResponseEntity.ok(info);
    }

    /**
     * Check username availability
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        boolean exists = authService.getUserService().existsByUsername(username);

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("available", !exists);

        return ResponseEntity.ok(response);
    }

    /**
     * Check email availability
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean exists = authService.getUserService().existsByEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("available", !exists);

        return ResponseEntity.ok(response);
    }
}
