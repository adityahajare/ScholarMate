package com.scholarmate.search.controller;

import com.scholarmate.entity.User;
import com.scholarmate.repository.UserRepository;
import com.scholarmate.search.dto.HybridSearchResponseDto;
import com.scholarmate.search.service.HybridSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scholarships")
public class HybridSearchController {

    private final HybridSearchService hybridSearchService;
    private final UserRepository userRepository;

    public HybridSearchController(HybridSearchService hybridSearchService, UserRepository userRepository) {
        this.hybridSearchService = hybridSearchService;
        this.userRepository = userRepository;
    }

    @GetMapping("/hybrid-search")
    public ResponseEntity<HybridSearchResponseDto> search(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            Authentication authentication) {

        User student = null;
        if (authentication != null && authentication.getName() != null) {
            student = userRepository.findByEmail(authentication.getName()).orElse(null);
        }

        HybridSearchResponseDto response = hybridSearchService.search(query, student, limit);
        return ResponseEntity.ok(response);
    }
}
