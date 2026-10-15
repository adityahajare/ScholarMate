package com.scholarmate.service;

import com.scholarmate.dto.LoginRequest;
import com.scholarmate.dto.RegisterRequest;
import com.scholarmate.dto.RegisterResponse;
import com.scholarmate.entity.User;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.UserProfileRepository;
import com.scholarmate.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.scholarmate.config.JwtService jwtService;

    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder,
                       com.scholarmate.config.JwtService jwtService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this(userRepository, userProfileRepository, passwordEncoder,
                new com.scholarmate.config.JwtService("scholarmate-super-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256-security", 86400000));
    }

    /**
     * Registers a new user and creates the associated profile.
     * Returns a RegisterResponse containing basic user info.
     */
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("STUDENT");

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .fullName(request.getName())
                .age(request.getAge())
                .lifeStage(request.getLifeStage())
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
                .educationStage(request.getEducationStage())
                .features(request.getFeatures())
                .behaviors(request.getBehaviors())
                .build();

        userProfileRepository.save(profile);

        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole(), savedUser.getId());
        return new RegisterResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole(), token);
    }

    /**
     * Authenticates a user and returns the User entity.
     */
    public User loginUser(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return user;
        } else {
            throw new RuntimeException("Invalid Email or Password");
        }
    }
}
