package com.scholarmate.form.repository;

import com.scholarmate.entity.User;
import com.scholarmate.form.model.DiscoverySession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DiscoverySessionRepository extends JpaRepository<DiscoverySession, Long> {
    Optional<DiscoverySession> findByIdAndUser(Long id, User user);
    List<DiscoverySession> findAllByUserOrderByUpdatedAtDesc(User user);
    Optional<DiscoverySession> findFirstByUserAndSessionStatusOrderByUpdatedAtDesc(User user, String sessionStatus);
}
