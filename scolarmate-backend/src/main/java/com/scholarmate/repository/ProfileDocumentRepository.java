package com.scholarmate.repository;

import com.scholarmate.entity.ProfileDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProfileDocumentRepository extends JpaRepository<ProfileDocument, Long> {
    List<ProfileDocument> findByUserProfileIdOrderByUploadedAtDesc(Long profileId);
    Optional<ProfileDocument> findByIdAndUserProfileId(Long id, Long profileId);
}
