package com.scholarmate.repository;

import com.scholarmate.entity.ScholarshipEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScholarshipEvidenceRepository
        extends JpaRepository<ScholarshipEvidence, Long> {

    List<ScholarshipEvidence> findByScholarshipId(Long scholarshipId);

    List<ScholarshipEvidence> findByContentHash(String contentHash);
}