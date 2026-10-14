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

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        User user = userService.loginUser(request);
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }
}
