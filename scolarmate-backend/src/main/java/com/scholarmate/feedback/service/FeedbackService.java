package com.scholarmate.feedback.service;

import com.scholarmate.entity.RecommendationFeedback;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.feedback.client.MlFeedbackClient;
import com.scholarmate.feedback.client.MlFeedbackEventDto;
import com.scholarmate.feedback.client.MlFeedbackResponseDto;
import com.scholarmate.feedback.dto.FeedbackRecordResponseDto;
import com.scholarmate.feedback.dto.FeedbackRequestDto;
import com.scholarmate.repository.RecommendationFeedbackRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final RecommendationFeedbackRepository feedbackRepo;
    private final ScholarshipRepository scholarshipRepo;
    private final UserProfileRepository userProfileRepo;
    private final MlFeedbackClient mlFeedbackClient;

    public FeedbackService(
            RecommendationFeedbackRepository feedbackRepo,
            ScholarshipRepository scholarshipRepo,
            UserProfileRepository userProfileRepo,
            MlFeedbackClient mlFeedbackClient) {
        this.feedbackRepo = feedbackRepo;
        this.scholarshipRepo = scholarshipRepo;
        this.userProfileRepo = userProfileRepo;
        this.mlFeedbackClient = mlFeedbackClient;
    }

    @Transactional
    public FeedbackRecordResponseDto recordFeedback(User student, FeedbackRequestDto request) {
        Scholarship scholarship = scholarshipRepo.findById(request.scholarshipId())
                .orElseThrow(() -> new IllegalArgumentException("Scholarship not found: " + request.scholarshipId()));

        Map<String, Object> features = buildStudentFeatures(student, scholarship);
        if (request.contextData() != null) {
            features.putAll(request.contextData());
        }

        RecommendationFeedback feedback = RecommendationFeedback.builder()
                .student(student)
                .scholarship(scholarship)
                .eventType(request.eventType().trim().toUpperCase())
                .eventTimestamp(Instant.now())
                .recommendationVersion(request.recommendationVersion() != null ? request.recommendationVersion() : "hybrid-v1")
                .modelVersion(request.modelVersion() != null ? request.modelVersion() : "river-online-v1")
                .processedForTraining(false)
                .contextData(features)
                .build();

        RecommendationFeedback saved = feedbackRepo.save(feedback);

        // Immediate online training step
        MlFeedbackEventDto mlEvent = new MlFeedbackEventDto(
                student.getId(),
                scholarship.getId(),
                saved.getEventType(),
                features,
                saved.getEventTimestamp().toString()
        );

        MlFeedbackResponseDto mlResponse = mlFeedbackClient.sendFeedback(mlEvent);
        if ("learned".equalsIgnoreCase(mlResponse.status())) {
            saved.setProcessedForTraining(true);
            feedbackRepo.save(saved);
            log.info("Feedback recorded and online model updated: student={} scholarship={} event={} samples={}",
                    student.getId(), scholarship.getId(), saved.getEventType(), mlResponse.samplesSeen());
        }

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackRecordResponseDto> getStudentFeedback(User student, Pageable pageable) {
        return feedbackRepo.findByStudentIdOrderByEventTimestampDesc(student.getId(), pageable)
                .map(this::toDto);
    }

    @Transactional
    public int processPendingFeedback(int limit) {
        List<RecommendationFeedback> pending = feedbackRepo.findByProcessedForTrainingFalse(PageRequest.of(0, limit));
        if (pending.isEmpty()) {
            return 0;
        }

        List<MlFeedbackEventDto> batch = new ArrayList<>();
        List<Long> processedIds = new ArrayList<>();

        for (RecommendationFeedback item : pending) {
            batch.add(new MlFeedbackEventDto(
                    item.getStudent().getId(),
                    item.getScholarship().getId(),
                    item.getEventType(),
                    item.getContextData() != null ? item.getContextData() : Map.of(),
                    item.getEventTimestamp().toString()
            ));
            processedIds.add(item.getId());
        }

        boolean success = mlFeedbackClient.sendBatch(batch);
        if (success && !processedIds.isEmpty()) {
            feedbackRepo.markProcessedForTraining(processedIds);
            log.info("Batch processed {} pending feedback records for online learning", processedIds.size());
            return processedIds.size();
        }
        return 0;
    }

    private Map<String, Object> buildStudentFeatures(User student, Scholarship scholarship) {
        Map<String, Object> features = new HashMap<>();
        if (student != null) {
            UserProfile profile = userProfileRepo.findByUser(student).orElse(null);
            if (profile != null) {
                if (profile.getCgpa() != null) features.put("cgpa", profile.getCgpa());
                if (profile.getFamilyIncome() != null) features.put("family_income", profile.getFamilyIncome());
                if (profile.getYearOfStudy() != null) features.put("year_of_study", profile.getYearOfStudy());
            }
        }
        if (scholarship != null && scholarship.getAmount() != null) {
            features.put("scholarship_amount", scholarship.getAmount());
        }
        return features;
    }

    private FeedbackRecordResponseDto toDto(RecommendationFeedback f) {
        return new FeedbackRecordResponseDto(
                f.getId(),
                f.getStudent() != null ? f.getStudent().getId() : null,
                f.getScholarship() != null ? f.getScholarship().getId() : null,
                f.getScholarship() != null ? f.getScholarship().getName() : null,
                f.getEventType(),
                f.getEventTimestamp(),
                f.getRecommendationVersion(),
                f.getModelVersion(),
                f.isProcessedForTraining(),
                f.getContextData()
        );
    }
}
