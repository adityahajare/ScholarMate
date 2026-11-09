package com.scholarmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarmate.dto.*;
import com.scholarmate.entity.AgentAutofillSession;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.AgentAutofillSessionRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentAutofillServiceTest {

    @Mock
    private AgentAutofillSessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ScholarshipRepository scholarshipRepository;

    @Mock
    private StudentAutofillVaultService vaultService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AgentAutofillService agentService;

    private User testUser;
    private UserProfile testProfile;
    private AgentAutofillSession testSession;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("student@scholarmate.in");
        testUser.setName("Aadi Student");
        testUser.setMobile("9876543210");

        testProfile = UserProfile.builder()
                .id(10L)
                .user(testUser)
                .category("OBC")
                .domicileState("Maharashtra")
                .familyIncome(150000.0)
                .build();

        testSession = AgentAutofillSession.builder()
                .id(100L)
                .sessionId("session-uuid-1234")
                .user(testUser)
                .portalName("National Scholarship Portal")
                .portalUrl("https://scholarships.gov.in")
                .status("IN_PROGRESS")
                .fieldStates("{}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("initSession creates a new session in progress")
    void testInitSession() {
        when(userRepository.findByEmail("student@scholarmate.in")).thenReturn(Optional.of(testUser));
        when(sessionRepository.save(any(AgentAutofillSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InitAgentSessionRequest req = new InitAgentSessionRequest(null, "NSP Portal", "https://nsp.gov.in");
        AgentSessionResponse response = agentService.initSession("student@scholarmate.in", req);

        assertNotNull(response);
        assertNotNull(response.getSessionId());
        assertEquals("NSP Portal", response.getPortalName());
        assertEquals("IN_PROGRESS", response.getStatus());
    }

    @Test
    @DisplayName("inspectAndSuggest provides field-level suggestions (VS Code style)")
    void testInspectAndSuggest() {
        when(userRepository.findByEmail("student@scholarmate.in")).thenReturn(Optional.of(testUser));
        when(sessionRepository.findBySessionId("session-uuid-1234")).thenReturn(Optional.of(testSession));
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        List<StudentAutofillVaultService.VaultEntry> entries = List.of(
                new StudentAutofillVaultService.VaultEntry("name", "Aadi Student", "USER_ACCOUNT", 1.0, List.of("name", "fullname")),
                new StudentAutofillVaultService.VaultEntry("family_income", "150000", "STUDENT_PROFILE", 0.95, List.of("income", "family_income"))
        );
        when(vaultService.compileVaultEntries(testUser, testProfile)).thenReturn(entries);
        when(sessionRepository.save(any(AgentAutofillSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InspectPortalFieldsRequest request = new InspectPortalFieldsRequest(List.of(
                new PortalFieldDefinition("applicant_name", "Full Name", "text"),
                new PortalFieldDefinition("annual_income", "Family Income", "number"),
                new PortalFieldDefinition("ration_card_no", "Ration Card", "text")
        ));

        AgentSessionResponse response = agentService.inspectAndSuggest("student@scholarmate.in", "session-uuid-1234", request);

        assertNotNull(response);
        assertEquals(3, response.getTotalFields());
        assertEquals(2, response.getFilledFields());
        assertEquals(1, response.getMissingFields());
        assertEquals(67, response.getCompletionPercentage()); // 2 out of 3 = 67%

        List<FieldAutofillSuggestion> suggestions = response.getSuggestions();
        assertEquals(3, suggestions.size());

        // Field 1: Name matched
        FieldAutofillSuggestion s1 = suggestions.get(0);
        assertEquals("applicant_name", s1.getFieldKey());
        assertEquals("Aadi Student", s1.getSuggestedValue());
        assertEquals("AVAILABLE", s1.getStatus());
        assertEquals("PENDING", s1.getUserDecision());

        // Field 2: Income matched
        FieldAutofillSuggestion s2 = suggestions.get(1);
        assertEquals("annual_income", s2.getFieldKey());
        assertEquals("150000", s2.getSuggestedValue());

        // Field 3: Ration card missing
        FieldAutofillSuggestion s3 = suggestions.get(2);
        assertEquals("ration_card_no", s3.getFieldKey());
        assertNull(s3.getSuggestedValue());
        assertEquals("MISSING_IN_PROFILE", s3.getStatus());
    }

    @Test
    @DisplayName("recordDecisions updates accept/reject/edit decisions and recalculates completion")
    void testRecordDecisions() throws Exception {
        when(userRepository.findByEmail("student@scholarmate.in")).thenReturn(Optional.of(testUser));

        FieldAutofillSuggestion existing = FieldAutofillSuggestion.builder()
                .fieldKey("applicant_name")
                .label("Full Name")
                .suggestedValue("Aadi Student")
                .userDecision("PENDING")
                .build();
        testSession.setFieldStates(objectMapper.writeValueAsString(java.util.Map.of("applicant_name", existing)));

        when(sessionRepository.findBySessionId("session-uuid-1234")).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(AgentAutofillSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordDecisionsRequest decReq = new RecordDecisionsRequest(List.of(
                new FieldDecisionItem("applicant_name", "ACCEPTED")
        ));

        AgentSessionResponse response = agentService.recordDecisions("student@scholarmate.in", "session-uuid-1234", decReq);

        assertNotNull(response);
        assertEquals(1, response.getAcceptedFields());
        assertEquals("COMPLETED", response.getStatus()); // 1 out of 1 accepted = 100%
        assertEquals(100, response.getCompletionPercentage());
    }

    @Test
    @DisplayName("pauseSession transitions session to PAUSED state")
    void testPauseSession() {
        when(userRepository.findByEmail("student@scholarmate.in")).thenReturn(Optional.of(testUser));
        when(sessionRepository.findBySessionId("session-uuid-1234")).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(AgentAutofillSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgentSessionResponse response = agentService.pauseSession("student@scholarmate.in", "session-uuid-1234");

        assertNotNull(response);
        assertEquals("PAUSED", response.getStatus());
    }
}
