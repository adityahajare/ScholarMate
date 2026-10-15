package com.scholarmate.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secretString;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    @Autowired
    public JwtService(
            @Value("${scholarmate.jwt.secret:scholarmate-super-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256-security}") String secretString,
            @Value("${scholarmate.jwt.access-expiration-ms:${scholarmate.jwt.expiration-ms:900000}}") long accessExpirationMs,
            @Value("${scholarmate.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.secretString = secretString;
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public JwtService(String secretString, long expirationMs) {
        this.secretString = secretString;
        this.accessExpirationMs = expirationMs;
        this.refreshExpirationMs = 604800000L;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, String role, Long userId) {
        return generateAccessToken(username, role, userId);
    }

    public String generateAccessToken(String username, String role, Long userId) {
        return createToken(username, role, userId, "access", accessExpirationMs);
    }

    public String generateRefreshToken(String username, Long userId) {
        return createToken(username, null, userId, "refresh", refreshExpirationMs);
    }

    private String createToken(String username, String role, Long userId, String tokenType, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        if (role != null) {
            claims.put("role", role);
        }
        if (userId != null) {
            claims.put("userId", userId);
        }
        claims.put("tokenType", tokenType);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractAllClaims(token).get("tokenType", String.class));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return !isRefreshToken(token) && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
