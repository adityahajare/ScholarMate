package com.scholarmate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_review_records")
@Getter
@Setter
@NoArgsConstructor
public class DocumentReviewRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false) private String inputMethod;
    @Column(nullable = false, length = 100000) private String extractedText;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false, columnDefinition = "jsonb") private String responseJson;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void setCreatedAt() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
