package com.example.riskmanagementsystem.service;

import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.model.UserProfile;
import com.example.riskmanagementsystem.repo.UserProfileRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final UserProfileRepository profileRepo;

    private final PasswordEncoder encoder;

    public AuthService(UserRepository userRepo, UserProfileRepository profileRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.encoder = encoder;
    }

    public void register(String fullName, String email, String rawPassword) {

        String cleanedEmail = email == null ? null : email.trim().toLowerCase();
        String cleanedName = fullName == null ? null : fullName.trim();

        if (cleanedName == null || cleanedName.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        // only letters and spaces allowed in full name
        if (!cleanedName.matches("^[a-zA-Z\\s]{2,50}$")) {
            throw new IllegalArgumentException(
                    "Full name must be between 2 and 50 characters " +
                            "and contain only letters.");
        }

        if (cleanedEmail == null || cleanedEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        // basic email format check
        if (!cleanedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address.");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (rawPassword.length() < 8 || rawPassword.length() > 64) {
            throw new IllegalArgumentException(
                    "Password must be between 8 and 64 characters.");
        }
        if (!rawPassword.matches(".*[A-Z].*") || !rawPassword.matches(".*[a-z].*")) {
            throw new IllegalArgumentException(
                    "Password must contain both uppercase and lowercase letters.");
        }
        if (!rawPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one number.");
        }
        if (!rawPassword.matches(".*[@$!%*?&].*")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one special character " +
                            "such as @ $ ! % * ? &");
        }

        if (userRepo.existsByEmail(cleanedEmail)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        User u = new User();
        u.setEmail(cleanedEmail);
        u.setPasswordHash(encoder.encode(rawPassword));
        userRepo.save(u);

        UserProfile p = new UserProfile();
        p.setUser(u);
        p.setFullName(cleanedName);
        profileRepo.save(p);
    }
}
