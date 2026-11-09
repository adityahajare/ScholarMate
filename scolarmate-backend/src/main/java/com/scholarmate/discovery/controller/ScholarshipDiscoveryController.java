package com.scholarmate.discovery.controller;

import com.scholarmate.discovery.dto.*;
import com.scholarmate.discovery.service.AdaptiveDiscoveryService;
import com.scholarmate.entity.User;
import com.scholarmate.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scholarship-discovery")
@RequiredArgsConstructor
@CrossOrigin
public class ScholarshipDiscoveryController {

    private final AdaptiveDiscoveryService discoveryService;
    private final UserRepository userRepo;

    @PostMapping("/sessions")
    public ResponseEntity<DiscoverySessionResponseDto> startDiscoverySession(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(discoveryService.startOrResumeSession(user));
    }

    @GetMapping("/sessions/{id}/questions")
    public ResponseEntity<DiscoverySessionResponseDto> getNextQuestions(
            @PathVariable Long id,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(discoveryService.getSessionQuestions(id, user));
    }

    @PostMapping("/sessions/{id}/answers")
    public ResponseEntity<DiscoverySessionResponseDto> submitAnswers(
            @PathVariable Long id,
            @Valid @RequestBody DiscoveryAnswersRequestDto request,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(discoveryService.submitAnswers(id, user, request.getAnswers()));
    }

    @GetMapping("/sessions/{id}/matches")
    public ResponseEntity<List<ScholarshipMatchResponseDto>> getCandidateMatches(
            @PathVariable Long id,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(discoveryService.getMatches(id, user));
    }

    @GetMapping("/sessions/{id}/progress")
    public ResponseEntity<DiscoveryProgressDto> getDiscoveryProgress(
            @PathVariable Long id,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(discoveryService.getProgress(id, user));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Authentication required to access scholarship discovery.");
        }
        return userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authentication.getName()));
    }
}
