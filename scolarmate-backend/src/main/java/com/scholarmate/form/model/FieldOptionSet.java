package com.scholarmate.form.model;

import jakarta.persistence.*;

@Entity
@Table(name = "field_option_sets")
public class FieldOptionSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_key", nullable = false, unique = true)
    private String optionKey;

    @Column(nullable = false)
    private String label;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOptionKey() { return optionKey; }
    public void setOptionKey(String optionKey) { this.optionKey = optionKey; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
