package com.scholarmate.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNoteRequest {
    private String title;
    private String content; // optional if URL is provided
    private String url;    // optional external source to scrape
}

