package com.scholarmate.controller;

import com.scholarmate.entity.Scholarship;
import com.scholarmate.dto.ScholarshipCatalogStatus;
import com.scholarmate.dto.ScholarshipDataQualityReport;
import com.scholarmate.service.ScholarshipDataQualityService;
import com.scholarmate.service.ScholarshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/scholarships")
public class ScholarshipController {

    private final ScholarshipService scholarshipService;
    private final ScholarshipDataQualityService dataQualityService;

    public ScholarshipController(
            ScholarshipService scholarshipService,
            ScholarshipDataQualityService dataQualityService) {
        this.scholarshipService = scholarshipService;
        this.dataQualityService = dataQualityService;
    }

    @GetMapping("/data-quality")
    public ResponseEntity<ScholarshipDataQualityReport> getDataQualityReport() {
        return ResponseEntity.ok(dataQualityService.createReport());
    }

    @GetMapping("/catalog-status")
    public ResponseEntity<ScholarshipCatalogStatus> getCatalogStatus() {
        return ResponseEntity.ok(scholarshipService.getCatalogStatus());
    }

    @PostMapping
    public ResponseEntity<Scholarship> createScholarship(
            @RequestBody Scholarship scholarship) {

        return ResponseEntity.ok(
                scholarshipService.createScholarship(scholarship)
        );
    }

    @GetMapping
    public ResponseEntity<List<Scholarship>> getAllScholarships() {

        return ResponseEntity.ok(
                scholarshipService.getAllScholarships()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Scholarship>> searchScholarships(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        PageRequest request = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(scholarshipService.searchActiveScholarships(q, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Scholarship> getScholarship(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                scholarshipService.getScholarshipById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Scholarship> updateScholarship(
            @PathVariable Long id,
            @RequestBody Scholarship scholarship) {

        return ResponseEntity.ok(
                scholarshipService.updateScholarship(id, scholarship)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteScholarship(
            @PathVariable Long id) {

        scholarshipService.deleteScholarship(id);

        return ResponseEntity.ok(
                "Scholarship deleted successfully"
        );
    }
}
