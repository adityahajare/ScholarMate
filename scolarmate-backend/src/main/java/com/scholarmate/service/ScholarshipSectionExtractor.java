package com.scholarmate.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ScholarshipSectionExtractor {

    private final ScholarshipTextNormalizer normalizer;

    public ScholarshipSectionExtractor(
            ScholarshipTextNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public Map<String, String> extractSections(String rawText) {

        String text = normalizer.normalize(rawText);

        Map<String, String> sections =
                new LinkedHashMap<>();

        if (text.isBlank()) {
            return sections;
        }

        String[] lines = text.split("\\n");

        String currentSection = null;

        StringBuilder content =
                new StringBuilder();

        for (String rawLine : lines) {

            String line = rawLine.trim();

            if (line.isBlank()) {

                if (currentSection != null
                        && content.length() > 0) {

                    content.append("\n");
                }

                continue;
            }

            String detectedSection =
                    detectSection(line);

            /*
             * New section found
             */
            if (detectedSection != null) {

                saveSection(
                        sections,
                        currentSection,
                        content
                );

                currentSection =
                        detectedSection;

                content.setLength(0);

                continue;
            }

            /*
             * Normal content
             */
            if (currentSection != null) {

                if (content.length() > 0) {
                    content.append("\n");
                }

                content.append(line);
            }
        }

        /*
         * Save final section
         */
        saveSection(
                sections,
                currentSection,
                content
        );

        return sections;
    }

    // =========================================================
    // SECTION DETECTION
    // =========================================================

    private String detectSection(String line) {

        String normalized =
                normalizeHeading(line);

        String compact =
                normalized.replaceAll(
                        "[^a-z0-9]",
                        ""
                );

        // -----------------------------------------------------
        // ELIGIBILITY
        // -----------------------------------------------------

        if (compact.equals("eligibility")) {
            return "ELIGIBILITY";
        }

        // -----------------------------------------------------
        // DOCUMENTS
        // -----------------------------------------------------

        if (compact.equals("documentsrequired")
                || compact.equals("requireddocuments")) {

            return "DOCUMENTS";
        }

        // -----------------------------------------------------
        // EXCLUSIONS
        // -----------------------------------------------------

        if (compact.equals("exclusions")
                || compact.equals("exclusion")) {

            return "EXCLUSIONS";
        }

        // -----------------------------------------------------
        // PAYMENT
        // -----------------------------------------------------

        if (compact.equals("paymentofscholarship")) {

            return "PAYMENT";
        }

        // -----------------------------------------------------
        // APPLICATION
        // -----------------------------------------------------

        if (compact.equals("howtoapply")) {

            return "APPLICATION";
        }

        // -----------------------------------------------------
        // COURSES
        // -----------------------------------------------------

        if (compact.equals(
                "listofprofessionaltechnicaldegreecoursesapplicableforpmss")) {

            return "COURSES";
        }

        return null;
    }

    // =========================================================
    // HEADING NORMALIZATION
    // =========================================================

    private String normalizeHeading(String line) {

        String normalized =
                line.toLowerCase()
                        .trim();

        /*
         * Remove numbered prefixes.
         *
         * Examples:
         *
         * 10. Exclusions
         * 10) Exclusions
         * 10- Exclusions
         * 10: Exclusions
         */

        normalized =
                normalized.replaceFirst(
                        "^\\d+[.)\\-:]\\s*",
                        ""
                );

        /*
         * Remove alphabetic prefixes.
         *
         * Examples:
         *
         * A) Eligibility
         * B) Documents
         */

        normalized =
                normalized.replaceFirst(
                        "^[a-z][.)\\-:]\\s*",
                        ""
                );

        /*
         * Normalize multiple spaces.
         */

        normalized =
                normalized.replaceAll(
                        "\\s+",
                        " "
                );

        /*
         * PDF/OCR artifacts.
         *
         * Example:
         *
         * How _ to apply
         * How | to apply
         */

        normalized =
                normalized.replaceAll(
                        "\\s*[_|]+\\s*",
                        " "
                );

        /*
         * PDF sometimes produces:
         *
         * Payment of Scholarship:-
         * How to apply:-
         * Exclusions:-
         */

        normalized =
                normalized.replaceAll(
                        "[:;,.]+$",
                        ""
                );

        /*
         * Remove trailing hyphens after heading punctuation.
         */

        normalized =
                normalized.replaceAll(
                        "\\s*-+$",
                        ""
                );

        /*
         * Normalize spaces one final time.
         */

        normalized =
                normalized.replaceAll(
                        "\\s+",
                        " "
                );

        return normalized.trim();
    }

    // =========================================================
    // SAVE SECTION
    // =========================================================

    private void saveSection(
            Map<String, String> sections,
            String sectionName,
            StringBuilder content) {

        if (sectionName == null) {
            return;
        }

        String result =
                content.toString().trim();

        if (!result.isBlank()) {

            sections.put(
                    sectionName,
                    result
            );
        }
    }
}