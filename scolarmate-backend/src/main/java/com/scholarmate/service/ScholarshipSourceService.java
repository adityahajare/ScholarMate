package com.scholarmate.service;

import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.repository.ScholarshipSourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScholarshipSourceService {

    private final ScholarshipSourceRepository repository;

    public ScholarshipSourceService(ScholarshipSourceRepository repository) {
        this.repository = repository;
    }

    public ScholarshipSource addSource(ScholarshipSource source) {

        if (repository.findByName(source.getName()).isPresent()) {
            throw new RuntimeException("Source already exists");
        }

        return repository.save(source);
    }

    public List<ScholarshipSource> getAllSources() {
        return repository.findAll();
    }

    public ScholarshipSource getSourceById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Source not found"));
    }
}