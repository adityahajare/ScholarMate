package com.scholarmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.scholarmate.entity.NoteEntity;

public interface NoteRepository extends JpaRepository<NoteEntity, Long> {
    // Spring Data JPA provides basic CRUD methods out of the box
}
