package com.scholarmate.controller;

import com.scholarmate.dto.*;
import com.scholarmate.entity.User;
import com.scholarmate.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final com.scholarmate.config.JwtService jwtService;

    @Autowired
    public AuthController(UserService userService, com.scholarmate.config.JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public AuthController(UserService userService) {
        this(userService, new com.scholarmate.config.JwtService("scholarmate-super-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256-security", 86400000));
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.loginUser(request);
        String token = jwtService.generateAccessToken(user.getEmail(), user.getRole(), user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId());
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), token, refreshToken, 900000));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        if (!jwtService.isRefreshToken(request.refreshToken()) || jwtService.isTokenExpired(request.refreshToken())) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        String email = jwtService.extractUsername(request.refreshToken());
        User user = userService.findByEmail(email);
        String token = jwtService.generateAccessToken(user.getEmail(), user.getRole(), user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId());
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), token, refreshToken, 900000));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String message = userService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = userService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
