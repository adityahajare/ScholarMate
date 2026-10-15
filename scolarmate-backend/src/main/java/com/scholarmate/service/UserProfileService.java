package com.scholarmate.service;

import com.scholarmate.dto.ProfileCompletionResponse;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public UserProfileService(
            UserProfileRepository userProfileRepository,
            UserRepository userRepository) {

        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    public UserProfile createProfile(Long userId, UserProfile profile) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        profile.setUser(user);

        return userProfileRepository.save(profile);
    }

    public ProfileCompletionResponse getProfileCompletion(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        List<String> missingFields = new ArrayList<>();
        int totalFields = 14;
        int completedFields = 0;

        if (isFilled(profile.getFullName())) completedFields++; else missingFields.add("fullName");
        if (profile.getAge() != null) completedFields++; else missingFields.add("age");
        if (isFilled(profile.getLifeStage())) completedFields++; else missingFields.add("lifeStage");
        if (isFilled(profile.getOccupation())) completedFields++; else missingFields.add("occupation");
        if (isFilled(profile.getInstitutionType())) completedFields++; else missingFields.add("institutionType");
        if (isFilled(profile.getCourse())) completedFields++; else missingFields.add("course");
        if (isFilled(profile.getCollege())) completedFields++; else missingFields.add("college");
        if (profile.getCgpa() != null) completedFields++; else missingFields.add("cgpa");
        if (profile.getFamilyIncome() != null) completedFields++; else missingFields.add("familyIncome");
        if (isFilled(profile.getCategory())) completedFields++; else missingFields.add("category");
        if (isFilled(profile.getState())) completedFields++; else missingFields.add("state");
        if (profile.getYearOfStudy() != null) completedFields++; else missingFields.add("yearOfStudy");
        if (isFilled(profile.getGender())) completedFields++; else missingFields.add("gender");
        if (isFilled(profile.getDomicileState())) completedFields++; else missingFields.add("domicileState");

        int completionPercentage = (completedFields * 100) / totalFields;

        return new ProfileCompletionResponse(completionPercentage, completedFields, totalFields, missingFields);
    }

    public UserProfile getProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));
    }

    public UserProfile getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return getProfileByUserId(user.getId());
    }

    public UserProfile updateProfile(
            Long userId,
            UserProfile updatedProfile) {

        UserProfile existingProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        existingProfile.setFullName(updatedProfile.getFullName());
        existingProfile.setAge(updatedProfile.getAge());
        existingProfile.setLifeStage(updatedProfile.getLifeStage());
        existingProfile.setOccupation(updatedProfile.getOccupation());
        existingProfile.setInstitutionType(updatedProfile.getInstitutionType());
        existingProfile.setCourse(updatedProfile.getCourse());
        existingProfile.setCollege(updatedProfile.getCollege());
        existingProfile.setCgpa(updatedProfile.getCgpa());
        existingProfile.setFamilyIncome(updatedProfile.getFamilyIncome());
        existingProfile.setCategory(updatedProfile.getCategory());
        existingProfile.setState(updatedProfile.getState());
        existingProfile.setYearOfStudy(updatedProfile.getYearOfStudy());
        existingProfile.setGender(updatedProfile.getGender());
        existingProfile.setDomicileState(updatedProfile.getDomicileState());
        existingProfile.setFeatures(updatedProfile.getFeatures());
        existingProfile.setBehaviors(updatedProfile.getBehaviors());
        existingProfile.setProfileData(updatedProfile.getProfileData());

        return userProfileRepository.save(existingProfile);
    }

    public UserProfile updateProfileByEmail(String email, UserProfile updatedProfile) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return updateProfile(user.getId(), updatedProfile);
    }

    public void deleteProfile(Long userId) {

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        userProfileRepository.delete(profile);
    }

    public void assertCanAccess(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Authentication required");
        }
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (admin) return;
        User owner = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!userId.equals(owner.getId())) {
            throw new RuntimeException("Profile access denied");
        }
    }

    private boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
