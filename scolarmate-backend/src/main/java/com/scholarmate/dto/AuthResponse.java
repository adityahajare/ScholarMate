package com.scholarmate.dto;

public record AuthResponse(Long id, String name, String email, String role, String token, String refreshToken, long expiresIn) {
    public AuthResponse(Long id, String name, String email, String role) {
        this(id, name, email, role, null, null, 0);
    }

    public AuthResponse(Long id, String name, String email, String role, String token) {
        this(id, name, email, role, token, null, 0);
    }
}
