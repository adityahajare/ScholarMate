package com.scholarmate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scholarship_evidence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScholarshipEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String evidenceType;

    @Column(length = 2000)
    private String sourceUrl;

    @Column(length = 1000)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    private String extractionMethod;

    private Double confidence;

    private LocalDateTime retrievedAt;

    private String contentHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_id")
    private Scholarship scholarship;
}