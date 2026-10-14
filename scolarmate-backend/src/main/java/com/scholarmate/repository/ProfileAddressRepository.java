package com.scholarmate.repository;

import com.scholarmate.entity.ProfileAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProfileAddressRepository extends JpaRepository<ProfileAddress, Long> {
    List<ProfileAddress> findByUserProfileIdOrderByIdAsc(Long profileId);
    Optional<ProfileAddress> findByIdAndUserProfileId(Long id, Long profileId);
}
