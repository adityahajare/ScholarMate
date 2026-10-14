package com.scholarmate.controller;

import com.scholarmate.entity.EligibilityCriteria;
import com.scholarmate.service.EligibilityCriteriaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eligibility")
public class EligibilityCriteriaController {

    private final EligibilityCriteriaService service;

    public EligibilityCriteriaController(
            EligibilityCriteriaService service) {
        this.service = service;
    }

    @PostMapping
    public EligibilityCriteria addCriteria(
            @RequestBody EligibilityCriteria criteria) {

        return service.addCriteria(criteria);
    }

    @GetMapping("/scholarship/{scholarshipId}")
    public List<EligibilityCriteria> getByScholarship(
            @PathVariable Long scholarshipId) {

        return service.getByScholarship(scholarshipId);
    }

    @GetMapping("/{id}")
    public EligibilityCriteria getById(
            @PathVariable Long id) {

        return service.getById(id);
    }

    @PutMapping("/{id}")
    public EligibilityCriteria updateCriteria(
            @PathVariable Long id,
            @RequestBody EligibilityCriteria criteria) {

        return service.updateCriteria(id, criteria);
    }

    @DeleteMapping("/{id}")
    public String deleteCriteria(
            @PathVariable Long id) {

        service.deleteCriteria(id);

        return "Eligibility criteria deleted successfully";
    }
}