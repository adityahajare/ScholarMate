package com.scholarmate.repository;

import com.scholarmate.entity.EducationRecord;
import com.scholarmate.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EducationRecordRepository extends JpaRepository<EducationRecord, Long> {
    List<EducationRecord> findByUserProfileOrderBySortOrderAscIdAsc(UserProfile userProfile);
    Optional<EducationRecord> findByIdAndUserProfile(Long id, UserProfile userProfile);
}
