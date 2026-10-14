package com.scholarmate.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scholarmate.dto.CreateNoteRequest;
import com.scholarmate.dto.NoteDto;
import com.scholarmate.entity.NoteEntity;
import com.scholarmate.repository.NoteRepository;
import com.scholarmate.scraper.HtmlScraper;
import com.scholarmate.ml.MlClientService;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoteService {
    private final NoteRepository noteRepository;
    private final HtmlScraper htmlScraper;
    private final MlClientService mlClientService;

    public NoteService(NoteRepository noteRepository, HtmlScraper htmlScraper, MlClientService mlClientService) {
        this.noteRepository = noteRepository;
        this.htmlScraper = htmlScraper;
        this.mlClientService = mlClientService;
    }

    public NoteDto createNote(CreateNoteRequest request) {
        String content = request.getContent();
        // If a URL is supplied, scrape the page and replace content
        if (request.getUrl() != null && !request.getUrl().isBlank()) {
            content = htmlScraper.fetchContent(request.getUrl());
        }
        // Call ML service to obtain tags (comma‑separated)
        String tags = mlClientService.predictTags(content);
        NoteEntity entity = NoteEntity.builder()
                .title(request.getTitle())
                .content(content)
                .tags(tags)
                .build();
        noteRepository.save(entity);
        return mapToDto(entity);
    }

    @Transactional(readOnly = true)
    public List<NoteDto> getAllNotes() {
        return noteRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private NoteDto mapToDto(NoteEntity e) {
        return NoteDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .content(e.getContent())
                .tags(e.getTags())
                .createdAt(e.getCreatedAt())
                .build();
    }
}

