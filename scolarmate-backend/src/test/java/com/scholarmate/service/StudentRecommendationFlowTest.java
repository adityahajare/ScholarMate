package com.scholarmate.service;

import com.scholarmate.dto.StudentRecommendations;
import com.scholarmate.entity.FeatureStatusValue;
import com.scholarmate.entity.FeatureStatus;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudentRecommendationFlowTest {

    @Test
    void filtersEligibilitySerializesRequestAndMapsPythonResponse() throws Exception {
        HttpServer mlServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> receivedBody = new AtomicReference<>();
        mlServer.createContext("/api/v1/recommend", exchange -> respond(exchange, receivedBody));
        mlServer.start();

        try {
            UserRepository userRepository = mock(UserRepository.class);
            UserProfileRepository profileRepository = mock(UserProfileRepository.class);
            ScholarshipRepository scholarshipRepository = mock(ScholarshipRepository.class);
            EligibilityCriteriaRepository criteriaRepository = mock(EligibilityCriteriaRepository.class);
            User user = new User();
            user.setId(10L);
            user.setEmail("flow-test@example.test");
            UserProfile profile = UserProfile.builder()
                    .course("B.Tech")
                    .state("Maharashtra")
                    .category("OBC")
                    .cgpa(8.5)
                    .familyIncome(250000.0)
                    .yearOfStudy(3)
                    .educationStage("UG")
                    .features(Map.of(
                            "branch", new FeatureStatusValue(FeatureStatus.KNOWN, "AI/ML"),
                            "previous_score", new FeatureStatusValue(FeatureStatus.UNKNOWN, null)))
                    .behaviors(Map.of(
                            "views_last_7_days", new FeatureStatusValue(FeatureStatus.KNOWN, 4)))
                    .build();
            Scholarship eligible = Scholarship.builder()
                    .id(101L)
                    .name("Merit Scholarship")
                    .description("Scholarship for eligible engineering students.")
                    .provider("Test Provider")
                    .course("B.Tech")
                    .state("Maharashtra")
                    .category("OBC")
                    .minimumCgpa(7.0)
                    .maximumFamilyIncome(800000.0)
                    .minimumYearOfStudy(1)
                    .maximumYearOfStudy(4)
                    .features(Map.of(
                            "required_branch", new FeatureStatusValue(FeatureStatus.KNOWN, "AI/ML")))
                    .build();
            Scholarship ineligible = Scholarship.builder()
                    .id(102L)
                    .name("Higher CGPA Scholarship")
                    .minimumCgpa(9.0)
                    .build();
            Scholarship unknown = Scholarship.builder()
                    .id(103L)
                    .name("Unverified Scholarship")
                    .build();

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(profileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
            when(scholarshipRepository.findByActiveTrue()).thenReturn(List.of(eligible, ineligible, unknown));
            when(criteriaRepository.findByScholarshipIdIn(List.of(101L, 102L, 103L))).thenReturn(List.of());

            StudentRecommendationService service = new StudentRecommendationService(
                    userRepository,
                    profileRepository,
                    scholarshipRepository,
                    criteriaRepository,
                    new EligibilityEvaluationService(),
                    new MlRecommendationClient("http://127.0.0.1:" + mlServer.getAddress().getPort()));

            StudentRecommendations result = service.recommendForStudent(user.getEmail());

            assertEquals(1, result.eligibleCandidateCount());
            assertEquals(1, result.recommendations().size());
            assertEquals(101L, result.recommendations().getFirst().scholarshipId());
            assertEquals("Merit Scholarship", result.recommendations().getFirst().name());
            assertEquals(0.91, result.recommendations().getFirst().score());
            assertNull(result.pipeline().clusteringMetrics().get("hdbscan").get("silhouette"));
            assertTrue(receivedBody.get().contains("\"familyIncome\":250000.0"));
            assertTrue(receivedBody.get().contains("\"educationStage\":\"UG\""));
            assertTrue(receivedBody.get().contains("\"branch\":{\"status\":\"KNOWN\",\"value\":\"AI/ML\"}"));
            assertTrue(receivedBody.get().contains("\"previous_score\":{\"status\":\"UNKNOWN\",\"value\":null}"));
            assertTrue(receivedBody.get().contains("\"views_last_7_days\":{\"status\":\"KNOWN\",\"value\":4}"));
            assertTrue(receivedBody.get().contains("\"required_branch\":{\"status\":\"KNOWN\",\"value\":\"AI/ML\"}"));
            assertTrue(receivedBody.get().contains("\"id\":101"));
            assertTrue(!receivedBody.get().contains("\"id\":102"));
            assertTrue(!receivedBody.get().contains("\"id\":103"));
        } finally {
            mlServer.stop(0);
        }
    }

    private static void respond(HttpExchange exchange, AtomicReference<String> receivedBody) throws IOException {
        receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        String response = """
                {"recommendations":[{"scholarshipId":101,"score":0.91,
                  "signals":{"autoencoder":0.8,"semantic":0.9,"clustering":1.0,"graph":0.7},
                  "clusters":{"kmeans":1,"gmm":0,"hdbscan":-1},
                  "reasons":["Highest matching signal: semantic"]}],
                 "pipeline":{"version":"unsupervised-hybrid-v1","candidateCount":1,
                  "latentDimensions":32,"semanticModel":"test-model","semanticStatus":"available",
                  "clusteringMetrics":{"hdbscan":{"silhouette":null,"daviesBouldin":null}},
                  "rankingWeights":{"autoencoder":0.25,"semantic":0.35,"clustering":0.2,"graph":0.2}}}
                """;
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
