package com.scholarmate.form.repository;

import com.scholarmate.form.model.FieldOption;
import com.scholarmate.form.model.FieldOptionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FieldOptionRepository extends JpaRepository<FieldOption, Long> {
    List<FieldOption> findAllByOptionSetAndActiveTrueOrderBySortOrderAsc(FieldOptionSet optionSet);
}
