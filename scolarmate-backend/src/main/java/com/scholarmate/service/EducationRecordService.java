package com.scholarmate.service;

import com.scholarmate.entity.EducationRecord;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.EducationRecordRepository;
import com.scholarmate.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EducationRecordService {
    private final EducationRecordRepository repository;
    private final UserProfileRepository profileRepository;

    public EducationRecordService(EducationRecordRepository repository, UserProfileRepository profileRepository) {
        this.repository = repository;
        this.profileRepository = profileRepository;
    }

    public List<EducationRecord> listMine(String email) {
        return repository.findByUserProfileOrderBySortOrderAscIdAsc(profile(email));
    }

    @Transactional
    public EducationRecord createMine(String email, EducationRecord input) {
        UserProfile owner = profile(email);
        input.setId(null);
        input.setUserProfile(owner);
        validate(input);
        return repository.save(input);
    }

    @Transactional
    public EducationRecord updateMine(String email, Long id, EducationRecord input) {
        UserProfile owner = profile(email);
        EducationRecord current = repository.findByIdAndUserProfile(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("Education record not found."));
        current.setLevel(input.getLevel());
        current.setBoardOrUniversity(input.getBoardOrUniversity());
        current.setStream(input.getStream());
        current.setInstitution(input.getInstitution());
        current.setQualifyingExamination(input.getQualifyingExamination());
        current.setPassingYear(input.getPassingYear());
        current.setObtainedMarks(input.getObtainedMarks());
        current.setMaximumMarks(input.getMaximumMarks());
        current.setPercentage(input.computePercentage());
        current.setResult(input.getResult());
        current.setAttempts(input.getAttempts());
        current.setGapYears(input.getGapYears());
        current.setCompletionStatus(input.getCompletionStatus());
        current.setStartDate(input.getStartDate());
        current.setEndDate(input.getEndDate());
        current.setCurrentSemester(input.getCurrentSemester());
        current.setCourseDurationYears(input.getCourseDurationYears());
        current.setAdmissionDate(input.getAdmissionDate());
        current.setAccreditation(input.getAccreditation());
        current.setAdmissionType(input.getAdmissionType());
        current.setAdmissionRound(input.getAdmissionRound());
        current.setAllotmentNumber(input.getAllotmentNumber());
        current.setSeatCategory(input.getSeatCategory());
        current.setAdmissionConfirmed(input.getAdmissionConfirmed());
        current.setBacklogs(input.getBacklogs());
        current.setAttendancePercent(input.getAttendancePercent());
        current.setPromotionStatus(input.getPromotionStatus());
        current.setRenewalPerformance(input.getRenewalPerformance());
        current.setCurrentRecord(input.getCurrentRecord());
        current.setSortOrder(input.getSortOrder());
        validate(current);
        return repository.save(current);
    }

    @Transactional
    public void deleteMine(String email, Long id) {
        UserProfile owner = profile(email);
        EducationRecord record = repository.findByIdAndUserProfile(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("Education record not found."));
        repository.delete(record);
    }

    private UserProfile profile(String email) {
        return profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for authenticated user."));
    }

    private void validate(EducationRecord record) {
        if (record.getLevel() == null || record.getLevel().isBlank()) {
            throw new IllegalArgumentException("Education level is required.");
        }
        if (record.getObtainedMarks() != null && record.getMaximumMarks() != null
                && (record.getObtainedMarks() < 0 || record.getMaximumMarks() <= 0
                || record.getObtainedMarks() > record.getMaximumMarks())) {
            throw new IllegalArgumentException("Obtained marks must be between zero and maximum marks.");
        }
        record.setPercentage(record.computePercentage());
    }
}
