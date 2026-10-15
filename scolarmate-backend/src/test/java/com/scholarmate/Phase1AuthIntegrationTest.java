package com.scholarmate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarmate.dto.*;
import com.scholarmate.entity.User;
import com.scholarmate.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scholarmate.scraper.schedule.enabled=false"
)
class Phase1AuthIntegrationTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("Gate 1 Scenario: Register -> Login -> Own Profile works; User B cannot read User A's profile")
    void testPhase1CompleteGateScenario() throws Exception {
        String emailA = "gate_alpha_" + System.currentTimeMillis() + "@scholarmate.in";
        String emailB = "gate_beta_" + System.currentTimeMillis() + "@scholarmate.in";

        // 1. Register User A with full Phase 1 fields
        RegisterRequest reqA = new RegisterRequest();
        reqA.setName("Student Alpha");
        reqA.setEmail(emailA);
        reqA.setPassword("AlphaSecret123!");
        reqA.setMobile("9876543210");
        reqA.setEducationType("HIGHER_SECONDARY");
        reqA.setEducationLevel("12th Standard");
        reqA.setCourse("Science");
        reqA.setState("Maharashtra");
        reqA.setTermsAccepted(true);

        HttpRequest postRegisterA = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/auth/register")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(reqA)))
                .build();

        HttpResponse<String> regResA = httpClient.send(postRegisterA, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, regResA.statusCode(), "Registration of User A should succeed: " + regResA.body());

        RegisterResponse regResponseA = objectMapper.readValue(regResA.body(), RegisterResponse.class);
        assertNotNull(regResponseA.getToken(), "Access token should be issued on register");
        assertNotNull(regResponseA.getRefreshToken(), "Refresh token should be issued on register");
        Long userAId = regResponseA.getId();

        // 2. Login User A
        LoginRequest loginReqA = new LoginRequest();
        loginReqA.setEmail(emailA);
        loginReqA.setPassword("AlphaSecret123!");

        HttpRequest postLoginA = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/auth/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginReqA)))
                .build();

        HttpResponse<String> loginResA = httpClient.send(postLoginA, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, loginResA.statusCode());

        AuthResponse authA = objectMapper.readValue(loginResA.body(), AuthResponse.class);
        String tokenA = authA.token();
        String refreshTokenA = authA.refreshToken();
        assertNotNull(tokenA);
        assertNotNull(refreshTokenA);

        // 3. User A reads own profile via /me
        HttpRequest getProfileMe = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/profiles/me")))
                .header("Authorization", "Bearer " + tokenA)
                .GET()
                .build();
        HttpResponse<String> profileMeRes = httpClient.send(getProfileMe, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, profileMeRes.statusCode());
        assertTrue(profileMeRes.body().contains("Student Alpha"));

        // 4. User A reads own profile via /{userAId}
        HttpRequest getProfileIdA = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/profiles/" + userAId)))
                .header("Authorization", "Bearer " + tokenA)
                .GET()
                .build();
        HttpResponse<String> profileIdARes = httpClient.send(getProfileIdA, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, profileIdARes.statusCode());
        assertTrue(profileIdARes.body().contains("Student Alpha"));

        // 5. Register and login User B
        RegisterRequest reqB = new RegisterRequest();
        reqB.setName("Student Beta");
        reqB.setEmail(emailB);
        reqB.setPassword("BetaSecret123!");
        reqB.setMobile("9123456780");
        reqB.setEducationType("UNDERGRADUATE");
        reqB.setEducationLevel("B.Tech");
        reqB.setCourse("Computer Engineering");
        reqB.setState("Karnataka");
        reqB.setTermsAccepted(true);

        HttpRequest postRegisterB = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/auth/register")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(reqB)))
                .build();
        HttpResponse<String> regResB = httpClient.send(postRegisterB, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, regResB.statusCode());

        LoginRequest loginReqB = new LoginRequest();
        loginReqB.setEmail(emailB);
        loginReqB.setPassword("BetaSecret123!");

        HttpRequest postLoginB = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/auth/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginReqB)))
                .build();
        HttpResponse<String> loginResB = httpClient.send(postLoginB, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, loginResB.statusCode());

        AuthResponse authB = objectMapper.readValue(loginResB.body(), AuthResponse.class);
        String tokenB = authB.token();

        // 6. User B attempts to read User A's profile via /{userAId} -> MUST RETURN 403 FORBIDDEN
        HttpRequest getUnauthorizedProfile = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/profiles/" + userAId)))
                .header("Authorization", "Bearer " + tokenB)
                .GET()
                .build();
        HttpResponse<String> unauthRes = httpClient.send(getUnauthorizedProfile, HttpResponse.BodyHandlers.ofString());
        assertEquals(403, unauthRes.statusCode(), "User B must not be allowed to read User A's profile");

        // 7. Token refresh verification: User A uses refresh token to obtain fresh access token
        RefreshTokenRequest refreshReq = new RefreshTokenRequest(refreshTokenA);
        HttpRequest postRefresh = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/auth/refresh")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(refreshReq)))
                .build();
        HttpResponse<String> refreshRes = httpClient.send(postRefresh, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, refreshRes.statusCode());

        AuthResponse refreshedAuthA = objectMapper.readValue(refreshRes.body(), AuthResponse.class);
        assertNotNull(refreshedAuthA.token());

        // 8. Forgot password & Reset password flow
        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest();
        forgotReq.setEmail(emailA);
        HttpRequest postForgot = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/auth/forgot-password")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(forgotReq)))
                .build();
        HttpResponse<String> forgotRes = httpClient.send(postForgot, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, forgotRes.statusCode());

        User userFromDb = userRepository.findByEmail(emailA).orElseThrow();
        String resetToken = userFromDb.getResetToken();
        assertNotNull(resetToken, "Reset token should be stored on User entity");

        ResetPasswordRequest resetReq = new ResetPasswordRequest();
        resetReq.setToken(resetToken);
        resetReq.setNewPassword("NewAlphaSecret456!");

        HttpRequest postReset = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/auth/reset-password")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(resetReq)))
                .build();
        HttpResponse<String> resetRes = httpClient.send(postReset, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resetRes.statusCode());

        // Login with old password fails
        HttpRequest postLoginOld = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/auth/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginReqA)))
                .build();
        HttpResponse<String> loginOldRes = httpClient.send(postLoginOld, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, loginOldRes.statusCode());

        // Login with new password succeeds
        loginReqA.setPassword("NewAlphaSecret456!");
        HttpRequest postLoginNew = HttpRequest.newBuilder()
                .uri(URI.create(url("/api/auth/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginReqA)))
                .build();
        HttpResponse<String> loginNewRes = httpClient.send(postLoginNew, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, loginNewRes.statusCode());
    }
}
