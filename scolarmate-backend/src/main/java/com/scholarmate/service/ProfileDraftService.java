package com.scholarmate.service;

import com.scholarmate.dto.ProfileDraftRequest;
import com.scholarmate.dto.ProfileDraftResponse;
import com.scholarmate.entity.ProfileDraft;
import com.scholarmate.entity.User;
import com.scholarmate.repository.ProfileDraftRepository;
import com.scholarmate.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Service layer for handling profile draft CRUD operations.
 *
 * The draft JSON structure is free‑form – the front‑end serialises each step
 * into a map and stores it under {@code data}. This service merely persists the
 * map and provides a convenient conversion to the response DTO.
 */
@Service
public class ProfileDraftService {

    private final ProfileDraftRepository draftRepo;
    private final UserRepository userRepo;

    public ProfileDraftService(ProfileDraftRepository draftRepo, UserRepository userRepo) {
        this.draftRepo = draftRepo;
        this.userRepo = userRepo;
    }

    /**
     * Retrieves the current draft for the authenticated user. If none exists,
     * a new empty draft is created and returned.
     */
    public ProfileDraftResponse getOrCreateDraft(String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ProfileDraft draft = draftRepo.findByUser(user)
                .orElseGet(() -> createEmptyDraft(user));
        return toResponse(draft);
    }

    /**
     * Persists the supplied draft data. If a draft already exists for the user it
     * is updated; otherwise a new draft is created.
     */
    @Transactional
    public ProfileDraftResponse saveDraft(String userEmail, ProfileDraftRequest request) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ProfileDraft draft = draftRepo.findByUser(user).orElseGet(() -> createEmptyDraft(user));
        draft.setData(request.getData() != null ? request.getData() : new HashMap<>());
        draft.setUpdatedAt(LocalDateTime.now());
        draftRepo.save(draft);
        return toResponse(draft);
    }

    private ProfileDraft createEmptyDraft(User user) {
        ProfileDraft draft = new ProfileDraft();
        draft.setUser(user);
        draft.setData(new HashMap<>());
        draft.setCreatedAt(LocalDateTime.now());
        draft.setUpdatedAt(LocalDateTime.now());
        return draftRepo.save(draft);
    }

    private ProfileDraftResponse toResponse(ProfileDraft draft) {
        ProfileDraftResponse resp = new ProfileDraftResponse();
        resp.setId(draft.getId());
        resp.setUserId(draft.getUser().getId());
        resp.setData(draft.getData());
        resp.setCreatedAt(draft.getCreatedAt());
        resp.setUpdatedAt(draft.getUpdatedAt());
        return resp;
    }
}
