package com.scholarmate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.scholarmate.dto.CreateNoteRequest;
import com.scholarmate.dto.NoteDto;
import com.scholarmate.service.NoteService;

@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {
    private final NoteService noteService;
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<NoteDto> create(@RequestBody CreateNoteRequest request) {
        NoteDto created = noteService.createNote(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<java.util.List<NoteDto>> list() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }
}

