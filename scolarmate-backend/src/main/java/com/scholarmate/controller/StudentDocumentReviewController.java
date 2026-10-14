package com.scholarmate.controller;

import com.scholarmate.dto.DocumentTextRequest;
import com.scholarmate.dto.ProfileDocumentReviewResponse;
import com.scholarmate.dto.DocumentReviewHistoryItem;
import com.scholarmate.entity.DocumentReviewRecord;
import com.scholarmate.entity.User;
import com.scholarmate.repository.DocumentReviewRecordRepository;
import com.scholarmate.repository.UserRepository;
import com.scholarmate.service.StudentDocumentReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/profile-documents")
public class StudentDocumentReviewController {

    private final StudentDocumentReviewService reviewService;
    private final UserRepository userRepository;
    private final DocumentReviewRecordRepository reviewRecordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StudentDocumentReviewController(
            StudentDocumentReviewService reviewService,
            UserRepository userRepository,
            DocumentReviewRecordRepository reviewRecordRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
        this.reviewRecordRepository = reviewRecordRepository;
    }

    @PostMapping(value = "/{userId}/review/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDocumentReviewResponse reviewFile(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestPart("file") MultipartFile file) {
        verifyOwner(userId, currentUser);
        return saveReview(currentUser.getUsername(), reviewService.reviewFile(file));
    }

    @PostMapping(value = "/{userId}/review/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProfileDocumentReviewResponse reviewText(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody DocumentTextRequest request) {
        verifyOwner(userId, currentUser);
        return saveReview(currentUser.getUsername(), reviewService.reviewText(request.text()));
    }

    @PostMapping(value = "/me/review/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDocumentReviewResponse reviewMyFile(
            Authentication authentication,
            @RequestPart("file") MultipartFile file) {
        return saveReview(authentication.getName(), reviewService.reviewFile(file));
    }

    @PostMapping(value = "/me/review/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProfileDocumentReviewResponse reviewMyText(
            Authentication authentication,
            @Valid @RequestBody DocumentTextRequest request) {
        return saveReview(authentication.getName(), reviewService.reviewText(request.text()));
    }

    @GetMapping("/me/reviews")
    public java.util.List<DocumentReviewHistoryItem> getMyReviews(Authentication authentication) {
        return reviewRecordRepository.findByUserEmailOrderByCreatedAtDesc(authentication.getName())
                .stream()
                .map(record -> new DocumentReviewHistoryItem(
                        record.getId(), record.getInputMethod(), record.getCreatedAt(), record.getResponseJson()))
                .toList();
    }

    private ProfileDocumentReviewResponse saveReview(String email, ProfileDocumentReviewResponse response) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
        try {
            DocumentReviewRecord record = new DocumentReviewRecord();
            record.setUser(user);
            record.setInputMethod(response.inputMethod());
            record.setExtractedText(response.extractedText());
            record.setResponseJson(objectMapper.writeValueAsString(response));
            reviewRecordRepository.save(record);
            return response;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not store the document review response.", exception);
        }
    }

    private void verifyOwner(Long userId, UserDetails currentUser) {
        boolean ownsProfile = userRepository.findByEmail(currentUser.getUsername())
                .map(user -> user.getId().equals(userId))
                .orElse(false);
        if (!ownsProfile) {
            throw new AccessDeniedException("You can only review your own documents.");
        }
    }
}
