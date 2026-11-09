package com.scholarmate.form.repository;

import com.scholarmate.form.model.DiscoverySession;
import com.scholarmate.form.model.DiscoverySessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DiscoverySessionAnswerRepository extends JpaRepository<DiscoverySessionAnswer, Long> {
    List<DiscoverySessionAnswer> findAllBySession(DiscoverySession session);
    Optional<DiscoverySessionAnswer> findBySessionAndFieldKey(DiscoverySession session, String fieldKey);
}
