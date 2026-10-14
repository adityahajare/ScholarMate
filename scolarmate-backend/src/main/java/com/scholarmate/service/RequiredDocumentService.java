package com.scholarmate.service;

import com.scholarmate.entity.RequiredDocument;
import com.scholarmate.repository.RequiredDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequiredDocumentService {

    private final RequiredDocumentRepository repository;

    public RequiredDocumentService(
            RequiredDocumentRepository repository) {
        this.repository = repository;
    }

    public RequiredDocument addDocument(
            RequiredDocument document) {

        return repository.save(document);
    }

    public List<RequiredDocument> getByScholarship(
            Long scholarshipId) {

        return repository.findByScholarshipId(scholarshipId);
    }

    public RequiredDocument getById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Required document not found"));
    }

    public RequiredDocument updateDocument(
            Long id,
            RequiredDocument updated) {

        RequiredDocument existing = getById(id);

        existing.setDocumentType(
                updated.getDocumentType());

        existing.setRequirementType(
                updated.getRequirementType());

        existing.setCondition(
                updated.getCondition());

        existing.setEvidence(
                updated.getEvidence());

        return repository.save(existing);
    }

    public void deleteDocument(Long id) {

        if (!repository.existsById(id)) {
            throw new RuntimeException(
                    "Required document not found");
        }

        repository.deleteById(id);
    }
}