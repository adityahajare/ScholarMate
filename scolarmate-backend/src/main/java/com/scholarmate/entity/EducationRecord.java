package com.scholarmate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One row per qualification a student has or is currently pursuing.
 *
 * Covers profile sections C (Class 10), D (Class 12 / qualifying exam),
 * E (other past qualifications), F (current course), G (admission details)
 * and K (marks and academic progress for the current course).
 *
 * Past and current records share this table; fields not applicable to a
 * given level stay null rather than being guessed.
 */
@Entity
@Table(name = "education_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    @JsonIgnore
    private UserProfile userProfile;

    /** CLASS_10, CLASS_12, DIPLOMA, ITI_VOCATIONAL, UG, PG, PHD_RESEARCH, PROFESSIONAL, OTHER */
    @Column(nullable = false, length = 40)
    private String level;

    /** Board for school levels, university for degree levels. */
    @Column(length = 200)
    private String boardOrUniversity;

    @Column(length = 200)
    private String stream;

    @Column(length = 300)
    private String institution;

    /** Class 11-12 qualifying examination name, e.g. "HSC", "CBSE AISSCE". */
    @Column(length = 200)
    private String qualifyingExamination;

    private Integer passingYear;

    private Double obtainedMarks;

    private Double maximumMarks;

    /** Stored only when it is derivable from obtained/maximum marks. */
    private Double percentage;

    /** PASS, FAIL, APPEARING, WITHHELD */
    @Column(length = 40)
    private String result;

    private Integer attempts;

    private Integer gapYears;

    /** COMPLETED, ONGOING, NOT_COMPLETED */
    @Column(length = 40)
    private String completionStatus;

    private LocalDate startDate;

    private LocalDate endDate;

    /* ---- Section F: current course ---- */

    private Integer currentSemester;

    private Integer courseDurationYears;

    private LocalDate admissionDate;

    /** Institution accreditation/approval reference, e.g. AICTE, NAAC grade. */
    @Column(length = 300)
    private String accreditation;

    /* ---- Section G: admission details ---- */

    /** CAP, CENTRALIZED, INSTITUTE_LEVEL, MANAGEMENT, OTHER */
    @Column(length = 60)
    private String admissionType;

    @Column(length = 60)
    private String admissionRound;

    @Column(length = 120)
    private String allotmentNumber;

    /** Admission/seat category, which may differ from social category. */
    @Column(length = 120)
    private String seatCategory;

    private Boolean admissionConfirmed;

    /* ---- Section K: marks and academic progress ---- */

    private Integer backlogs;

    private Double attendancePercent;

    @Column(length = 60)
    private String promotionStatus;

    @Column(length = 300)
    private String renewalPerformance;

    /* ---- Bookkeeping ---- */

    /** True for the qualification the student is currently enrolled in. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean currentRecord = false;

    @Builder.Default
    private Integer sortOrder = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Percentage is only ever computed from real marks, never invented. */
    @Transient
    public Double computePercentage() {
        if (obtainedMarks == null || maximumMarks == null || maximumMarks <= 0) {
            return null;
        }
        return Math.round((obtainedMarks / maximumMarks) * 10000.0) / 100.0;
    }
}
