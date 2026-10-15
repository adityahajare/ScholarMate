package com.scholarmate.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 32)
    private String mobile;

    @Column(length = 64)
    private String educationType;

    @Column(length = 64)
    private String educationLevel;

    @Column(length = 128)
    private String course;

    @Column(length = 64)
    private String state;

    @Column(length = 128)
    private String guardianName;

    @Column(length = 32)
    private String guardianPhone;

    private Boolean isUnder18 = false;

    private Boolean termsAccepted = false;

    private LocalDateTime termsAcceptedAt;

    @Column(length = 128)
    @JsonIgnore
    private String resetToken;

    @JsonIgnore
    private LocalDateTime resetTokenExpiry;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
