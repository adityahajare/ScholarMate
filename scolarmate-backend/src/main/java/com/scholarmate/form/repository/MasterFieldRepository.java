package com.scholarmate.form.repository;

import com.scholarmate.form.model.MasterField;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MasterFieldRepository extends JpaRepository<MasterField, Long> {
    Optional<MasterField> findByFieldKey(String fieldKey);
}
