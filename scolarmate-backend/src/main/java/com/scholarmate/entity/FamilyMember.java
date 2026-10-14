package com.scholarmate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profile_family_members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FamilyMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    @JsonIgnore
    private UserProfile userProfile;
    @Column(nullable = false, length = 80) private String relationship;
    @Column(nullable = false, length = 200) private String name;
    @Column(length = 120) private String occupation;
    private Double annualIncome;
    private Boolean dependent;
    private Integer beneficiaryCount;
}
