package com.scholarmate.service;

import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.ScholarshipEvidence;
import com.scholarmate.repository.ScholarshipEvidenceRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.scraper.HtmlScholarshipExtractor;
import com.scholarmate.scraper.PdfScholarshipExtractor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScholarshipEvidenceCollector {

    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipEvidenceRepository evidenceRepository;
    private final HtmlScholarshipExtractor htmlExtractor;
    private final PdfScholarshipExtractor pdfExtractor;

    public ScholarshipEvidenceCollector(
            ScholarshipRepository scholarshipRepository,
            ScholarshipEvidenceRepository evidenceRepository,
            HtmlScholarshipExtractor htmlExtractor,
            PdfScholarshipExtractor pdfExtractor) {

        this.scholarshipRepository = scholarshipRepository;
        this.evidenceRepository = evidenceRepository;
        this.htmlExtractor = htmlExtractor;
        this.pdfExtractor = pdfExtractor;
    }

    public String collectForScholarship(Long scholarshipId) {

        Scholarship scholarship =
                scholarshipRepository.findById(scholarshipId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Scholarship not found"));

        int collected = 0;

        Set<String> processedUrls =
                new HashSet<>();

        /*
         * Primary scholarship page
         */
        if (hasUrl(scholarship.getSourceUrl())
                && isSpecificScholarshipPage(
                scholarship.getSourceUrl())) {

            if (processedUrls.add(
                    scholarship.getSourceUrl())) {

                if (collectHtml(
                        scholarship,
                        scholarship.getSourceUrl(),
                        "Scholarship Page")) {

                    collected++;
                }
            }
        }

        /*
         * Specification PDF
         */
        if (hasUrl(
                scholarship.getSpecificationUrl())) {

            if (processedUrls.add(
                    scholarship.getSpecificationUrl())) {

                if (collectPdf(
                        scholarship,
                        scholarship.getSpecificationUrl(),
                        "Specification")) {

                    collected++;
                }
            }
        }

        /*
         * FAQ
         */
        if (hasUrl(
                scholarship.getFaqUrl())) {

            if (processedUrls.add(
                    scholarship.getFaqUrl())) {

                if (isPdf(
                        scholarship.getFaqUrl())) {

                    if (collectPdf(
                            scholarship,
                            scholarship.getFaqUrl(),
                            "FAQ")) {

                        collected++;
                    }

                } else {

                    if (collectHtml(
                            scholarship,
                            scholarship.getFaqUrl(),
                            "FAQ")) {

                        collected++;
                    }
                }
            }
        }

        return "Evidence collection completed. "
                + "Scholarship ID: "
                + scholarshipId
                + ", Evidence collected: "
                + collected;
    }

    public String collectForAllActiveScholarships() {

        List<Scholarship> scholarships =
                scholarshipRepository.findAll()
                        .stream()
                        .filter(s ->
                                Boolean.TRUE.equals(
                                        s.getActive()))
                        .toList();

        int processed = 0;
        int failed = 0;
        int evidenceCount = 0;

        for (Scholarship scholarship : scholarships) {

            try {

                String result =
                        collectForScholarship(
                                scholarship.getId());

                processed++;

                if (result.contains(
                        "Evidence collected: 0")) {

                    failed++;

                } else {

                    evidenceCount++;
                }

            } catch (Exception e) {

                failed++;

                System.out.println(
                        "Evidence collection failed for "
                                + scholarship.getName()
                                + " → "
                                + e.getMessage());
            }
        }

        return "Evidence collection completed. "
                + "Scholarships processed: "
                + processed
                + ", Successful: "
                + evidenceCount
                + ", Failed/No evidence: "
                + failed;
    }

    private boolean collectHtml(
            Scholarship scholarship,
            String url,
            String title) {

        try {

            String text =
                    htmlExtractor.extractText(url);

            return saveEvidence(
                    scholarship,
                    "HTML",
                    url,
                    title,
                    text,
                    "HTML_TEXT",
                    0.90);

        } catch (Exception e) {

            System.out.println(
                    "HTML evidence failed: "
                            + url
                            + " → "
                            + e.getMessage());

            return false;
        }
    }

    private boolean collectPdf(
            Scholarship scholarship,
            String url,
            String title) {

        try {

            String text =
                    pdfExtractor.extractText(url);

            return saveEvidence(
                    scholarship,
                    "PDF",
                    url,
                    title,
                    text,
                    "PDF_TEXT",
                    0.95);

        } catch (Exception e) {

            System.out.println(
                    "PDF evidence failed: "
                            + url
                            + " → "
                            + e.getMessage());

            return false;
        }
    }

    private boolean saveEvidence(
            Scholarship scholarship,
            String evidenceType,
            String url,
            String title,
            String text,
            String extractionMethod,
            double confidence) {

        if (text == null || text.isBlank()) {
            return false;
        }

        String hash =
                generateHash(text);

        List<ScholarshipEvidence> existing =
                evidenceRepository
                        .findByScholarshipId(
                                scholarship.getId());

        boolean alreadyExists =
                existing.stream()
                        .anyMatch(e ->
                                hash.equals(
                                        e.getContentHash()));

        if (alreadyExists) {
            return true;
        }

        ScholarshipEvidence evidence =
                ScholarshipEvidence.builder()
                        .scholarship(scholarship)
                        .evidenceType(evidenceType)
                        .sourceUrl(url)
                        .title(title)
                        .extractedText(text)
                        .extractionMethod(
                                extractionMethod)
                        .confidence(confidence)
                        .retrievedAt(
                                LocalDateTime.now())
                        .contentHash(hash)
                        .build();

        evidenceRepository.save(evidence);

        return true;
    }

    private boolean hasUrl(String url) {

        return url != null
                && !url.isBlank();
    }

    private boolean isPdf(String url) {

        return url.toLowerCase()
                .contains(".pdf");
    }

    private boolean isSpecificScholarshipPage(
            String url) {

        if (url == null) {
            return false;
        }

        /*
         * NSP All-Scholarships is only a listing page.
         * Don't store the same listing page as evidence
         * for every NSP scholarship.
         */

        return !url.contains(
                "/All-Scholarships");
    }

    private String generateHash(String text) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            text.getBytes(
                                    StandardCharsets.UTF_8));

            StringBuilder result =
                    new StringBuilder();

            for (byte b : hash) {

                result.append(
                        String.format(
                                "%02x", b));
            }

            return result.toString();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Hash generation failed");
        }
    }
}