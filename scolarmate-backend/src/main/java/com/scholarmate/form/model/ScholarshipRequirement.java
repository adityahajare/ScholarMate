package com.scholarmate.form.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "scholarship_requirements")
public class ScholarshipRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scholarship_key", nullable = false)
    private String scholarshipKey;

    @Column(name = "scholarship_name", nullable = false)
    private String scholarshipName;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "condition_key", nullable = false)
    private String conditionKey;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(nullable = false, length = 50)
    private String operator;

    @Column(name = "expected_value", length = 500)
    private String expectedValue;

    @Column(nullable = false)
    private Boolean mandatory = true;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "source_document")
    private String sourceDocument;

    @Column(name = "source_section")
    private String sourceSection;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus = "VERIFIED";

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_until")
    private LocalDate effectiveUntil;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getScholarshipKey() { return scholarshipKey; }
    public void setScholarshipKey(String scholarshipKey) { this.scholarshipKey = scholarshipKey; }

    public String getScholarshipName() { return scholarshipName; }
    public void setScholarshipName(String scholarshipName) { this.scholarshipName = scholarshipName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getConditionKey() { return conditionKey; }
    public void setConditionKey(String conditionKey) { this.conditionKey = conditionKey; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getSourceDocument() { return sourceDocument; }
    public void setSourceDocument(String sourceDocument) { this.sourceDocument = sourceDocument; }

    public String getSourceSection() { return sourceSection; }
    public void setSourceSection(String sourceSection) { this.sourceSection = sourceSection; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveUntil() { return effectiveUntil; }
    public void setEffectiveUntil(LocalDate effectiveUntil) { this.effectiveUntil = effectiveUntil; }
}
