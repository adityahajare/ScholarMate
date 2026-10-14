package com.scholarmate.repository;

import com.scholarmate.entity.DocumentReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentReviewRecordRepository extends JpaRepository<DocumentReviewRecord, Long> {
    List<DocumentReviewRecord> findByUserEmailOrderByCreatedAtDesc(String email);
}
