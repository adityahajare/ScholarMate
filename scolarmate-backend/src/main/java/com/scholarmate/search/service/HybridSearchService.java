package com.scholarmate.search.service;

import com.scholarmate.discovery.dto.ScholarshipMatchResponseDto;
import com.scholarmate.discovery.service.AdaptiveEligibilityEvaluator;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.search.client.MlSemanticSearchClient;
import com.scholarmate.search.client.MlSemanticSearchRequestDto.MlDocSnippet;
import com.scholarmate.search.client.MlSemanticSearchResponseDto.MlSemanticMatch;
import com.scholarmate.search.dto.HybridSearchResponseDto;
import com.scholarmate.search.dto.HybridSearchResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HybridSearchService {

    private static final Logger log = LoggerFactory.getLogger(HybridSearchService.class);

    private final ScholarshipRepository scholarshipRepo;
    private final UserProfileRepository userProfileRepo;
    private final MlSemanticSearchClient semanticSearchClient;
    private final AdaptiveEligibilityEvaluator eligibilityEvaluator;

    public HybridSearchService(
            ScholarshipRepository scholarshipRepo,
            UserProfileRepository userProfileRepo,
            MlSemanticSearchClient semanticSearchClient,
            AdaptiveEligibilityEvaluator eligibilityEvaluator) {
        this.scholarshipRepo = scholarshipRepo;
        this.userProfileRepo = userProfileRepo;
        this.semanticSearchClient = semanticSearchClient;
        this.eligibilityEvaluator = eligibilityEvaluator;
    }

    @Transactional(readOnly = true)
    public HybridSearchResponseDto search(String query, User student, int limit) {
        String cleanQuery = (query != null) ? query.trim() : "";
        if (cleanQuery.isEmpty()) {
            return new HybridSearchResponseDto(cleanQuery, 0, Collections.emptyList(), "EMPTY_QUERY");
        }

        // 1. Fuzzy Trigram Search Candidates (handles misspellings like 'engneering')
        List<Scholarship> fuzzyCandidates = scholarshipRepo
                .searchFuzzyTrigram(cleanQuery, 0.12, PageRequest.of(0, 40))
                .getContent();

        // 2. Keyword fallback if fuzzy pool is small
        Set<Long> candidateIds = new LinkedHashSet<>();
        List<Scholarship> candidatePool = new ArrayList<>(fuzzyCandidates);
        for (Scholarship s : candidatePool) {
            candidateIds.add(s.getId());
        }

        if (candidatePool.size() < 15) {
            org.springframework.data.domain.Page<Scholarship> keywordCandidates = scholarshipRepo
                    .searchActive(cleanQuery, PageRequest.of(0, 30));
            if (keywordCandidates != null && keywordCandidates.getContent() != null) {
                for (Scholarship s : keywordCandidates.getContent()) {
                    if (candidateIds.add(s.getId())) {
                        candidatePool.add(s);
                    }
                }
            }
        }

        if (candidatePool.isEmpty()) {
            return new HybridSearchResponseDto(cleanQuery, 0, Collections.emptyList(), "HYBRID_FUZZY_SEMANTIC");
        }

        // 3. Semantic Search via SentenceTransformers
        List<MlDocSnippet> snippets = candidatePool.stream()
                .map(s -> new MlDocSnippet(s.getId(), s.getName(), s.getDescription(), s.getProvider(), s.getCategory()))
                .collect(Collectors.toList());

        List<MlSemanticMatch> semanticMatches = semanticSearchClient.search(cleanQuery, snippets, candidatePool.size());
        Map<Long, Double> semanticScoresById = semanticMatches.stream()
                .collect(Collectors.toMap(MlSemanticMatch::scholarshipId, MlSemanticMatch::score, (a, b) -> a));

        // 4. Deterministic Eligibility Check (if student profile exists)
        UserProfile profile = (student != null) ? userProfileRepo.findByUser(student).orElse(null) : null;
        Map<String, ScholarshipMatchResponseDto> eligibilityMap = new HashMap<>();
        if (profile != null) {
            try {
                List<ScholarshipMatchResponseDto> evaluations = eligibilityEvaluator.evaluate(profile, Collections.emptyMap());
                for (ScholarshipMatchResponseDto eval : evaluations) {
                    eligibilityMap.put(eval.getScholarshipName().toLowerCase(), eval);
                }
            } catch (Exception e) {
                log.warn("Deterministic eligibility evaluation skipped during search: {}", e.getMessage());
            }
        }

        // 5. Score Fusion and Explainable Recommendation Generation
        List<HybridSearchResultDto> results = new ArrayList<>();
        String lowerQuery = cleanQuery.toLowerCase();

        for (Scholarship s : candidatePool) {
            double fuzzyScore = computeFuzzyScore(s.getName(), lowerQuery);
            double semanticScore = semanticScoresById.getOrDefault(s.getId(), 0.0);

            // Deterministic eligibility status
            String eligibilityStatus = "UNCHECKED";
            double eligibilityScore = 0.5;
            List<String> reasons = new ArrayList<>();

            if (profile != null) {
                ScholarshipMatchResponseDto match = findMatchByName(eligibilityMap, s.getName());
                if (match != null) {
                    eligibilityStatus = match.getEvaluationStatus();
                    if ("POTENTIALLY_ELIGIBLE".equals(eligibilityStatus)) {
                        eligibilityScore = 1.0;
                        reasons.add("Meets core eligibility requirements based on your profile");
                    } else if ("NOT_ELIGIBLE".equals(eligibilityStatus)) {
                        eligibilityScore = 0.0;
                        reasons.add("Ineligible: Did not meet criteria (" + String.join(", ", match.getFailedConditions()) + ")");
                    } else if ("NEEDS_MORE_INFORMATION".equals(eligibilityStatus)) {
                        eligibilityScore = 0.6;
                        reasons.add("Candidate match; complete additional profile questions to verify");
                    }
                } else {
                    eligibilityStatus = "PENDING_VERIFICATION";
                    eligibilityScore = 0.5;
                }
            }

            if (fuzzyScore > 0.4) {
                reasons.add(String.format("Strong textual title match (score: %.2f)", fuzzyScore));
            }
            if (semanticScore > 0.4) {
                reasons.add(String.format("Semantic similarity to query topic (score: %.2f)", semanticScore));
            }
            if (s.getAmount() != null && s.getAmount() > 0) {
                reasons.add(String.format("Award amount: ₹%.0f", s.getAmount()));
            }

            // Fusion: 45% semantic, 35% fuzzy, 20% deterministic eligibility
            double finalScore = (0.45 * semanticScore) + (0.35 * fuzzyScore) + (0.20 * eligibilityScore);

            results.add(new HybridSearchResultDto(
                    s.getId(),
                    s.getName(),
                    s.getProvider(),
                    s.getCategory(),
                    s.getAmount(),
                    s.getApplicationStartDate() != null ? s.getApplicationStartDate().toString() : null,
                    s.getApplicationUrl(),
                    Math.round(fuzzyScore * 100.0) / 100.0,
                    Math.round(semanticScore * 100.0) / 100.0,
                    Math.round(finalScore * 100.0) / 100.0,
                    eligibilityStatus,
                    reasons
            ));
        }

        // Sort descending by finalScore
        results.sort(Comparator.comparingDouble(HybridSearchResultDto::finalScore).reversed());

        int effectiveLimit = Math.min(results.size(), Math.max(1, limit));
        List<HybridSearchResultDto> paged = results.subList(0, effectiveLimit);

        return new HybridSearchResponseDto(cleanQuery, results.size(), paged, "HYBRID_FUZZY_SEMANTIC_DETERMINISTIC");
    }

    private double computeFuzzyScore(String name, String query) {
        if (name == null || query.isEmpty()) return 0.0;
        String lowerName = name.toLowerCase();
        if (lowerName.contains(query)) {
            return 1.0;
        }

        // Trigram similarity (exact pg_trgm calculation)
        String a = "  " + lowerName.trim() + " ";
        String b = "  " + query.trim() + " ";
        if (a.length() < 3 || b.length() < 3) return 0.0;

        Set<String> triA = new HashSet<>();
        for (int i = 0; i <= a.length() - 3; i++) {
            triA.add(a.substring(i, i + 3));
        }

        Set<String> triB = new HashSet<>();
        for (int i = 0; i <= b.length() - 3; i++) {
            triB.add(b.substring(i, i + 3));
        }

        int intersection = 0;
        for (String t : triA) {
            if (triB.contains(t)) {
                intersection++;
            }
        }
        int union = triA.size() + triB.size() - intersection;
        double trigramSim = union == 0 ? 0.0 : (double) intersection / union;

        // Approximate token overlap
        String[] queryTokens = query.split("\\s+");
        int matched = 0;
        for (String token : queryTokens) {
            if (token.length() > 2 && lowerName.contains(token)) {
                matched++;
            }
        }
        double tokenOverlap = (double) matched / Math.max(1, queryTokens.length);

        return Math.max(trigramSim, tokenOverlap);
    }

    private ScholarshipMatchResponseDto findMatchByName(Map<String, ScholarshipMatchResponseDto> map, String name) {
        if (name == null) return null;
        String lower = name.toLowerCase().trim();
        if (map.containsKey(lower)) {
            return map.get(lower);
        }
        for (Map.Entry<String, ScholarshipMatchResponseDto> e : map.entrySet()) {
            if (lower.contains(e.getKey()) || e.getKey().contains(lower)) {
                return e.getValue();
            }
        }
        return null;
    }
}
