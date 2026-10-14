package com.scholarmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO returned after successful registration.
 * Provides enough data for the frontend to log the user in immediately.
 */
@Data
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
}

