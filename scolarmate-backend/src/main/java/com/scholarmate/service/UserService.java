package com.scholarmate.service;

import com.scholarmate.dto.*;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.scholarmate.config.JwtService jwtService;
    private final LoginRateLimiterService rateLimiterService;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder,
                       com.scholarmate.config.JwtService jwtService,
                       LoginRateLimiterService rateLimiterService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.rateLimiterService = rateLimiterService;
    }

    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this(userRepository, userProfileRepository, passwordEncoder,
                new com.scholarmate.config.JwtService("scholarmate-super-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256-security", 86400000),
                new LoginRateLimiterService());
    }

    /**
     * Registers a new user and creates the associated profile.
     * Returns a RegisterResponse containing basic user info and tokens.
     */
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setMobile(request.getMobile());
        user.setEducationType(request.getEducationType());
        user.setEducationLevel(request.getEducationLevel());
        user.setCourse(request.getCourse());
        user.setState(request.getState());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null && !request.getRole().isBlank() ? request.getRole() : "STUDENT");
        user.setIsUnder18(Boolean.TRUE.equals(request.getIsUnder18()) || (request.getAge() != null && request.getAge() < 18));
        user.setGuardianName(request.getGuardianName());
        user.setGuardianPhone(request.getGuardianPhone());
        user.setTermsAccepted(Boolean.TRUE.equals(request.getTermsAccepted()));
        if (Boolean.TRUE.equals(request.getTermsAccepted())) {
            user.setTermsAcceptedAt(LocalDateTime.now());
        }

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .fullName(request.getName())
                .age(request.getAge())
                .lifeStage(request.getEducationType() != null ? request.getEducationType() : request.getLifeStage())
                .occupation(request.getOccupation())
                .institutionType(request.getInstitutionType())
                .course(request.getCourse())
                .college(request.getCollege())
                .cgpa(request.getCgpa())
                .familyIncome(request.getFamilyIncome())
                .category(request.getCategory())
                .state(request.getState())
                .yearOfStudy(request.getYearOfStudy())
                .gender(request.getGender())
                .domicileState(request.getState())
                .educationStage(request.getEducationLevel() != null ? request.getEducationLevel() : request.getEducationStage())
                .features(request.getFeatures())
                .behaviors(request.getBehaviors())
                .build();

        userProfileRepository.save(profile);

        String token = jwtService.generateAccessToken(savedUser.getEmail(), savedUser.getRole(), savedUser.getId());
        String refreshToken = jwtService.generateRefreshToken(savedUser.getEmail(), savedUser.getId());
        return new RegisterResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole(), token, refreshToken, 900000);
    }

    /**
     * Authenticates a user with rate limiting and returns the User entity.
     */
    public User loginUser(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        rateLimiterService.checkRateLimit(email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            rateLimiterService.recordFailedAttempt(email);
            throw new RuntimeException("Invalid Email or Password");
        }

        rateLimiterService.recordSuccessfulLogin(email);
        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Initiates password reset by issuing a secure token.
     */
    public String forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            // In a production setup, this sends an email via SMTP.
            // For audit/logging in dev mode:
            System.out.println("Password reset token generated for " + email + ": " + token);
        });
        return "If an account with that email exists, password reset instructions have been sent.";
    }

    /**
     * Resets password using a validated token.
     */
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired password reset token."));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        return "Password has been successfully reset. You may now log in with your new password.";
    }
}
