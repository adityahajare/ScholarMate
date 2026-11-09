package com.scholarmate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarmate.config.JwtService;
import com.scholarmate.dto.FieldDecisionItem;
import com.scholarmate.dto.InitAgentSessionRequest;
import com.scholarmate.dto.InspectPortalFieldsRequest;
import com.scholarmate.dto.PortalFieldDefinition;
import com.scholarmate.dto.RecordDecisionsRequest;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scholarmate.scraper.schedule.enabled=false"
)
class AgentAutofillControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private User student;
    private String studentToken;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void setUp() {
        String email = "agent_student_" + System.currentTimeMillis() + "@scholarmate.in";
        student = new User();
        student.setName("Aadi Developer");
        student.setEmail(email);
        student.setMobile("9823456789");
        student.setPassword(passwordEncoder.encode("Pass123!"));
        student.setRole("STUDENT");
        student = userRepository.save(student);

        UserProfile profile = UserProfile.builder()
                .user(student)
                .category("OPEN")
                .domicileState("Maharashtra")
                .familyIncome(200000.0)
                .course("Computer Science")
                .college("COEP Pune")
                .build();
        userProfileRepository.save(profile);

        studentToken = "Bearer " + jwtService.generateAccessToken(student.getEmail(), "STUDENT", student.getId());
    }

    @Test
    @DisplayName("GET /api/agent/vault/me returns compiled student vault")
    void testGetVault() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/agent/vault/me")))
                .header("Authorization", studentToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonNode root = objectMapper.readTree(response.body());
        assertEquals("Aadi Developer", root.get("identity").get("fullName").asText());
        assertEquals("9823456789", root.get("identity").get("mobile").asText());
        assertEquals(200000.0, root.get("family").get("familyIncome").asDouble());
    }

    @Test
    @DisplayName("End-to-End Agent Autofill Lifecycle: Init -> Inspect -> Decide -> Pause -> Resume")
    void testAgentAutofillLifecycle() throws Exception {
        // 1. Init Session
        InitAgentSessionRequest initReq = new InitAgentSessionRequest(
                null, "National Scholarship Portal", "https://scholarships.gov.in"
        );
        HttpRequest initHttp = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/agent/session/init")))
                .header("Authorization", studentToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(initReq)))
                .build();

        HttpResponse<String> initRes = httpClient.send(initHttp, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, initRes.statusCode());
        JsonNode sessionNode = objectMapper.readTree(initRes.body());
        String sessionId = sessionNode.get("sessionId").asText();
        assertNotNull(sessionId);

        // 2. Inspect portal fields (Simulating browser extension inspecting portal form)
        InspectPortalFieldsRequest inspectReq = new InspectPortalFieldsRequest(List.of(
                new PortalFieldDefinition("student_name", "Candidate Full Name", "text"),
                new PortalFieldDefinition("annual_income", "Annual Family Income", "number"),
                new PortalFieldDefinition("ration_card", "Ration Card Number", "text")
        ));
        HttpRequest inspectHttp = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/agent/session/" + sessionId + "/inspect")))
                .header("Authorization", studentToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(inspectReq)))
                .build();

        HttpResponse<String> inspectRes = httpClient.send(inspectHttp, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, inspectRes.statusCode());
        JsonNode inspectJson = objectMapper.readTree(inspectRes.body());

        assertEquals(3, inspectJson.get("totalFields").asInt());
        assertEquals(2, inspectJson.get("filledFields").asInt());
        assertEquals(1, inspectJson.get("missingFields").asInt());
        assertEquals(67, inspectJson.get("completionPercentage").asInt());

        JsonNode suggestions = inspectJson.get("suggestions");
        assertEquals("Aadi Developer", suggestions.get(0).get("suggestedValue").asText());
        assertEquals("PENDING", suggestions.get(0).get("userDecision").asText());

        // 3. Record decision (Accept name, reject or edit income) - VS Code autocomplete style
        RecordDecisionsRequest decReq = new RecordDecisionsRequest(List.of(
                new FieldDecisionItem("student_name", "ACCEPTED"),
                new FieldDecisionItem("annual_income", "EDITED", "220000")
        ));
        HttpRequest decHttp = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/agent/session/" + sessionId + "/decisions")))
                .header("Authorization", studentToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(decReq)))
                .build();

        HttpResponse<String> decRes = httpClient.send(decHttp, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, decRes.statusCode());
        JsonNode decJson = objectMapper.readTree(decRes.body());
        assertEquals(2, decJson.get("acceptedFields").asInt());

        // 4. Pause Session (Preserving progress)
        HttpRequest pauseHttp = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/agent/session/" + sessionId + "/pause")))
                .header("Authorization", studentToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> pauseRes = httpClient.send(pauseHttp, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, pauseRes.statusCode());
        JsonNode pauseJson = objectMapper.readTree(pauseRes.body());
        assertEquals("PAUSED", pauseJson.get("status").asText());

        // 5. Resume and get session
        HttpRequest getHttp = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/agent/session/" + sessionId)))
                .header("Authorization", studentToken)
                .GET()
                .build();

        HttpResponse<String> getRes = httpClient.send(getHttp, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getRes.statusCode());
        JsonNode getJson = objectMapper.readTree(getRes.body());
        assertEquals(sessionId, getJson.get("sessionId").asText());
        assertEquals(1, getJson.get("missingFieldLabels").size());
        assertEquals("Ration Card Number", getJson.get("missingFieldLabels").get(0).asText());
    }
}
