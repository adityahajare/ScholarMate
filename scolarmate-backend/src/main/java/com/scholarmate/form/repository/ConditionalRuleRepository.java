package com.scholarmate.form.repository;

import com.scholarmate.form.model.ConditionalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConditionalRuleRepository extends JpaRepository<ConditionalRule, Long> {
    List<ConditionalRule> findAllByFieldKeyIn(List<String> fieldKeys);
}
