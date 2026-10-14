package com.scholarmate.controller;

import com.scholarmate.entity.RequiredDocument;
import com.scholarmate.service.RequiredDocumentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class RequiredDocumentController {

    private final RequiredDocumentService service;

    public RequiredDocumentController(
            RequiredDocumentService service) {
        this.service = service;
    }

    @PostMapping
    public RequiredDocument addDocument(
            @RequestBody RequiredDocument document) {

        return service.addDocument(document);
    }

    @GetMapping("/scholarship/{scholarshipId}")
    public List<RequiredDocument> getByScholarship(
            @PathVariable Long scholarshipId) {

        return service.getByScholarship(scholarshipId);
    }

    @GetMapping("/{id}")
    public RequiredDocument getById(
            @PathVariable Long id) {

        return service.getById(id);
    }

    @PutMapping("/{id}")
    public RequiredDocument updateDocument(
            @PathVariable Long id,
            @RequestBody RequiredDocument document) {

        return service.updateDocument(id, document);
    }

    @DeleteMapping("/{id}")
    public String deleteDocument(
            @PathVariable Long id) {

        service.deleteDocument(id);

        return "Required document deleted successfully";
    }
}