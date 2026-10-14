package com.scholarmate.repository;

import com.scholarmate.entity.Scholarship;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ScholarshipRepository extends JpaRepository<Scholarship,Long> {
    Optional<Scholarship> findByName(String name);

    List<Scholarship> findByActiveTrue();

    Page<Scholarship> findByActiveTrue(Pageable pageable);

    long countByActiveTrue();

    @Query("select scholarship from Scholarship scholarship " +
            "where scholarship.active = true " +
            "and (:query = '' or " +
            "lower(coalesce(scholarship.name, '')) like lower(concat('%', :query, '%')) or " +
            "lower(coalesce(scholarship.provider, '')) like lower(concat('%', :query, '%')) or " +
            "lower(coalesce(scholarship.category, '')) like lower(concat('%', :query, '%')) or " +
            "lower(coalesce(scholarship.description, '')) like lower(concat('%', :query, '%'))) " +
            "order by scholarship.id desc")
    Page<Scholarship> searchActive(@Param("query") String query, Pageable pageable);

    @Query("select scholarship from Scholarship scholarship " +
            "where lower(trim(scholarship.name)) = lower(trim(:name)) " +
            "order by scholarship.active desc, scholarship.id asc")
    List<Scholarship> findByNormalizedName(@Param("name") String name);

        @Query("select scholarship from Scholarship scholarship " +
            "where lower(trim(scholarship.name)) = lower(trim(:name)) " +
            "and scholarship.source.id = :sourceId " +
            "order by scholarship.active desc, scholarship.id asc")
        List<Scholarship> findByNormalizedNameAndSourceId(
            @Param("name") String name,
            @Param("sourceId") Long sourceId);

}
