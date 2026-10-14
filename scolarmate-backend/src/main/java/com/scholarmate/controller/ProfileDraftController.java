package com.scholarmate.controller;

import com.scholarmate.dto.ProfileDraftRequest;
import com.scholarmate.dto.ProfileDraftResponse;
import com.scholarmate.service.ProfileDraftService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/draft")
public class ProfileDraftController {

    private final ProfileDraftService profileDraftService;

    public ProfileDraftController(ProfileDraftService profileDraftService) {
        this.profileDraftService = profileDraftService;
    }

    /**
     * Retrieve the current user's draft. If none exists, a new draft is created automatically.
     */
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ProfileDraftResponse> getDraft(Authentication authentication) {
        String email = authentication.getName();
        ProfileDraftResponse response = profileDraftService.getOrCreateDraft(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a draft explicitly (rarely used because GET creates on‑fly).
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ProfileDraftResponse> createDraft(@RequestBody ProfileDraftRequest request,
                                                             Authentication authentication) {
        String email = authentication.getName();
        ProfileDraftResponse response = profileDraftService.saveDraft(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update the existing draft with partial data from the front‑end.
     */
    @PutMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ProfileDraftResponse> updateDraft(@RequestBody ProfileDraftRequest request,
                                                             Authentication authentication) {
        String email = authentication.getName();
        ProfileDraftResponse response = profileDraftService.saveDraft(email, request);
        return ResponseEntity.ok(response);
    }
}
