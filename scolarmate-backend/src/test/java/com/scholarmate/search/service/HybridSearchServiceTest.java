package com.scholarmate.search.service;

import com.scholarmate.discovery.service.AdaptiveEligibilityEvaluator;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.search.client.MlSemanticSearchClient;
import com.scholarmate.search.client.MlSemanticSearchResponseDto;
import com.scholarmate.search.dto.HybridSearchResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HybridSearchServiceTest {

    @Mock
    private ScholarshipRepository scholarshipRepo;

    @Mock
    private UserProfileRepository userProfileRepo;

    @Mock
    private MlSemanticSearchClient semanticSearchClient;

    @Mock
    private AdaptiveEligibilityEvaluator eligibilityEvaluator;

    private HybridSearchService hybridSearchService;

    @BeforeEach
    void setUp() {
        hybridSearchService = new HybridSearchService(scholarshipRepo, userProfileRepo, semanticSearchClient, eligibilityEvaluator);
    }

    @Test
    void search_returnsEmptyForBlankQuery() {
        HybridSearchResponseDto result = hybridSearchService.search("   ", null, 10);
        assertThat(result.totalMatches()).isEqualTo(0);
        assertThat(result.results()).isEmpty();
    }

    @Test
    void search_mergesFuzzyAndSemanticResults() {
        Scholarship s1 = Scholarship.builder()
                .id(1L)
                .name("National Engineering Merit Scholarship")
                .provider("Ministry of Education")
                .category("Engineering")
                .amount(50000.0)
                .build();

        Scholarship s2 = Scholarship.builder()
                .id(2L)
                .name("State Higher Education Bursary")
                .provider("State Government")
                .category("General")
                .amount(25000.0)
                .build();

        when(scholarshipRepo.searchFuzzyTrigram(eq("engneering"), anyDouble(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(s1, s2)));

        when(scholarshipRepo.searchActive(eq("engneering"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        when(semanticSearchClient.search(eq("engneering"), anyList(), anyInt()))
                .thenReturn(List.of(
                        new MlSemanticSearchResponseDto.MlSemanticMatch(1L, 0.88),
                        new MlSemanticSearchResponseDto.MlSemanticMatch(2L, 0.42)
                ));

        HybridSearchResponseDto response = hybridSearchService.search("engneering", null, 10);

        assertThat(response.totalMatches()).isEqualTo(2);
        assertThat(response.results()).hasSize(2);

        // First item should be the higher scoring one (s1)
        assertThat(response.results().get(0).scholarshipId()).isEqualTo(1L);
        assertThat(response.results().get(0).finalScore()).isGreaterThan(response.results().get(1).finalScore());
        assertThat(response.results().get(0).matchReasons()).isNotEmpty();
    }
}
