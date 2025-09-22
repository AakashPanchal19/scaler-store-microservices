package com.scaler.store.user.service;

import com.scaler.store.common.events.PasswordResetEvent;
import com.scaler.store.common.events.UserRegisteredEvent;
import com.scaler.store.user.dto.*;
import com.scaler.store.user.entity.PasswordResetToken;
import com.scaler.store.user.entity.Role;
import com.scaler.store.user.entity.User;
import com.scaler.store.user.repository.PasswordResetTokenRepository;
import com.scaler.store.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserKafkaProducer kafkaProducer;

    public UserService(UserRepository userRepository,
                       PasswordResetTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserKafkaProducer kafkaProducer) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + req.getEmail());
        }

        User user = new User();
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setStreet(req.getStreet());
        user.setCity(req.getCity());
        user.setState(req.getState());
        user.setPostalCode(req.getPostalCode());
        user.setCountry(req.getCountry());
        user.setRole(Role.ROLE_USER);

        User savedUser = userRepository.save(user);

        // Produce UserRegisteredEvent to Kafka
        UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getPhoneNumber()
        );
        kafkaProducer.publishUserRegistered(event);

        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return mapToProfile(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());
        if (req.getStreet() != null) user.setStreet(req.getStreet());
        if (req.getCity() != null) user.setCity(req.getCity());
        if (req.getState() != null) user.setState(req.getState());
        if (req.getPostalCode() != null) user.setPostalCode(req.getPostalCode());
        if (req.getCountry() != null) user.setCountry(req.getCountry());

        User updated = userRepository.save(user);
        return mapToProfile(updated);
    }

    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, 60); // 60 minutes
        tokenRepository.save(resetToken);

        String resetUrl = "http://localhost:8080/api/auth/reset-password?token=" + token;
        PasswordResetEvent event = new PasswordResetEvent(
                user.getId(),
                user.getEmail(),
                token,
                resetUrl,
                LocalDateTime.now().plusMinutes(60)
        );
        kafkaProducer.publishPasswordReset(event);

        return "Password reset link sent to your registered email.";
    }

    @Transactional
    public String resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Token has already been used");
        }
        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return "Password successfully reset. You may now log in.";
    }

    private UserProfileResponse mapToProfile(User user) {
        UserProfileResponse res = new UserProfileResponse();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setStreet(user.getStreet());
        res.setCity(user.getCity());
        res.setState(user.getState());
        res.setPostalCode(user.getPostalCode());
        res.setCountry(user.getCountry());
        res.setRole(user.getRole());
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }
}
