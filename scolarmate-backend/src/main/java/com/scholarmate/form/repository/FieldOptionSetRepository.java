package com.scholarmate.form.repository;

import com.scholarmate.form.model.FieldOptionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FieldOptionSetRepository extends JpaRepository<FieldOptionSet, Long> {
    Optional<FieldOptionSet> findByOptionKey(String optionKey);
}
