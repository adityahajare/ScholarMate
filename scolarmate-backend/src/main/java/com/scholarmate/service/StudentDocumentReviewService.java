package com.scholarmate.service;

import com.scholarmate.dto.DocumentScholarshipComparison;
import com.scholarmate.dto.ExtractedStudentProfile;
import com.scholarmate.dto.ProfileDocumentReviewResponse;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.EligibilityCriteriaRepository;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.scraper.PdfScholarshipExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentDocumentReviewService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final PdfScholarshipExtractor documentExtractor;
    private final StudentProfileDocumentParser profileParser;
    private final ScholarshipRepository scholarshipRepository;
    private final EligibilityCriteriaRepository criteriaRepository;
    private final EligibilityEvaluationService evaluationService;

    public StudentDocumentReviewService(
            PdfScholarshipExtractor documentExtractor,
            StudentProfileDocumentParser profileParser,
            ScholarshipRepository scholarshipRepository,
            EligibilityCriteriaRepository criteriaRepository,
            EligibilityEvaluationService evaluationService) {
        this.documentExtractor = documentExtractor;
        this.profileParser = profileParser;
        this.scholarshipRepository = scholarshipRepository;
        this.criteriaRepository = criteriaRepository;
        this.evaluationService = evaluationService;
    }

    public ProfileDocumentReviewResponse reviewText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Document text is required.");
        }
        if (text.length() > 100_000) {
            throw new IllegalArgumentException("Document text must be no larger than 100,000 characters.");
        }
        return review("PASTED_TEXT", text);
    }

    public ProfileDocumentReviewResponse reviewFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a non-empty PDF, PNG, or JPEG file.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("The maximum document size is 10 MB.");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read the uploaded document.", e);
        }

        String extractedText;
        String inputMethod;
        if (contentType.equals("application/pdf")) {
            extractedText = documentExtractor.extractText(bytes);
            inputMethod = "PDF_OCR_OR_TEXT";
        } else if (contentType.equals("image/png") || contentType.equals("image/jpeg")) {
            extractedText = documentExtractor.extractImageText(bytes);
            inputMethod = "IMAGE_OCR";
        } else {
            throw new IllegalArgumentException("Only PDF, PNG, and JPEG documents are supported.");
        }

        return review(inputMethod, extractedText);
    }

    private ProfileDocumentReviewResponse review(String inputMethod, String extractedText) {
        ExtractedStudentProfile suggestion = profileParser.parse(extractedText);
        UserProfile candidateProfile = UserProfile.builder()
                .fullName(suggestion.fullName())
                .course(suggestion.course())
                .college(suggestion.college())
                .cgpa(suggestion.cgpa())
                .familyIncome(suggestion.familyIncome())
                .category(suggestion.category())
                .state(suggestion.state())
                .yearOfStudy(suggestion.yearOfStudy())
                .build();

        List<DocumentScholarshipComparison> comparisons = scholarshipRepository.findAll().stream()
                .filter(scholarship -> Boolean.TRUE.equals(scholarship.getActive()))
                .map(scholarship -> compare(candidateProfile, scholarship))
                .toList();

        List<String> warnings = new ArrayList<>();
        if (suggestion.cgpa() == null) warnings.add("CGPA was not confidently found; comparisons may be UNKNOWN.");
        if (suggestion.familyIncome() == null) warnings.add("Family income was not confidently found; comparisons may be UNKNOWN.");
        if (suggestion.course() == null) warnings.add("Course was not confidently found; comparisons may be UNKNOWN.");
        if (suggestion.category() == null) warnings.add("Category was not confidently found; comparisons may be UNKNOWN.");
        if (suggestion.state() == null) warnings.add("State was not confidently found; comparisons may be UNKNOWN.");
        if (comparisons.isEmpty()) warnings.add("No active scholarships are available to compare.");

        return new ProfileDocumentReviewResponse(
                inputMethod,
                extractedText,
                suggestion,
                comparisons,
                warnings
        );
    }

    private DocumentScholarshipComparison compare(UserProfile profile, Scholarship scholarship) {
        var assessment = evaluationService.evaluate(
                profile,
                scholarship,
                criteriaRepository.findByScholarshipId(scholarship.getId())
        );
        return new DocumentScholarshipComparison(
                scholarship.getId(),
                scholarship.getName(),
                assessment.getStatus(),
                assessment.getMatchedCriteria(),
                assessment.getUnknownCriteria(),
                assessment.getFailedCriteria(),
                assessment.getEvidence()
        );
    }
}