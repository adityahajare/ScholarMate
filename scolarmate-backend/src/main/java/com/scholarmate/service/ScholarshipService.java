package com.scholarmate.service;

import com.scholarmate.entity.Scholarship;
import com.scholarmate.dto.ScholarshipCatalogStatus;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.ScholarshipSourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipSourceRepository sourceRepository;
    private final EligibilityCriteriaDefaultsService criteriaDefaultsService;

    public ScholarshipService(
            ScholarshipRepository scholarshipRepository,
            ScholarshipSourceRepository sourceRepository,
            EligibilityCriteriaDefaultsService criteriaDefaultsService) {
        this.scholarshipRepository = scholarshipRepository;
        this.sourceRepository = sourceRepository;
        this.criteriaDefaultsService = criteriaDefaultsService;
    }

    public Scholarship createScholarship(Scholarship scholarship) {
        Scholarship saved = scholarshipRepository.save(scholarship);
        criteriaDefaultsService.ensurePlaceholder(saved);
        return saved;
    }

    public List<Scholarship> getAllScholarships() {
        return scholarshipRepository.findByActiveTrue(Pageable.ofSize(50)).getContent();
    }

    public Page<Scholarship> searchActiveScholarships(String query, Pageable pageable) {
        return scholarshipRepository.searchActive(query == null ? "" : query.trim(), pageable);
    }

    public ScholarshipCatalogStatus getCatalogStatus() {
        List<com.scholarmate.entity.ScholarshipSource> sources =
                sourceRepository.findByActiveTrueAndScrapingAllowedTrueAndLastSuccessfulScrapeAtNotNull();
        LocalDateTime lastRefresh = sources.stream()
                .map(source -> source.getLastSuccessfulScrapeAt())
                .max((left, right) -> left.compareTo(right))
                .orElse(null);
        boolean stale = lastRefresh == null || lastRefresh.plusHours(5).isBefore(LocalDateTime.now());
        return new ScholarshipCatalogStatus(
                scholarshipRepository.countByActiveTrue(),
                sourceRepository.findByActiveTrueAndScrapingAllowedTrue().size(),
                lastRefresh,
                stale);
    }

    public Scholarship getScholarshipById(Long id) {
        return scholarshipRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Scholarship not found"));
    }

    public Scholarship updateScholarship(
            Long id,
            Scholarship updatedScholarship) {

        Scholarship existing = getScholarshipById(id);

        existing.setName(updatedScholarship.getName());
        existing.setDescription(updatedScholarship.getDescription());
        existing.setProvider(updatedScholarship.getProvider());
        existing.setSourceUrl(updatedScholarship.getSourceUrl());
        existing.setApplicationUrl(updatedScholarship.getApplicationUrl());
        existing.setAmount(updatedScholarship.getAmount());
        existing.setCategory(updatedScholarship.getCategory());
        existing.setState(updatedScholarship.getState());
        existing.setCourse(updatedScholarship.getCourse());
        existing.setMinimumCgpa(updatedScholarship.getMinimumCgpa());
        existing.setMaximumFamilyIncome(
                updatedScholarship.getMaximumFamilyIncome()
        );
        existing.setEligibleCategories(
                updatedScholarship.getEligibleCategories()
        );
        existing.setMinimumYearOfStudy(
                updatedScholarship.getMinimumYearOfStudy()
        );
        existing.setMaximumYearOfStudy(
                updatedScholarship.getMaximumYearOfStudy()
        );
        existing.setApplicationDeadline(
                updatedScholarship.getApplicationDeadline()
        );
        existing.setActive(updatedScholarship.getActive());

        return scholarshipRepository.save(existing);
    }

    public void deleteScholarship(Long id) {
        Scholarship scholarship = getScholarshipById(id);
        scholarshipRepository.delete(scholarship);
    }
}
