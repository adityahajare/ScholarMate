package com.scholarmate.service;

import com.scholarmate.dto.StudentVaultResponse;
import com.scholarmate.entity.*;
import com.scholarmate.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StudentAutofillVaultService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final EducationRecordRepository educationRecordRepository;
    private final ProfileAddressRepository addressRepository;
    private final FamilyMemberRepository familyMemberRepository;

    public StudentAutofillVaultService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            EducationRecordRepository educationRecordRepository,
            ProfileAddressRepository addressRepository,
            FamilyMemberRepository familyMemberRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.educationRecordRepository = educationRecordRepository;
        this.addressRepository = addressRepository;
        this.familyMemberRepository = familyMemberRepository;
    }

    public record VaultEntry(
            String canonicalKey,
            String value,
            String source,
            double confidence,
            List<String> matchKeywords
    ) {}

    @Transactional(readOnly = true)
    public StudentVaultResponse getVaultResponse(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
        UserProfile profile = profileOpt.orElse(null);

        Map<String, Object> identity = new LinkedHashMap<>();
        identity.put("fullName", user.getName());
        identity.put("email", user.getEmail());
        identity.put("mobile", user.getMobile());
        identity.put("educationType", user.getEducationType());
        identity.put("educationLevel", user.getEducationLevel());

        Map<String, Object> academics = new LinkedHashMap<>();
        Map<String, Object> address = new LinkedHashMap<>();
        Map<String, Object> family = new LinkedHashMap<>();
        Map<String, Object> bank = new LinkedHashMap<>();
        Map<String, Object> ocrExtracted = new LinkedHashMap<>();

        if (profile != null) {
            identity.put("gender", profile.getGender());
            identity.put("age", profile.getAge());
            identity.put("category", profile.getCategory());
            identity.put("state", profile.getState());
            identity.put("domicileState", profile.getDomicileState());

            academics.put("currentCourse", profile.getCourse());
            academics.put("college", profile.getCollege());
            academics.put("cgpa", profile.getCgpa());
            academics.put("yearOfStudy", profile.getYearOfStudy());

            family.put("familyIncome", profile.getFamilyIncome());

            // Pull extended bank details if available in profileData
            Map<String, Object> pd = profile.getProfileData();
            if (pd != null) {
                if (pd.containsKey("bankAccountNumber")) bank.put("accountNumber", pd.get("bankAccountNumber"));
                if (pd.containsKey("bankIfsc")) bank.put("ifscCode", pd.get("bankIfsc"));
                if (pd.containsKey("bankName")) bank.put("bankName", pd.get("bankName"));
                if (pd.containsKey("bankBranch")) bank.put("branchName", pd.get("bankBranch"));
                if (pd.containsKey("aadhaarNumber")) identity.put("aadhaarNumber", pd.get("aadhaarNumber"));
                if (pd.containsKey("religion")) identity.put("religion", pd.get("religion"));
                if (pd.containsKey("disability")) identity.put("disability", pd.get("disability"));
            }

            // Addresses
            List<ProfileAddress> addresses = addressRepository.findByUserProfileIdOrderByIdAsc(profile.getId());
            for (ProfileAddress a : addresses) {
                String prefix = a.getAddressType() != null ? a.getAddressType().toLowerCase() : "address";
                address.put(prefix + "_line1", a.getLine1());
                address.put(prefix + "_district", a.getDistrict());
                address.put(prefix + "_state", a.getState());
                address.put(prefix + "_pincode", a.getPinCode());
            }

            // Family members
            List<FamilyMember> members = familyMemberRepository.findByUserProfileIdOrderByIdAsc(profile.getId());
            for (FamilyMember m : members) {
                String rel = m.getRelationship() != null ? m.getRelationship().toLowerCase() : "family";
                family.put(rel + "_name", m.getName());
                family.put(rel + "_occupation", m.getOccupation());
                if (m.getAnnualIncome() != null) family.put(rel + "_income", m.getAnnualIncome());
            }

            // Education records
            List<EducationRecord> records = educationRecordRepository.findByUserProfileOrderBySortOrderAscIdAsc(profile);
            for (EducationRecord er : records) {
                String lvl = er.getLevel() != null ? er.getLevel().toLowerCase() : "edu";
                academics.put(lvl + "_board", er.getBoardOrUniversity());
                academics.put(lvl + "_institution", er.getInstitution());
                academics.put(lvl + "_passingYear", er.getPassingYear());
                academics.put(lvl + "_percentage", er.getPercentage());
                academics.put(lvl + "_stream", er.getStream());
            }
        }

        int readiness = profile != null ? profile.calculateCompletionPercentage() : 25;

        return StudentVaultResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .identity(identity)
                .academics(academics)
                .address(address)
                .family(family)
                .bank(bank)
                .ocrExtracted(ocrExtracted)
                .readinessScore(readiness)
                .build();
    }

    @Transactional(readOnly = true)
    public List<VaultEntry> compileVaultEntries(User user, UserProfile profile) {
        List<VaultEntry> entries = new ArrayList<>();

        // Name
        if (user.getName() != null && !user.getName().isBlank()) {
            entries.add(new VaultEntry("name", user.getName(), "USER_ACCOUNT", 1.0,
                    List.of("name", "fullname", "full_name", "applicant_name", "student_name", "candidate_name", "studentname")));
        }
        // Email
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            entries.add(new VaultEntry("email", user.getEmail(), "USER_ACCOUNT", 1.0,
                    List.of("email", "e_mail", "email_id", "emailid", "applicant_email")));
        }
        // Mobile
        if (user.getMobile() != null && !user.getMobile().isBlank()) {
            entries.add(new VaultEntry("mobile", user.getMobile(), "USER_ACCOUNT", 1.0,
                    List.of("mobile", "phone", "contact", "mobile_number", "contact_number", "phone_number", "phonenumber")));
        }

        if (profile != null) {
            // Gender
            if (profile.getGender() != null && !profile.getGender().isBlank()) {
                entries.add(new VaultEntry("gender", profile.getGender(), "STUDENT_PROFILE", 1.0,
                        List.of("gender", "sex")));
            }
            // Category / Caste
            if (profile.getCategory() != null && !profile.getCategory().isBlank()) {
                entries.add(new VaultEntry("category", profile.getCategory(), "STUDENT_PROFILE", 1.0,
                        List.of("category", "caste", "social_category", "community", "reservation_category")));
            }
            // State of Domicile
            String state = profile.getDomicileState() != null ? profile.getDomicileState() : profile.getState();
            if (state != null && !state.isBlank()) {
                entries.add(new VaultEntry("domicile_state", state, "STUDENT_PROFILE", 1.0,
                        List.of("domicile", "domicile_state", "state_of_domicile", "native_state", "state")));
            }
            // Family Income
            if (profile.getFamilyIncome() != null) {
                String incomeStr = String.valueOf(profile.getFamilyIncome().longValue());
                entries.add(new VaultEntry("family_income", incomeStr, "STUDENT_PROFILE", 0.95,
                        List.of("income", "family_income", "annual_income", "parent_income", "annual_family_income", "gross_income")));
            }
            // Current College / Institute
            if (profile.getCollege() != null && !profile.getCollege().isBlank()) {
                entries.add(new VaultEntry("college", profile.getCollege(), "STUDENT_PROFILE", 1.0,
                        List.of("college", "institution", "institute", "school", "college_name", "university_name")));
            }
            // Current Course
            if (profile.getCourse() != null && !profile.getCourse().isBlank()) {
                entries.add(new VaultEntry("course", profile.getCourse(), "STUDENT_PROFILE", 1.0,
                        List.of("course", "program", "degree", "branch", "current_course", "course_name")));
            }
            // CGPA / Marks
            if (profile.getCgpa() != null) {
                entries.add(new VaultEntry("cgpa", String.valueOf(profile.getCgpa()), "STUDENT_PROFILE", 1.0,
                        List.of("cgpa", "gpa", "grade_point", "marks_percentage")));
            }

            // Addresses
            List<ProfileAddress> addresses = addressRepository.findByUserProfileIdOrderByIdAsc(profile.getId());
            for (ProfileAddress a : addresses) {
                if (a.getPinCode() != null && !a.getPinCode().isBlank()) {
                    entries.add(new VaultEntry("pincode", a.getPinCode(), "PROFILE_ADDRESS", 1.0,
                            List.of("pincode", "pin_code", "postal_code", "zip", "zip_code")));
                }
                if (a.getDistrict() != null && !a.getDistrict().isBlank()) {
                    entries.add(new VaultEntry("district", a.getDistrict(), "PROFILE_ADDRESS", 1.0,
                            List.of("district", "city", "town")));
                }
                if (a.getLine1() != null && !a.getLine1().isBlank()) {
                    entries.add(new VaultEntry("address_line1", a.getLine1(), "PROFILE_ADDRESS", 1.0,
                            List.of("address", "address_line1", "residential_address", "permanent_address", "street")));
                }
            }

            // Education Records (10th, 12th)
            List<EducationRecord> records = educationRecordRepository.findByUserProfileOrderBySortOrderAscIdAsc(profile);
            for (EducationRecord er : records) {
                String level = er.getLevel() != null ? er.getLevel().toUpperCase() : "";
                if (level.contains("10") || level.contains("CLASS_10") || level.contains("SSC")) {
                    if (er.getPercentage() != null) {
                        entries.add(new VaultEntry("class_10_percentage", String.valueOf(er.getPercentage()), "EDUCATION_RECORD", 1.0,
                                List.of("10th_percentage", "class_10_marks", "ssc_percentage", "tenth_percentage", "10th_marks", "matric_percentage")));
                    }
                    if (er.getBoardOrUniversity() != null) {
                        entries.add(new VaultEntry("class_10_board", er.getBoardOrUniversity(), "EDUCATION_RECORD", 1.0,
                                List.of("10th_board", "ssc_board", "class_10_board", "matric_board")));
                    }
                    if (er.getPassingYear() != null) {
                        entries.add(new VaultEntry("class_10_passing_year", String.valueOf(er.getPassingYear()), "EDUCATION_RECORD", 1.0,
                                List.of("10th_year", "class_10_year", "year_of_passing_10th", "ssc_year")));
                    }
                } else if (level.contains("12") || level.contains("CLASS_12") || level.contains("HSC")) {
                    if (er.getPercentage() != null) {
                        entries.add(new VaultEntry("class_12_percentage", String.valueOf(er.getPercentage()), "EDUCATION_RECORD", 1.0,
                                List.of("12th_percentage", "class_12_marks", "hsc_percentage", "twelfth_percentage", "12th_marks")));
                    }
                    if (er.getBoardOrUniversity() != null) {
                        entries.add(new VaultEntry("class_12_board", er.getBoardOrUniversity(), "EDUCATION_RECORD", 1.0,
                                List.of("12th_board", "hsc_board", "class_12_board", "intermediate_board")));
                    }
                    if (er.getPassingYear() != null) {
                        entries.add(new VaultEntry("class_12_passing_year", String.valueOf(er.getPassingYear()), "EDUCATION_RECORD", 1.0,
                                List.of("12th_year", "class_12_year", "year_of_passing_12th", "hsc_year")));
                    }
                }
            }

            // Extended profileData (e.g. Bank Account, IFSC)
            Map<String, Object> pd = profile.getProfileData();
            if (pd != null) {
                if (pd.get("bankAccountNumber") != null) {
                    entries.add(new VaultEntry("bank_account", String.valueOf(pd.get("bankAccountNumber")), "BANK_DETAIL", 0.95,
                            List.of("bank_account", "account_number", "account_no", "bank_account_number", "acc_no")));
                }
                if (pd.get("bankIfsc") != null) {
                    entries.add(new VaultEntry("bank_ifsc", String.valueOf(pd.get("bankIfsc")), "BANK_DETAIL", 0.95,
                            List.of("ifsc", "ifsc_code", "bank_ifsc", "branch_ifsc")));
                }
                if (pd.get("bankName") != null) {
                    entries.add(new VaultEntry("bank_name", String.valueOf(pd.get("bankName")), "BANK_DETAIL", 0.95,
                            List.of("bank_name", "bank", "name_of_bank")));
                }
                if (pd.get("aadhaarNumber") != null) {
                    entries.add(new VaultEntry("aadhaar", String.valueOf(pd.get("aadhaarNumber")), "IDENTITY_DOCUMENT", 0.98,
                            List.of("aadhaar", "aadhaar_number", "uid", "aadhar", "national_id")));
                }
            }
        }

        return entries;
    }
}
