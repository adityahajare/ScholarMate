package com.scholarmate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profile_addresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileAddress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    @JsonIgnore
    private UserProfile userProfile;
    @Column(nullable = false, length = 30) private String addressType;
    @Column(length = 300) private String line1;
    @Column(length = 300) private String line2;
    @Column(length = 100) private String state;
    @Column(length = 100) private String district;
    @Column(length = 100) private String taluka;
    @Column(length = 120) private String village;
    @Column(length = 12) private String pinCode;
    private Boolean domicile;
}
