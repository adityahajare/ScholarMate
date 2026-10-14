package com.scholarmate.repository;

import com.scholarmate.entity.ProfileDraft;
import com.scholarmate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for {@link ProfileDraft}. Provides a shortcut to fetch a draft by its owner.
 */
public interface ProfileDraftRepository extends JpaRepository<ProfileDraft, Long> {
    Optional<ProfileDraft> findByUser(User user);
}
