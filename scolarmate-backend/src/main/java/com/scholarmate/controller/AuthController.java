package com.scholarmate.controller;

import com.scholarmate.dto.RegisterRequest;
import com.scholarmate.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.scholarmate.dto.LoginRequest;
import com.scholarmate.dto.AuthResponse;
import com.scholarmate.dto.RegisterResponse;
import com.scholarmate.entity.User;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final com.scholarmate.config.JwtService jwtService;

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
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        User user = userService.loginUser(request);
        String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), token));
    }
}
