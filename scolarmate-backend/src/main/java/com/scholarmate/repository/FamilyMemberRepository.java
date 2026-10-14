package com.scholarmate.repository;

import com.scholarmate.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    List<FamilyMember> findByUserProfileIdOrderByIdAsc(Long profileId);
    Optional<FamilyMember> findByIdAndUserProfileId(Long id, Long profileId);
}
