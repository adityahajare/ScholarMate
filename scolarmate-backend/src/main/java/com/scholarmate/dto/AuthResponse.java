package com.scholarmate.dto;

public record AuthResponse(Long id, String name, String email, String role, String token) {
    public AuthResponse(Long id, String name, String email, String role) {
        this(id, name, email, role, null);
    }
}
