package com.scholarmate.form.repository;

import com.scholarmate.form.model.ScholarshipRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScholarshipRequirementRepository extends JpaRepository<ScholarshipRequirement, Long> {
    List<ScholarshipRequirement> findAllByScholarshipKey(String scholarshipKey);
    List<ScholarshipRequirement> findAllByScholarshipKeyIn(List<String> scholarshipKeys);
}
