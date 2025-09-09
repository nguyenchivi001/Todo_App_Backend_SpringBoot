package com.todoapp.auth.service;

import com.todoapp.auth.entity.User;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.function.Function;

public interface JwtService {

    long getAccessTokenExpiration();

    long getRefreshTokenExpiration();

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String extractUsername(String token);

    String extractUserId(String token);

    String extractTokenType(String token);

    Date extractExpiration(String token);

    Date extractIssuedAt(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    boolean isTokenExpired(String token);

    boolean isTokenBlacklisted(String token);

    boolean isValidToken(String token);

    boolean validateAccessToken(String token);

    boolean validateRefreshToken(String token);

    void blacklistToken(String token);

    long getTokenExpirationTime(String token);

    boolean shouldRefreshToken(String token);

    Claims parseTokenUnsafe(String token);
}
