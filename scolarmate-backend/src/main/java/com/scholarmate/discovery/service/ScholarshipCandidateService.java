package com.scholarmate.discovery.service;

import com.scholarmate.discovery.dto.CandidateAnalysisResult;
import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.form.model.ScholarshipRequirement;
import com.scholarmate.form.repository.ScholarshipRequirementRepository;
import com.scholarmate.repository.ScholarshipRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ScholarshipCandidateService {

    private final ScholarshipRequirementRepository requirementRepo;
    private final ScholarshipRepository scholarshipRepo;

    public ScholarshipCandidateService(ScholarshipRequirementRepository requirementRepo,
                                       ScholarshipRepository scholarshipRepo) {
        super();
        this.requirementRepo = requirementRepo;
        this.scholarshipRepo = scholarshipRepo;
    }

    public CandidateAnalysisResult analyzeCandidates(UserProfile profile, Map<String, Object> sessionAnswers) {
        // 1. Gather all known fields from UserProfile and sessionAnswers
        Map<String, Object> allKnown = new HashMap<>();
        if (profile != null) {
            extractProfileFields(profile, allKnown);
            if (profile.getProfileData() != null) {
                allKnown.putAll(profile.getProfileData());
            }
        }
        if (sessionAnswers != null) {
            allKnown.putAll(sessionAnswers);
        }

        // 2. Load all verified scholarship requirements
        List<ScholarshipRequirement> allRequirements = requirementRepo.findAll();

        // Group requirements by scholarship_key
        Map<String, List<ScholarshipRequirement>> reqsByScheme = new HashMap<>();
        Map<String, String> schNames = new HashMap<>();

        for (ScholarshipRequirement req : allRequirements) {
            reqsByScheme.computeIfAbsent(req.getScholarshipKey(), k -> new ArrayList<>()).add(req);
            schNames.putIfAbsent(req.getScholarshipKey(), req.getScholarshipName());
        }

        List<String> candidateKeys = new ArrayList<>();
        Map<String, List<String>> fieldToSchemeMap = new HashMap<>();
        Set<String> missingFieldsSet = new LinkedHashSet<>();

        // 3. Evaluate each scheme for candidacy
        for (Map.Entry<String, List<ScholarshipRequirement>> entry : reqsByScheme.entrySet()) {
            String schKey = entry.getKey();
            List<ScholarshipRequirement> requirements = entry.getValue();

            boolean isEliminated = false;
            List<String> schemeMissingFields = new ArrayList<>();

            for (ScholarshipRequirement req : requirements) {
                String fieldKey = req.getFieldKey();
                Object studentVal = allKnown.get(fieldKey);

                if (studentVal == null || studentVal.toString().trim().isEmpty()) {
                    // Field is missing -> scheme remains a candidate, but record the missing field
                    schemeMissingFields.add(fieldKey);
                    continue;
                }

                // If field is known, check if it directly violates a mandatory requirement
                if (req.getMandatory()) {
                    boolean satisfies = evaluateRule(req.getOperator(), req.getExpectedValue(), studentVal.toString());
                    if (!satisfies) {
                        isEliminated = true;
                        break;
                    }
                }
            }

            if (!isEliminated) {
                candidateKeys.add(schKey);
                for (String mf : schemeMissingFields) {
                    missingFieldsSet.add(mf);
                    fieldToSchemeMap.computeIfAbsent(mf, k -> new ArrayList<>()).add(schKey);
                }
            }
        }

        // Also, if candidateKeys has scholarships from DB that don't have explicit requirements yet,
        // we keep standard discovery fields (income, domicile, category) active
        if (missingFieldsSet.isEmpty() && !candidateKeys.isEmpty()) {
            // Check if income is known; if not, ask
            if (!allKnown.containsKey("annual_family_income")) missingFieldsSet.add("annual_family_income");
            if (!allKnown.containsKey("domicile_state")) missingFieldsSet.add("domicile_state");
            if (!allKnown.containsKey("caste_category")) missingFieldsSet.add("caste_category");
        }

        return CandidateAnalysisResult.builder()
                .candidateScholarshipKeys(candidateKeys)
                .missingFieldKeys(new ArrayList<>(missingFieldsSet))
                .requirementFieldMap(fieldToSchemeMap)
                .scholarshipNames(schNames)
                .knownFieldKeys(allKnown.keySet())
                .build();
    }

    private boolean evaluateRule(String operator, String expected, String actual) {
        if (expected == null || actual == null) return false;
        String op = operator.toUpperCase().trim();
        String act = actual.trim();
        String exp = expected.trim();

        try {
            return switch (op) {
                case "EQUALS", "=" -> act.equalsIgnoreCase(exp);
                case "NOT_EQUALS", "!=" -> !act.equalsIgnoreCase(exp);
                case "IN" -> {
                    // expected could be JSON array or comma separated
                    String clean = exp.replaceAll("[\\[\\]\"']", "");
                    List<String> items = Arrays.stream(clean.split(","))
                            .map(String::trim)
                            .toList();
                    yield items.stream().anyMatch(i -> i.equalsIgnoreCase(act));
                }
                case "NOT_IN" -> {
                    String clean = exp.replaceAll("[\\[\\]\"']", "");
                    List<String> items = Arrays.stream(clean.split(","))
                            .map(String::trim)
                            .toList();
                    yield items.stream().noneMatch(i -> i.equalsIgnoreCase(act));
                }
                case "LESS_THAN_OR_EQUAL", "<=" -> Double.parseDouble(act) <= Double.parseDouble(exp);
                case "GREATER_THAN_OR_EQUAL", ">=" -> Double.parseDouble(act) >= Double.parseDouble(exp);
                case "LESS_THAN", "<" -> Double.parseDouble(act) < Double.parseDouble(exp);
                case "GREATER_THAN", ">" -> Double.parseDouble(act) > Double.parseDouble(exp);
                default -> true;
            };
        } catch (NumberFormatException e) {
            log.debug("Numeric comparison failed between '{}' and '{}' for op '{}'", act, exp, op);
            return false;
        }
    }

    private void extractProfileFields(UserProfile profile, Map<String, Object> map) {
        if (profile.getFullName() != null) map.put("full_name", profile.getFullName());
        if (profile.getEducationStage() != null) map.put("education_level", profile.getEducationStage());
        if (profile.getCourse() != null) map.put("course_name", profile.getCourse());
        if (profile.getCollege() != null) map.put("institution_name", profile.getCollege());
        if (profile.getState() != null) map.put("permanent_state", profile.getState());
        if (profile.getDomicileState() != null) map.put("domicile_state", profile.getDomicileState());
        if (profile.getCategory() != null) map.put("caste_category", profile.getCategory());
        if (profile.getFamilyIncome() != null) map.put("annual_family_income", profile.getFamilyIncome());
        if (profile.getCgpa() != null) map.put("previous_percentage", profile.getCgpa() * 9.5); // standard CGPA to %
        if (profile.getYearOfStudy() != null) map.put("current_year", String.valueOf(profile.getYearOfStudy()));
        if (profile.getGender() != null) map.put("gender", profile.getGender());
    }
}
