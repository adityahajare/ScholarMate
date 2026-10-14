package com.scholarmate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "eligibility_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String criterionType;

    private String operator;

    @Column(length = 1000)
    private String value;

    @Column(length = 1000)
    private String value2;

    private String status;

    @Column(length = 2000)
    private String evidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_id")
    private Scholarship scholarship;
}