package com.scholarmate.service;

import com.scholarmate.entity.RequiredDocument;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.repository.RequiredDocumentRepository;
import com.scholarmate.repository.ScholarshipRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScholarshipDocumentParser {

    private final RequiredDocumentRepository documentRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipSectionExtractor sectionExtractor;

    public ScholarshipDocumentParser(
            RequiredDocumentRepository documentRepository,
            ScholarshipRepository scholarshipRepository,
            ScholarshipSectionExtractor sectionExtractor) {

        this.documentRepository = documentRepository;
        this.scholarshipRepository = scholarshipRepository;
        this.sectionExtractor = sectionExtractor;
    }

    public List<RequiredDocument> parseAndSave(
            Long scholarshipId,
            String rawText) {

        Scholarship scholarship =
                scholarshipRepository.findById(scholarshipId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Scholarship not found"
                                ));

        Map<String, String> sections =
                sectionExtractor.extractSections(rawText);

        String documents =
                sections.get("DOCUMENTS");

        if (documents == null || documents.isBlank()) {
            throw new RuntimeException(
                    "Documents section not found"
            );
        }

        /*
         * PDF extraction sometimes splits one sentence
         * across multiple lines.
         *
         * Example:
         *
         * PPO/Discharge Certificate/Book
         * (Mandatory for category
         * A to F)
         *
         * Convert it into:
         *
         * PPO/Discharge Certificate/Book
         * (Mandatory for category A to F)
         */

        documents = repairPdfLineBreaks(documents);

        List<RequiredDocument> results =
                new ArrayList<>();

        addDocument(
                results,
                scholarship,
                documents,
                "SERVICE_CERTIFICATE",
                "MANDATORY",
                "Service Certificate"
        );

        addDocument(
                results,
                scholarship,
                documents,
                "MEQ_MARKSHEET",
                "MANDATORY",
                "Mark sheet MEQ"
        );

        addDocument(
                results,
                scholarship,
                documents,
                "PPO_DISCHARGE_CERTIFICATE_BOOK",
                "CONDITIONAL",
                "PPO/Discharge Certificate/Book"
        );

        addDocument(
                results,
                scholarship,
                documents,
                "DISABILITY_CERTIFICATE",
                "CONDITIONAL",
                "Disability Certificate"
        );

        addDocument(
                results,
                scholarship,
                documents,
                "DEATH_CERTIFICATE",
                "CONDITIONAL",
                "Death Certificate"
        );

        addDocument(
                results,
                scholarship,
                documents,
                "GALLANTRY_AWARD_CERTIFICATE",
                "CONDITIONAL",
                "Certificate of Gallantry award"
        );

        addDocument(
                results,
                scholarship,
                documents,
                "BONAFIDE_CERTIFICATE",
                "MANDATORY",
                "Bonafide certificate"
        );

        addDocument(
                results,
                scholarship,
                documents,
                "STATE_GOVERNMENT_CERTIFICATE",
                "CONDITIONAL",
                "State Govt"
        );

        addDocument(
                results,
                scholarship,
                documents,
                "AADHAAR",
                "CONDITIONAL",
                "Aadhaar"
        );

        /*
         * Save parsed documents.
         */

        for (RequiredDocument document : results) {
            documentRepository.save(document);
        }

        return results;
    }

    // =========================================================
    // REPAIR PDF LINE BREAKS
    // =========================================================

    private String repairPdfLineBreaks(String text) {

        if (text == null || text.isBlank()) {
            return text;
        }

        String repaired = text;

        /*
         * category
         * A to F
         *
         * becomes:
         *
         * category A to F
         */

        repaired = repaired.replaceAll(
                "(?i)category\\s*\\n\\s*([A-F])\\s+to\\s+([A-F])",
                "category $1 to $2"
        );

        /*
         * category
         * B & D
         */

        repaired = repaired.replaceAll(
                "(?i)category\\s*\\n\\s*([A-F])\\s*&\\s*([A-F])",
                "category $1 & $2"
        );

        /*
         * category
         * A & C
         */

        repaired = repaired.replaceAll(
                "(?i)category\\s*\\n\\s*([A-F])\\s*&\\s*([A-F])",
                "category $1 & $2"
        );

        /*
         * category
         * E
         */

        repaired = repaired.replaceAll(
                "(?i)category\\s*\\n\\s*([A-F])",
                "category $1"
        );

        /*
         * General whitespace normalization.
         */

        repaired = repaired.replaceAll(
                "[ \\t]+",
                " "
        );

        return repaired.trim();
    }

    // =========================================================
    // ADD DOCUMENT
    // =========================================================

    private void addDocument(
            List<RequiredDocument> results,
            Scholarship scholarship,
            String sourceText,
            String documentType,
            String requirementType,
            String searchText) {

        String lower =
                sourceText.toLowerCase();

        String target =
                searchText.toLowerCase();

        int index =
                lower.indexOf(target);

        if (index == -1) {
            return;
        }

        String evidence =
                extractDocumentEvidence(
                        sourceText,
                        index
                );

        String condition =
                extractCategoryCondition(
                        evidence
                );

        /*
         * Special conditions.
         */

        if (condition == null) {

            condition =
                    extractSpecialCondition(
                            evidence,
                            documentType
                    );
        }

        /*
         * If a condition exists, document is conditional.
         */

        if (condition != null) {
            requirementType = "CONDITIONAL";
        }

        RequiredDocument document =
                new RequiredDocument();

        document.setScholarship(scholarship);
        document.setDocumentType(documentType);
        document.setRequirementType(requirementType);
        document.setCondition(condition);
        document.setEvidence(evidence);

        results.add(document);
    }

    // =========================================================
    // DOCUMENT EVIDENCE
    // =========================================================

    private String extractDocumentEvidence(
            String text,
            int index) {

        int start =
                text.lastIndexOf(
                        "\n",
                        index
                );

        if (start == -1) {
            start = 0;
        } else {
            start++;
        }

        int end =
                text.indexOf(
                        "\n",
                        index
                );

        if (end == -1) {
            end = text.length();
        }

        String line =
                text.substring(
                        start,
                        end
                ).trim();

        /*
         * Sometimes PDF extraction leaves the condition
         * on the next line.
         */

        int nextEnd =
                text.indexOf(
                        "\n",
                        end + 1
                );

        if (nextEnd == -1) {
            nextEnd = text.length();
        }

        String nextLine =
                text.substring(
                        end + 1,
                        nextEnd
                ).trim();

        /*
         * Join when the current line appears incomplete.
         */

        if (!nextLine.isBlank()
                && shouldJoinLines(line, nextLine)) {

            line =
                    line + " " + nextLine;

            /*
             * One more line may contain:
             *
             * A to F
             */

            int thirdEnd =
                    text.indexOf(
                            "\n",
                            nextEnd + 1
                    );

            if (thirdEnd == -1) {
                thirdEnd = text.length();
            }

            String thirdLine =
                    text.substring(
                            nextEnd + 1,
                            thirdEnd
                    ).trim();

            if (!thirdLine.isBlank()
                    && shouldJoinLines(
                    line,
                    thirdLine)) {

                line =
                        line + " " + thirdLine;
            }
        }

        line =
                line.replaceAll(
                        "\\s+",
                        " "
                ).trim();

        return line;
    }

    // =========================================================
    // SHOULD JOIN
    // =========================================================

    private boolean shouldJoinLines(
            String current,
            String next) {

        if (current == null
                || next == null) {

            return false;
        }

        String lower =
                current.toLowerCase();

        String nextLower =
                next.toLowerCase();

        /*
         * Explicit PDF condition split.
         */

        if (lower.contains("mandatory for category")
                || lower.endsWith("category")) {

            return true;
        }

        /*
         * Current line ends with an incomplete word.
         */

        if (lower.endsWith("mandatory")
                || lower.endsWith("for")) {

            return true;
        }

        /*
         * Next line begins with category information.
         */

        if (nextLower.matches(
                "^[a-f](\\s+to\\s+[a-f])?.*")) {

            return true;
        }

        return false;
    }

  

    private String extractCategoryCondition(
            String evidence) {

        if (evidence == null) {
            return null;
        }

        /*
         * A to F
         */

        Matcher aToF =
                Pattern.compile(
                        "(?i)category\\s+A\\s+to\\s+F"
                ).matcher(evidence);

        if (aToF.find()) {
            return "CATEGORY_A_TO_F";
        }

        /*
         * B & D
         */

        Matcher bAndD =
                Pattern.compile(
                        "(?i)category\\s+B\\s*(?:&|and)\\s*D"
                ).matcher(evidence);

        if (bAndD.find()) {
            return "CATEGORY_B_OR_D";
        }

        /*
         * A & C
         */

        Matcher aAndC =
                Pattern.compile(
                        "(?i)category\\s+A\\s*(?:&|and)\\s*C"
                ).matcher(evidence);

        if (aAndC.find()) {
            return "CATEGORY_A_OR_C";
        }

        /*
         * Category E
         */

        Matcher categoryE =
                Pattern.compile(
                        "(?i)category\\s+E"
                ).matcher(evidence);

        if (categoryE.find()) {
            return "CATEGORY_E";
        }

        return null;
    }

    // =========================================================
    // SPECIAL CONDITIONS
    // =========================================================

    private String extractSpecialCondition(
            String evidence,
            String documentType) {

        if (evidence == null) {
            return null;
        }

        String lower =
                evidence.toLowerCase();

        /*
         * State Government certificate.
         */

        if (documentType.equals(
                "STATE_GOVERNMENT_CERTIFICATE")) {

            if (lower.contains("police personnel")
                    || lower.contains("terror")
                    || lower.contains("naxal")) {

                return "STATE_POLICE_TERROR_OR_NAXAL_VIOLENCE";
            }
        }

        /*
         * Aadhaar.
         *
         * Aadhaar instructions are not automatically
         * a scholarship eligibility condition.
         */

        if (documentType.equals("AADHAAR")) {

            return "AADHAAR_INSTRUCTIONS";
        }

        return null;
    }
}