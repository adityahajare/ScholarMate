package com.scholarmate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarmate.dto.*;
import com.scholarmate.entity.AgentAutofillSession;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.AgentAutofillSessionRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AgentAutofillService {

    private static final Set<String> STOP_WORDS = Set.of(
            "number", "no", "txt", "input", "field", "enter", "your", "the", "for", "and", "card", "code", "details"
    );

    private final AgentAutofillSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final StudentAutofillVaultService vaultService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentAutofillService(
            AgentAutofillSessionRepository sessionRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            ScholarshipRepository scholarshipRepository,
            StudentAutofillVaultService vaultService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.scholarshipRepository = scholarshipRepository;
        this.vaultService = vaultService;
    }

    @Transactional
    public AgentSessionResponse initSession(String userEmail, InitAgentSessionRequest request) {
        User user = findUserByEmail(userEmail);
        Scholarship scholarship = null;
        if (request.scholarshipId() != null) {
            scholarship = scholarshipRepository.findById(request.scholarshipId()).orElse(null);
        }

        String sessionId = UUID.randomUUID().toString();

        AgentAutofillSession session = AgentAutofillSession.builder()
                .sessionId(sessionId)
                .user(user)
                .scholarship(scholarship)
                .portalName(request.portalName())
                .portalUrl(request.portalUrl())
                .status("IN_PROGRESS")
                .fieldStates("{}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        AgentAutofillSession saved = sessionRepository.save(session);
        return toDto(saved, Collections.emptyList());
    }

    @Transactional
    public AgentSessionResponse inspectAndSuggest(String userEmail, String sessionId, InspectPortalFieldsRequest request) {
        User user = findUserByEmail(userEmail);
        AgentAutofillSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this autofill session.");
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        List<StudentAutofillVaultService.VaultEntry> vaultEntries = vaultService.compileVaultEntries(user, profile);

        Map<String, FieldAutofillSuggestion> existingMap = parseFieldStates(session.getFieldStates());
        List<FieldAutofillSuggestion> currentSuggestions = new ArrayList<>();

        int total = request.fields().size();
        int filled = 0;
        int missing = 0;

        for (PortalFieldDefinition field : request.fields()) {
            String key = field.fieldKey();
            FieldAutofillSuggestion existing = existingMap.get(key);

            // If user already took an explicit decision on this field, retain it
            if (existing != null && existing.getUserDecision() != null && !"PENDING".equalsIgnoreCase(existing.getUserDecision())) {
                currentSuggestions.add(existing);
                if ("ACCEPTED".equalsIgnoreCase(existing.getUserDecision()) || "EDITED".equalsIgnoreCase(existing.getUserDecision())) {
                    filled++;
                } else if ("REJECTED".equalsIgnoreCase(existing.getUserDecision())) {
                    missing++;
                }
                continue;
            }

            // Find matching vault entry
            StudentAutofillVaultService.VaultEntry match = findBestMatch(field, vaultEntries);

            FieldAutofillSuggestion suggestion;
            if (match != null) {
                suggestion = FieldAutofillSuggestion.builder()
                        .fieldKey(key)
                        .label(field.label() != null ? field.label() : key)
                        .suggestedValue(match.value())
                        .confidence(match.confidence())
                        .source(match.source())
                        .explanation("Suggested from " + match.source() + " (" + match.canonicalKey() + ")")
                        .status("AVAILABLE")
                        .userDecision("PENDING")
                        .build();
                filled++;
            } else {
                suggestion = FieldAutofillSuggestion.builder()
                        .fieldKey(key)
                        .label(field.label() != null ? field.label() : key)
                        .suggestedValue(null)
                        .confidence(0.0)
                        .source("NONE")
                        .explanation("No matching field found in student profile; manual completion required.")
                        .status("MISSING_IN_PROFILE")
                        .userDecision("PENDING")
                        .build();
                missing++;
            }

            existingMap.put(key, suggestion);
            currentSuggestions.add(suggestion);
        }

        int percentage = total > 0 ? (int) Math.round(((double) filled / total) * 100.0) : 0;

        session.setTotalFields(total);
        session.setFilledFields(filled);
        session.setMissingFields(missing);
        session.setCompletionPercentage(percentage);
        session.setFieldStates(writeFieldStates(existingMap));
        session.setUpdatedAt(LocalDateTime.now());

        AgentAutofillSession updated = sessionRepository.save(session);
        return toDto(updated, currentSuggestions);
    }

    @Transactional
    public AgentSessionResponse recordDecisions(String userEmail, String sessionId, RecordDecisionsRequest request) {
        User user = findUserByEmail(userEmail);
        AgentAutofillSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this autofill session.");
        }

        Map<String, FieldAutofillSuggestion> existingMap = parseFieldStates(session.getFieldStates());

        for (FieldDecisionItem item : request.decisions()) {
            FieldAutofillSuggestion suggestion = existingMap.get(item.fieldKey());
            if (suggestion == null) {
                suggestion = FieldAutofillSuggestion.builder()
                        .fieldKey(item.fieldKey())
                        .label(item.fieldKey())
                        .build();
                existingMap.put(item.fieldKey(), suggestion);
            }

            String dec = item.decision() != null ? item.decision().trim().toUpperCase() : "PENDING";
            suggestion.setUserDecision(dec);

            if ("EDITED".equalsIgnoreCase(dec) && item.customValue() != null) {
                suggestion.setUserCustomValue(item.customValue());
                suggestion.setStatus("AVAILABLE");
            } else if ("ACCEPTED".equalsIgnoreCase(dec)) {
                suggestion.setStatus("AVAILABLE");
            } else if ("REJECTED".equalsIgnoreCase(dec)) {
                suggestion.setStatus("REJECTED_BY_USER");
            }
        }

        // Recalculate metrics
        int total = existingMap.size();
        int accepted = 0;
        int rejected = 0;
        int filled = 0;

        for (FieldAutofillSuggestion s : existingMap.values()) {
            if ("ACCEPTED".equalsIgnoreCase(s.getUserDecision())) {
                accepted++;
                filled++;
            } else if ("EDITED".equalsIgnoreCase(s.getUserDecision())) {
                accepted++;
                filled++;
            } else if ("REJECTED".equalsIgnoreCase(s.getUserDecision())) {
                rejected++;
            } else if (s.getSuggestedValue() != null && !s.getSuggestedValue().isBlank()) {
                filled++;
            }
        }

        int missing = Math.max(0, total - filled);
        int percentage = total > 0 ? (int) Math.round(((double) filled / total) * 100.0) : 0;

        session.setTotalFields(total);
        session.setAcceptedFields(accepted);
        session.setRejectedFields(rejected);
        session.setFilledFields(filled);
        session.setMissingFields(missing);
        session.setCompletionPercentage(percentage);

        if (percentage >= 100) {
            session.setStatus("COMPLETED");
        } else if (accepted > 0 || filled > 0) {
            session.setStatus("PARTIALLY_FILLED");
        }

        session.setFieldStates(writeFieldStates(existingMap));
        session.setUpdatedAt(LocalDateTime.now());

        AgentAutofillSession updated = sessionRepository.save(session);
        return toDto(updated, new ArrayList<>(existingMap.values()));
    }

    @Transactional
    public AgentSessionResponse pauseSession(String userEmail, String sessionId) {
        User user = findUserByEmail(userEmail);
        AgentAutofillSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this autofill session.");
        }

        session.setStatus("PAUSED");
        session.setUpdatedAt(LocalDateTime.now());
        AgentAutofillSession updated = sessionRepository.save(session);
        Map<String, FieldAutofillSuggestion> map = parseFieldStates(updated.getFieldStates());
        return toDto(updated, new ArrayList<>(map.values()));
    }

    @Transactional(readOnly = true)
    public AgentSessionResponse getSession(String userEmail, String sessionId) {
        User user = findUserByEmail(userEmail);
        AgentAutofillSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this autofill session.");
        }

        Map<String, FieldAutofillSuggestion> map = parseFieldStates(session.getFieldStates());
        return toDto(session, new ArrayList<>(map.values()));
    }

    @Transactional(readOnly = true)
    public List<AgentSessionResponse> getSessionsForUser(String userEmail) {
        User user = findUserByEmail(userEmail);
        List<AgentAutofillSession> sessions = sessionRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
        List<AgentSessionResponse> result = new ArrayList<>();
        for (AgentAutofillSession s : sessions) {
            Map<String, FieldAutofillSuggestion> map = parseFieldStates(s.getFieldStates());
            result.add(toDto(s, new ArrayList<>(map.values())));
        }
        return result;
    }

    private StudentAutofillVaultService.VaultEntry findBestMatch(
            PortalFieldDefinition field,
            List<StudentAutofillVaultService.VaultEntry> vaultEntries) {

        String raw = (field.fieldKey() + " " + (field.label() != null ? field.label() : "")).toLowerCase();
        String normalized = raw.replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ").trim();
        List<String> rawTokens = Arrays.stream(normalized.split(" "))
                .filter(t -> !t.isBlank() && !STOP_WORDS.contains(t) && t.length() >= 3)
                .toList();

        // 1. Exact phrase/keyword match
        for (StudentAutofillVaultService.VaultEntry entry : vaultEntries) {
            for (String kw : entry.matchKeywords()) {
                if (STOP_WORDS.contains(kw)) continue;
                if (normalized.equals(kw) || normalized.contains(" " + kw + " ") || normalized.startsWith(kw + " ") || normalized.endsWith(" " + kw)) {
                    return entry;
                }
            }
        }

        // 2. Token match against meaningful keywords
        for (String t : rawTokens) {
            for (StudentAutofillVaultService.VaultEntry entry : vaultEntries) {
                for (String kw : entry.matchKeywords()) {
                    if (STOP_WORDS.contains(kw)) continue;
                    if (kw.equals(t) || (t.length() >= 4 && kw.startsWith(t))) {
                        return entry;
                    }
                }
            }
        }

        return null;
    }

    private Map<String, FieldAutofillSuggestion> parseFieldStates(String json) {
        if (json == null || json.isBlank() || "{}".equals(json.trim())) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, FieldAutofillSuggestion>>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private String writeFieldStates(Map<String, FieldAutofillSuggestion> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    private AgentSessionResponse toDto(AgentAutofillSession s, List<FieldAutofillSuggestion> suggestions) {
        List<String> missing = new ArrayList<>();
        for (FieldAutofillSuggestion sug : suggestions) {
            if ("MISSING_IN_PROFILE".equalsIgnoreCase(sug.getStatus()) ||
                    "REJECTED".equalsIgnoreCase(sug.getUserDecision()) ||
                    (sug.getSuggestedValue() == null && sug.getUserCustomValue() == null)) {
                missing.add(sug.getLabel() != null ? sug.getLabel() : sug.getFieldKey());
            }
        }

        return AgentSessionResponse.builder()
                .sessionId(s.getSessionId())
                .scholarshipId(s.getScholarship() != null ? s.getScholarship().getId() : null)
                .scholarshipName(s.getScholarship() != null ? s.getScholarship().getName() : null)
                .portalName(s.getPortalName())
                .portalUrl(s.getPortalUrl())
                .status(s.getStatus())
                .totalFields(s.getTotalFields())
                .filledFields(s.getFilledFields())
                .acceptedFields(s.getAcceptedFields())
                .rejectedFields(s.getRejectedFields())
                .missingFields(s.getMissingFields())
                .completionPercentage(s.getCompletionPercentage())
                .suggestions(suggestions)
                .missingFieldLabels(missing)
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
