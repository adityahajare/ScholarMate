package com.scholarmate.repository;

import com.scholarmate.entity.RecommendationFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {

    Page<RecommendationFeedback> findByStudentIdOrderByEventTimestampDesc(Long studentId, Pageable pageable);

    List<RecommendationFeedback> findByProcessedForTrainingFalse(Pageable pageable);

    long countByProcessedForTrainingFalse();

    @Modifying
    @Query("UPDATE RecommendationFeedback rf SET rf.processedForTraining = true WHERE rf.id IN :ids")
    int markProcessedForTraining(@Param("ids") List<Long> ids);
}
