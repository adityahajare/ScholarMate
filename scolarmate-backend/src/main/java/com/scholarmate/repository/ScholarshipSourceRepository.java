package com.scholarmate.repository;

import com.scholarmate.entity.ScholarshipSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ScholarshipSourceRepository
        extends JpaRepository<ScholarshipSource, Long> {

    Optional<ScholarshipSource> findByName(String name);

    List<ScholarshipSource> findByActiveTrueAndScrapingAllowedTrue();

    List<ScholarshipSource> findByActiveTrueAndScrapingAllowedTrueAndLastSuccessfulScrapeAtNotNull();
}
