package com.example.riskmanagementsystem.service;

import com.example.riskmanagementsystem.model
        .PasswordResetToken;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo
        .PasswordResetTokenRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import org.springframework.security.crypto.password
        .PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepo,
            UserRepository userRepo,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void initiateReset(String email,
                              String baseUrl) {

        // find user by email — if not found do nothing
        // this prevents email enumeration attacks
        userRepo.findByEmail(email).ifPresent(user -> {

            // delete any existing tokens for this user
            tokenRepo.deleteByUser_UserId(
                    user.getUserId());

            // generate unique token
            String token = UUID.randomUUID().toString();

            // create and save token
            PasswordResetToken resetToken =
                    new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiresAt(
                    LocalDateTime.now().plusMinutes(30));
            resetToken.setUsed(false);
            tokenRepo.save(resetToken);

            // send reset email
            String resetLink = baseUrl
                    + "/auth/reset-password?token="
                    + token;
            emailService.sendPasswordResetEmail(
                    email, resetLink);
        });
    }

    @Transactional
    public boolean resetPassword(String token,
                                 String newPassword) {

        PasswordResetToken resetToken = tokenRepo
                .findByToken(token)
                .orElse(null);

        // validate token exists, not used and not expired
        if (resetToken == null
                || resetToken.isUsed()
                || resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {
            return false;
        }

        // update password
        User user = resetToken.getUser();
        user.setPasswordHash(
                passwordEncoder.encode(newPassword));
        userRepo.save(user);

        // mark token as used
        resetToken.setUsed(true);
        tokenRepo.save(resetToken);

        return true;
    }

    public boolean isValidToken(String token) {
        return tokenRepo.findByToken(token)
                .map(t -> !t.isUsed()
                        && t.getExpiresAt()
                        .isAfter(LocalDateTime.now()))
                .orElse(false);
    }
}
