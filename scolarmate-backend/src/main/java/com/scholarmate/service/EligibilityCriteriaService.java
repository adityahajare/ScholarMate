package com.scholarmate.service;

import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EligibilityCriteriaService {

    private final EligibilityCriteriaRepository repository;

    public EligibilityCriteriaService(
            EligibilityCriteriaRepository repository) {
        this.repository = repository;
    }

    public EligibilityCriteria addCriteria(
            EligibilityCriteria criteria) {

        return repository.save(criteria);
    }

    public List<EligibilityCriteria> getByScholarship(
            Long scholarshipId) {

        return repository.findByScholarshipId(scholarshipId);
    }

    public EligibilityCriteria getById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Eligibility criteria not found"));
    }

    public EligibilityCriteria updateCriteria(
            Long id,
            EligibilityCriteria updated) {

        EligibilityCriteria existing =
                getById(id);

        existing.setCriterionType(
                updated.getCriterionType());

        existing.setOperator(
                updated.getOperator());

        existing.setValue(
                updated.getValue());

        existing.setValue2(
                updated.getValue2());

        existing.setStatus(
                updated.getStatus());

        existing.setEvidence(
                updated.getEvidence());

        return repository.save(existing);
    }

    public void deleteCriteria(Long id) {

        if (!repository.existsById(id)) {
            throw new RuntimeException(
                    "Eligibility criteria not found");
        }

        repository.deleteById(id);
    }
}