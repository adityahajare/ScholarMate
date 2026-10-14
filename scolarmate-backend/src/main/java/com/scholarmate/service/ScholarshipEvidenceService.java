package com.scholarmate.service;

import com.scholarmate.entity.ScholarshipEvidence;
import com.scholarmate.repository.ScholarshipEvidenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScholarshipEvidenceService {

    private final ScholarshipEvidenceRepository repository;

    public ScholarshipEvidenceService(
            ScholarshipEvidenceRepository repository) {

        this.repository = repository;
    }

    public ScholarshipEvidence save(
            ScholarshipEvidence evidence) {

        return repository.save(evidence);
    }

    public List<ScholarshipEvidence> getByScholarship(
            Long scholarshipId) {

        return repository.findByScholarshipId(
                scholarshipId
        );
    }

    public ScholarshipEvidence getById(
            Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Scholarship evidence not found"
                        ));
    }

    public void deleteEvidence(
            Long id) {

        if (!repository.existsById(id)) {

            throw new RuntimeException(
                    "Scholarship evidence not found"
            );
        }

        repository.deleteById(id);
    }
}