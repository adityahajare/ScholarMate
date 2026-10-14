package com.scholarmate.service;

import org.springframework.stereotype.Service;

@Service
public class ScholarshipTextNormalizer {

    public String normalize(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        String normalized = text;

        normalized = normalized.replaceAll(
                "=====\\s*PAGE\\s*\\d+\\s*=====",
                "\n"
        );

        normalized = normalized.replace("\r\n", "\n");
        normalized = normalized.replace("\r", "\n");

        normalized = normalized.replaceAll(
                "[ \\t]+",
                " "
        );

        // Only spaces/tabs before punctuation.
        // Do NOT use \\s here because it can consume line breaks.
        normalized = normalized.replaceAll(
                "[ \\t]+([,.;:])",
                "$1"
        );

        normalized = normalized.replaceAll(
                "\\n{3,}",
                "\n\n"
        );

        return normalized.trim();
    }
}