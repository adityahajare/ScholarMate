package com.scholarmate.repository;

import com.scholarmate.entity.AgentAutofillSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentAutofillSessionRepository extends JpaRepository<AgentAutofillSession, Long> {

    Optional<AgentAutofillSession> findBySessionId(String sessionId);

    List<AgentAutofillSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<AgentAutofillSession> findByUserIdAndScholarshipId(Long userId, Long scholarshipId);

    Optional<AgentAutofillSession> findByUserIdAndPortalNameAndStatus(Long userId, String portalName, String status);
}
