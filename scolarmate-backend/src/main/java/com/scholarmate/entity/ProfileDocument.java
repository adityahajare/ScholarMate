package com.scholarmate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    @JsonIgnore
    private UserProfile userProfile;
    @Column(nullable = false, length = 120) private String documentType;
    @Column(nullable = false, length = 255) private String fileName;
    @Column(length = 120) private String contentType;
    private Long sizeBytes;
    @JsonIgnore @Column(length = 500) private String storageKey;
    @Column(nullable = false, length = 30) @Builder.Default private String status = "UPLOADED";
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private String extractedData;
    private LocalDate expiresAt;
    private LocalDateTime uploadedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime updatedAt;
    @PrePersist void onCreate() { if (uploadedAt == null) uploadedAt = LocalDateTime.now(); updatedAt = uploadedAt; }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
