package com.scholarmate.feedback.service;

import com.scholarmate.entity.RecommendationFeedback;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.User;
import com.scholarmate.feedback.client.MlFeedbackClient;
import com.scholarmate.feedback.client.MlFeedbackEventDto;
import com.scholarmate.feedback.client.MlFeedbackResponseDto;
import com.scholarmate.feedback.dto.FeedbackRecordResponseDto;
import com.scholarmate.feedback.dto.FeedbackRequestDto;
import com.scholarmate.repository.RecommendationFeedbackRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private RecommendationFeedbackRepository feedbackRepo;

    @Mock
    private ScholarshipRepository scholarshipRepo;

    @Mock
    private UserProfileRepository userProfileRepo;

    @Mock
    private MlFeedbackClient mlFeedbackClient;

    private FeedbackService feedbackService;

    @BeforeEach
    void setUp() {
        feedbackService = new FeedbackService(feedbackRepo, scholarshipRepo, userProfileRepo, mlFeedbackClient);
    }

    @Test
    void recordFeedback_persistsEventAndSendsToMlService() {
        User student = new User();
        student.setId(42L);
        student.setEmail("student@test.com");

        Scholarship scholarship = Scholarship.builder()
                .id(101L)
                .name("Post-Matric Scholarship")
                .amount(12000.0)
                .build();

        when(scholarshipRepo.findById(101L)).thenReturn(Optional.of(scholarship));
        when(userProfileRepo.findByUser(student)).thenReturn(Optional.empty());

        when(feedbackRepo.save(any(RecommendationFeedback.class)))
                .thenAnswer(inv -> {
                    RecommendationFeedback f = inv.getArgument(0);
                    f.setId(1L);
                    return f;
                });

        when(mlFeedbackClient.sendFeedback(any(MlFeedbackEventDto.class)))
                .thenReturn(new MlFeedbackResponseDto("learned", "APPLY_START", 5, 0.85, "river-online-v1", null));

        FeedbackRequestDto request = new FeedbackRequestDto(101L, "APPLY_START", "v1", "v1", Map.of("source", "card_click"));
        FeedbackRecordResponseDto response = feedbackService.recordFeedback(student, request);

        assertThat(response).isNotNull();
        assertThat(response.scholarshipId()).isEqualTo(101L);
        assertThat(response.eventType()).isEqualTo("APPLY_START");
        assertThat(response.processedForTraining()).isTrue();

        ArgumentCaptor<MlFeedbackEventDto> captor = ArgumentCaptor.forClass(MlFeedbackEventDto.class);
        verify(mlFeedbackClient).sendFeedback(captor.capture());
        assertThat(captor.getValue().studentId()).isEqualTo(42L);
        assertThat(captor.getValue().scholarshipId()).isEqualTo(101L);
        assertThat(captor.getValue().eventType()).isEqualTo("APPLY_START");
    }
}
