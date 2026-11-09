package com.scholarmate.form.model;

import jakarta.persistence.*;

@Entity
@Table(name = "master_fields")
public class MasterField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_key", nullable = false, unique = true)
    private String fieldKey;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String section;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @Column(name = "options_key")
    private String optionsKey;

    @Column(name = "default_required", nullable = false)
    private Boolean defaultRequired;

    @Column(name = "display_condition")
    private String displayCondition;

    @Column(name = "validation_rule")
    private String validationRule;

    @Column(name = "data_purpose")
    private String dataPurpose;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public String getOptionsKey() { return optionsKey; }
    public void setOptionsKey(String optionsKey) { this.optionsKey = optionsKey; }
    public Boolean getDefaultRequired() { return defaultRequired; }
    public void setDefaultRequired(Boolean defaultRequired) { this.defaultRequired = defaultRequired; }
    public String getDisplayCondition() { return displayCondition; }
    public void setDisplayCondition(String displayCondition) { this.displayCondition = displayCondition; }
    public String getValidationRule() { return validationRule; }
    public void setValidationRule(String validationRule) { this.validationRule = validationRule; }
    public String getDataPurpose() { return dataPurpose; }
    public void setDataPurpose(String dataPurpose) { this.dataPurpose = dataPurpose; }
}
