package com.scholarmate.service;

import com.scholarmate.controller.AuthController;
import com.scholarmate.dto.LoginRequest;
import com.scholarmate.dto.RegisterRequest;
import com.scholarmate.entity.FeatureStatusValue;
import com.scholarmate.entity.FeatureStatus;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserProfileRepository profileRepository = mock(UserProfileRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final UserService userService = new UserService(userRepository, profileRepository, passwordEncoder);
    private final AuthController authController = new AuthController(userService);
    private final Map<String, User> usersByEmail = new HashMap<>();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        usersByEmail.clear();
        mockMvc = standaloneSetup(authController).build();
        when(userRepository.existsByEmail(anyString()))
                .thenAnswer(invocation -> usersByEmail.containsKey(invocation.getArgument(0)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId((long) usersByEmail.size() + 1);
            usersByEmail.put(user.getEmail(), user);
            return user;
        });
        when(userRepository.findByEmail(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(usersByEmail.get(invocation.getArgument(0))));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void registersAndAuthenticatesTwoDummyStudentsAndRejectsWrongPassword() throws Exception {
        RegisterRequest first = registration("Asha Test", "asha.dummy@example.test", "dummy-pass-one");
        RegisterRequest second = registration("Rohan Test", "rohan.dummy@example.test", "dummy-pass-two");

        registerThroughApi(first);
        registerThroughApi(second);
        assertEquals(2, usersByEmail.size());
        assertEquals("STUDENT", usersByEmail.get(first.getEmail()).getRole());
        assertNotEquals(first.getPassword(), usersByEmail.get(first.getEmail()).getPassword());
        assertEquals(2, verifyProfilesWereCreated());
        verifyProfileFeatureValues(first);

        loginThroughApi(first.getEmail(), first.getPassword());
        loginThroughApi(second.getEmail(), second.getPassword());
        assertThrows(RuntimeException.class,
                () -> userService.loginUser(login(first.getEmail(), "incorrect-password")));
    }

    private void verifyProfileFeatureValues(RegisterRequest request) {
        org.mockito.ArgumentCaptor<UserProfile> profiles = org.mockito.ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository, org.mockito.Mockito.times(2)).save(profiles.capture());
        UserProfile profile = profiles.getAllValues().stream()
                .filter(saved -> saved.getUser().getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow();
        assertEquals(request.getEducationStage(), profile.getEducationStage());
        assertEquals(request.getFeatures(), profile.getFeatures());
        assertEquals(request.getBehaviors(), profile.getBehaviors());
    }

    private void registerThroughApi(RegisterRequest request) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    private void loginThroughApi(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    private String registrationJson(RegisterRequest request) {
        return "{\"name\":\"" + request.getName()
                + "\",\"email\":\"" + request.getEmail()
                + "\",\"password\":\"" + request.getPassword()
                + "\",\"course\":\"" + request.getCourse()
                + "\",\"college\":\"" + request.getCollege()
                + "\",\"category\":\"" + request.getCategory()
                + "\",\"state\":\"" + request.getState()
                + "\",\"gender\":\"" + request.getGender()
                + "\",\"yearOfStudy\":" + request.getYearOfStudy()
                + ",\"cgpa\":" + request.getCgpa()
                + ",\"familyIncome\":" + request.getFamilyIncome()
                + ",\"educationStage\":\"" + request.getEducationStage()
                + "\",\"features\":{\"branch\":{\"status\":\"KNOWN\",\"value\":\"AI/ML\"}}"
                + ",\"behaviors\":{\"views_last_7_days\":{\"status\":\"KNOWN\",\"value\":2}}}";
    }

    private String loginJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private int verifyProfilesWereCreated() {
        verify(profileRepository, org.mockito.Mockito.times(2)).save(any(UserProfile.class));
        return 2;
    }

    private RegisterRequest registration(String name, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        request.setCourse("B.Tech Computer Science");
        request.setCollege("Dummy College");
        request.setCategory("OBC");
        request.setState("Maharashtra");
        request.setGender("Other");
        request.setYearOfStudy(2);
        request.setCgpa(8.2);
        request.setFamilyIncome(250000.0);
        request.setEducationStage("UG");
        request.setFeatures(Map.of("branch", new FeatureStatusValue(FeatureStatus.KNOWN, "AI/ML")));
        request.setBehaviors(Map.of("views_last_7_days", new FeatureStatusValue(FeatureStatus.KNOWN, 2)));
        return request;
    }

    private LoginRequest login(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
