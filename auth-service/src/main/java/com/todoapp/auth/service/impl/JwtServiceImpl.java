package com.todoapp.auth.service.impl;

import com.todoapp.auth.config.JwtConfig;
import com.todoapp.auth.entity.User;
import com.todoapp.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, Object> redisTemplate;

    public long getAccessTokenExpiration() {
        return jwtConfig.getAccessTokenExpiration();
    }

    public long getRefreshTokenExpiration() {
        return jwtConfig.getRefreshTokenExpiration();
    }

    /**
     * Generate access token for user
     */
    public String generateAccessToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "access");
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("email", user.getEmail());
        extraClaims.put("enabled", user.isEnabled());

        return generateToken(extraClaims, user.getUsername(), jwtConfig.getAccessTokenExpiration());
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "refresh");
        extraClaims.put("userId", user.getId().toString());

        return generateToken(extraClaims, user.getUsername(), jwtConfig.getRefreshTokenExpiration());
    }

    /**
     * Generate token with custom claims and expiration
     */
    private String generateToken(Map<String, Object> extraClaims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Extract token type (access/refresh)
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract issued at date from token
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Extract any claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token: " + e.getMessage());
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        String key = "blacklisted_token:" + token;
        return redisTemplate.hasKey(key);
    }

    /**
     * Validate token
     */
    public boolean isValidToken(String token) {
        try {
            return !isTokenExpired(token) && !isTokenBlacklisted(token);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Validate access token
     */
    public boolean validateAccessToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "access".equals(tokenType) && isValidToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate refresh token
     */
    public boolean validateRefreshToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "refresh".equals(tokenType) && isValidToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Blacklist token (for logout)
     */
    public void blacklistToken(String token) {
        try {
            Date expiration = extractExpiration(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();

            if (ttl > 0) {
                String key = "blacklisted_token:" + token;
                redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // Token is already invalid, no need to blacklist
        }
    }

    /**
     * Get remaining time until token expiration (in seconds)
     */
    public long getTokenExpirationTime(String token) {
        Date expiration = extractExpiration(token);
        return (expiration.getTime() - System.currentTimeMillis()) / 1000;
    }

    /**
     * Check if token needs refresh (expires within next 5 minutes)
     */
    public boolean shouldRefreshToken(String token) {
        long expirationTime = getTokenExpirationTime(token);
        return expirationTime < 300; // Less than 5 minutes
    }

    /**
     * Get signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Convert LocalDateTime to Date
     */
    private Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Parse token without validation (for debugging)
     */
    public Claims parseTokenUnsafe(String token) {
        try {
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                throw new JwtException("Invalid token format");
            }

            byte[] decodedBytes = Decoders.BASE64URL.decode(chunks[1]);
            String decodedString = new String(decodedBytes);

            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new JwtException("Cannot parse token: " + e.getMessage());
        }
    }
}