package com.scholarmate.repository;

import com.scholarmate.entity.EligibilityCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EligibilityCriteriaRepository
        extends JpaRepository<EligibilityCriteria, Long> {

    List<EligibilityCriteria>
    findByScholarshipId(Long scholarshipId);

        @Query("select criteria from EligibilityCriteria criteria where criteria.scholarship.id in :scholarshipIds")
        List<EligibilityCriteria> findByScholarshipIdIn(@Param("scholarshipIds") Collection<Long> scholarshipIds);

    void deleteByScholarshipIdAndCriterionTypeIgnoreCase(
            Long scholarshipId,
            String criterionType);
}