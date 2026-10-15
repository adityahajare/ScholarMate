package com.scholarmate.controller;

import com.scholarmate.dto.ProfileCompletionResponse;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.getProfileByEmail(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfile> updateMyProfile(
            Authentication authentication,
            @RequestBody UserProfile updatedProfile) {
        return ResponseEntity.ok(
                userProfileService.updateProfileByEmail(authentication.getName(), updatedProfile));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<UserProfile> createProfile(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestBody UserProfile profile) {

        userProfileService.assertCanAccess(userId, authentication);

        UserProfile savedProfile =
                userProfileService.createProfile(userId, profile);

        return ResponseEntity.ok(savedProfile);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getProfile(
            Authentication authentication,
            @PathVariable Long userId) {

        userProfileService.assertCanAccess(userId, authentication);

        return ResponseEntity.ok(
                userProfileService.getProfileByUserId(userId)
        );
    }

    @GetMapping("/{userId}/completion")
    public ResponseEntity<ProfileCompletionResponse> getProfileCompletion(
            Authentication authentication,
            @PathVariable Long userId) {

        userProfileService.assertCanAccess(userId, authentication);

        return ResponseEntity.ok(
                userProfileService.getProfileCompletion(userId)
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfile> updateProfile(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestBody UserProfile updatedProfile) {

        userProfileService.assertCanAccess(userId, authentication);

        return ResponseEntity.ok(
                userProfileService.updateProfile(userId, updatedProfile)
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteProfile(
            Authentication authentication,
            @PathVariable Long userId) {

        userProfileService.assertCanAccess(userId, authentication);

        userProfileService.deleteProfile(userId);

        return ResponseEntity.ok("Profile deleted successfully");
    }
}
