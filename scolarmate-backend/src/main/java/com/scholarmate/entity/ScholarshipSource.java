package com.scholarmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "scholarship_sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScholarshipSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 1000)
    private String baseUrl;

    private String sourceType;

    private String scraperType;
    private String scraperKey;

    private Boolean scrapingAllowed;

    private Boolean active;

    private String crawlFrequency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String dynamicMetadata;

    private LocalDateTime lastScrapedAt;

    private LocalDateTime lastSuccessfulScrapeAt;

    @Column(length = 64)
    private String contentHash;
}