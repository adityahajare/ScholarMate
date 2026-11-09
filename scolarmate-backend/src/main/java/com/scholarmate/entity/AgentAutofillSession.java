package com.scholarmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_autofill_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentAutofillSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_id")
    private Scholarship scholarship;

    @Column(name = "portal_name", nullable = false, length = 120)
    private String portalName;

    @Column(name = "portal_url", length = 1024)
    private String portalUrl;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String status = "IN_PROGRESS";

    @Column(name = "total_fields", nullable = false)
    @Builder.Default
    private int totalFields = 0;

    @Column(name = "filled_fields", nullable = false)
    @Builder.Default
    private int filledFields = 0;

    @Column(name = "accepted_fields", nullable = false)
    @Builder.Default
    private int acceptedFields = 0;

    @Column(name = "rejected_fields", nullable = false)
    @Builder.Default
    private int rejectedFields = 0;

    @Column(name = "missing_fields", nullable = false)
    @Builder.Default
    private int missingFields = 0;

    @Column(name = "completion_percentage", nullable = false)
    @Builder.Default
    private int completionPercentage = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_states", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private String fieldStates = "{}";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null || status.isBlank()) {
            status = "IN_PROGRESS";
        }
        if (fieldStates == null || fieldStates.isBlank()) {
            fieldStates = "{}";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
