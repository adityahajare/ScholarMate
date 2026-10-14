package com.scholarmate.repository;

import com.scholarmate.entity.RequiredDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequiredDocumentRepository
        extends JpaRepository<RequiredDocument, Long> {

    List<RequiredDocument>
    findByScholarshipId(Long scholarshipId);
}