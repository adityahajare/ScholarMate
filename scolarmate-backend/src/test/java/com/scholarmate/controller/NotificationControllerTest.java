package com.scholarmate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarmate.config.JwtService;
import com.scholarmate.dto.CreateNotificationRequest;
import com.scholarmate.entity.User;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scholarmate.scraper.schedule.enabled=false"
)
class NotificationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

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
        String email = "notify_student_" + System.currentTimeMillis() + "@scholarmate.in";
        student = new User();
        student.setName("Notify Student");
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode("Secret123!"));
        student.setRole("STUDENT");
        student = userRepository.save(student);

        studentToken = "Bearer " + jwtService.generateAccessToken(student.getEmail(), "STUDENT", student.getId());
    }

    @Test
    @DisplayName("GET /api/notifications returns 401 without token")
    void testGetNotificationsUnauthorized() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/notifications")))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, response.statusCode());
    }

    @Test
    @DisplayName("Notification Lifecycle: Create -> Get -> Unread Count -> Mark Read -> Summary")
    void testNotificationLifecycle() throws Exception {
        // 1. Create a notification
        CreateNotificationRequest createReq = new CreateNotificationRequest(
                "Scholarship Match",
                "Your profile qualifies for National Merit Scheme.",
                "SCHOLARSHIP",
                "/scholarships/101",
                null
        );

        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/notifications")))
                .header("Authorization", studentToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(createReq)))
                .build();

        HttpResponse<String> postRes = httpClient.send(postReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postRes.statusCode());

        JsonNode createdNode = objectMapper.readTree(postRes.body());
        Long notificationId = createdNode.get("id").asLong();
        assertEquals("Scholarship Match", createdNode.get("title").asText());
        assertFalse(createdNode.get("isRead").asBoolean());

        // 2. Unread count should be at least 1
        HttpRequest unreadReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/notifications/unread-count")))
                .header("Authorization", studentToken)
                .GET()
                .build();

        HttpResponse<String> unreadRes = httpClient.send(unreadReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, unreadRes.statusCode());
        JsonNode unreadNode = objectMapper.readTree(unreadRes.body());
        assertTrue(unreadNode.get("unreadCount").asLong() >= 1);

        // 3. Get summary
        HttpRequest summaryReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/notifications/summary")))
                .header("Authorization", studentToken)
                .GET()
                .build();

        HttpResponse<String> summaryRes = httpClient.send(summaryReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, summaryRes.statusCode());
        JsonNode summaryNode = objectMapper.readTree(summaryRes.body());
        assertTrue(summaryNode.get("unreadCount").asLong() >= 1);
        assertTrue(summaryNode.get("notifications").size() >= 1);

        // 4. Mark as read
        HttpRequest readReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/notifications/" + notificationId + "/read")))
                .header("Authorization", studentToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> readRes = httpClient.send(readReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, readRes.statusCode());
        JsonNode readNode = objectMapper.readTree(readRes.body());
        assertTrue(readNode.get("isRead").asBoolean());
        assertNotNull(readNode.get("readAt"));

        // 5. Read-all endpoint
        HttpRequest readAllReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/notifications/read-all")))
                .header("Authorization", studentToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> readAllRes = httpClient.send(readAllReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, readAllRes.statusCode());

        // 6. Delete notification
        HttpRequest deleteReq = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/notifications/" + notificationId)))
                .header("Authorization", studentToken)
                .DELETE()
                .build();

        HttpResponse<String> deleteRes = httpClient.send(deleteReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteRes.statusCode());
    }
}
