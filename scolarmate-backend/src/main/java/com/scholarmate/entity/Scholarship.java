package com.scholarmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "scholarships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scholarship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   @ManyToOne(fetch=FetchType.LAZY)
   @JoinColumn(name="source_id")
   private ScholarshipSource source;
    private String name;

    @Column(length = 2000)
    private String description;

    private String provider;

    @Column(length = 2048)
    private String specificationUrl;

    @Column(length = 2048)
    private String faqUrl;

    private LocalDate applicationStartDate;

    @Column(length = 2048)
    private String sourceUrl;

    @Column(length = 2048)
    private String applicationUrl;

    private Double amount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String dynamicMetadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ml_features", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, FeatureStatusValue> features = new HashMap<>();

    private String category;

    private String state;

    private String course;

    private Double minimumCgpa;

    private Double maximumFamilyIncome;

    private String eligibleCategories;

    private Integer minimumYearOfStudy;

    private Integer maximumYearOfStudy;

    private LocalDate applicationDeadline;

    private Boolean active;
}