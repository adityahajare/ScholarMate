package com.scholarmate.dto;

/**
 * DTO returned after successful registration.
 * Provides enough data for the frontend to log the user in immediately.
 */
public class RegisterResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String token;
    private String refreshToken;
    private long expiresIn;

    public RegisterResponse() {
    }

    public RegisterResponse(Long id, String name, String email, String role, String token, String refreshToken, long expiresIn) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public RegisterResponse(Long id, String name, String email, String role) {
        this(id, name, email, role, null, null, 0);
    }

    public RegisterResponse(Long id, String name, String email, String role, String token) {
        this(id, name, email, role, token, null, 0);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
