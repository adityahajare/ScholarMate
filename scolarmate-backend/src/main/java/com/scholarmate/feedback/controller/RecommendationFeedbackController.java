package com.scholarmate.feedback.controller;

import com.scholarmate.entity.User;
import com.scholarmate.feedback.dto.FeedbackRecordResponseDto;
import com.scholarmate.feedback.dto.FeedbackRequestDto;
import com.scholarmate.feedback.service.FeedbackService;
import com.scholarmate.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/recommendations/feedback")
public class RecommendationFeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    public RecommendationFeedbackController(FeedbackService feedbackService, UserRepository userRepository) {
        this.feedbackService = feedbackService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<FeedbackRecordResponseDto> recordFeedback(
            Authentication authentication,
            @Valid @RequestBody FeedbackRequestDto request) {
        User user = getCurrentUser(authentication);
        FeedbackRecordResponseDto recorded = feedbackService.recordFeedback(user, request);
        return ResponseEntity.ok(recorded);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<FeedbackRecordResponseDto>> getMyFeedback(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(feedbackService.getStudentFeedback(user, PageRequest.of(page, size)));
    }

    @PostMapping("/process-pending")
    public ResponseEntity<Map<String, Object>> processPending(
            @RequestParam(defaultValue = "100") int limit) {
        int processed = feedbackService.processPendingFeedback(limit);
        return ResponseEntity.ok(Map.of(
                "status", "completed",
                "processedCount", processed
        ));
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authentication is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + authentication.getName()));
    }
}
