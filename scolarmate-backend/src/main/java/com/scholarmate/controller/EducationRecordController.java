package com.scholarmate.controller;

import com.scholarmate.entity.EducationRecord;
import com.scholarmate.service.EducationRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile/education")
public class EducationRecordController {
    private final EducationRecordService service;

    public EducationRecordController(EducationRecordService service) {
        this.service = service;
    }

    @GetMapping
    public List<EducationRecord> list(Authentication authentication) {
        return service.listMine(authentication.getName());
    }

    @PostMapping
    public ResponseEntity<EducationRecord> create(Authentication authentication, @RequestBody EducationRecord record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMine(authentication.getName(), record));
    }

    @PutMapping("/{id}")
    public EducationRecord update(Authentication authentication, @PathVariable Long id, @RequestBody EducationRecord record) {
        return service.updateMine(authentication.getName(), id, record);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        service.deleteMine(authentication.getName(), id);
    }
}
