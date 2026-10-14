package com.scholarmate.controller;

import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.service.ScholarshipSourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
public class ScholarshipSourceController {

    private final ScholarshipSourceService sourceService;

    public ScholarshipSourceController(
            ScholarshipSourceService sourceService) {
        this.sourceService = sourceService;
    }

    @PostMapping
    public ScholarshipSource addSource(
            @RequestBody ScholarshipSource source) {

        return sourceService.addSource(source);
    }

    @GetMapping
    public List<ScholarshipSource> getAllSources() {
        return sourceService.getAllSources();
    }

    @GetMapping("/{id}")
    public ScholarshipSource getSourceById(
            @PathVariable Long id) {

        return sourceService.getSourceById(id);
    }
}