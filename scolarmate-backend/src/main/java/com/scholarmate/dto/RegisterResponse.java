package com.scholarmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returned after successful registration.
 * Provides enough data for the frontend to log the user in immediately.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String token;

    public RegisterResponse(Long id, String name, String email, String role) {
        this(id, name, email, role, null);
    }
}

