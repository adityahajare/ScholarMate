package com.scholarmate.service;

import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EligibilityCriteriaDefaultsService implements CommandLineRunner {

    private static final String UNKNOWN_TYPE = "UNKNOWN";
    private static final String UNKNOWN_EVIDENCE =
            "No eligibility criteria could be extracted. Verify the source before evaluating eligibility.";

    private final ScholarshipRepository scholarshipRepository;
    private final EligibilityCriteriaRepository criteriaRepository;

    public EligibilityCriteriaDefaultsService(
            ScholarshipRepository scholarshipRepository,
            EligibilityCriteriaRepository criteriaRepository) {
        this.scholarshipRepository = scholarshipRepository;
        this.criteriaRepository = criteriaRepository;
    }

    @Override
    public void run(String... args) {
        ensurePlaceholdersForMissingScholarships();
    }

    @Transactional
    public int ensurePlaceholdersForMissingScholarships() {
        Set<Long> scholarshipIdsWithCriteria = new HashSet<>();
        criteriaRepository.findAll().stream()
                .filter(criterion -> criterion.getScholarship() != null)
                .forEach(criterion -> scholarshipIdsWithCriteria.add(criterion.getScholarship().getId()));

        List<EligibilityCriteria> placeholders = new ArrayList<>();
        for (Scholarship scholarship : scholarshipRepository.findAll()) {
            if (scholarship.getId() != null && !scholarshipIdsWithCriteria.contains(scholarship.getId())) {
                placeholders.add(unknownPlaceholder(scholarship));
            }
        }

        criteriaRepository.saveAll(placeholders);
        return placeholders.size();
    }

    @Transactional
    public EligibilityCriteria ensurePlaceholder(Scholarship scholarship) {
        List<EligibilityCriteria> existing = criteriaRepository.findByScholarshipId(scholarship.getId());
        if (!existing.isEmpty()) {
            return existing.getFirst();
        }
        return criteriaRepository.save(unknownPlaceholder(scholarship));
    }

    @Transactional
    public void removePlaceholder(Long scholarshipId) {
        criteriaRepository.deleteByScholarshipIdAndCriterionTypeIgnoreCase(scholarshipId, UNKNOWN_TYPE);
    }

    private EligibilityCriteria unknownPlaceholder(Scholarship scholarship) {
        return EligibilityCriteria.builder()
                .scholarship(scholarship)
                .criterionType(UNKNOWN_TYPE)
                .operator(UNKNOWN_TYPE)
                .status(UNKNOWN_TYPE)
                .evidence(UNKNOWN_EVIDENCE)
                .build();
    }
}