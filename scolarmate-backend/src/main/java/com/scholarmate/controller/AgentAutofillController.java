package com.scholarmate.controller;

import com.scholarmate.dto.*;
import com.scholarmate.service.AgentAutofillService;
import com.scholarmate.service.StudentAutofillVaultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
public class AgentAutofillController {

    private final AgentAutofillService agentService;
    private final StudentAutofillVaultService vaultService;

    public AgentAutofillController(
            AgentAutofillService agentService,
            StudentAutofillVaultService vaultService) {
        this.agentService = agentService;
        this.vaultService = vaultService;
    }

    @GetMapping("/vault/me")
    public ResponseEntity<StudentVaultResponse> getMyVault(Authentication authentication) {
        return ResponseEntity.ok(vaultService.getVaultResponse(authentication.getName()));
    }

    @PostMapping("/session/init")
    public ResponseEntity<AgentSessionResponse> initSession(
            Authentication authentication,
            @Valid @RequestBody InitAgentSessionRequest request) {
        AgentSessionResponse response = agentService.initSession(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/session/{sessionId}/inspect")
    public ResponseEntity<AgentSessionResponse> inspectPortalFields(
            Authentication authentication,
            @PathVariable String sessionId,
            @Valid @RequestBody InspectPortalFieldsRequest request) {
        return ResponseEntity.ok(agentService.inspectAndSuggest(authentication.getName(), sessionId, request));
    }

    @PostMapping("/session/{sessionId}/decisions")
    public ResponseEntity<AgentSessionResponse> recordDecisions(
            Authentication authentication,
            @PathVariable String sessionId,
            @Valid @RequestBody RecordDecisionsRequest request) {
        return ResponseEntity.ok(agentService.recordDecisions(authentication.getName(), sessionId, request));
    }

    @PostMapping("/session/{sessionId}/pause")
    public ResponseEntity<AgentSessionResponse> pauseSession(
            Authentication authentication,
            @PathVariable String sessionId) {
        return ResponseEntity.ok(agentService.pauseSession(authentication.getName(), sessionId));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<AgentSessionResponse> getSession(
            Authentication authentication,
            @PathVariable String sessionId) {
        return ResponseEntity.ok(agentService.getSession(authentication.getName(), sessionId));
    }

    @GetMapping("/sessions/me")
    public ResponseEntity<List<AgentSessionResponse>> getMySessions(Authentication authentication) {
        return ResponseEntity.ok(agentService.getSessionsForUser(authentication.getName()));
    }
}
