package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.service
        .PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(
            PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @RequestParam String email,
            HttpServletRequest request,
            Model model) {

        String baseUrl = request.getScheme()
                + "://" + request.getServerName()
                + ":" + request.getServerPort();

        passwordResetService.initiateReset(email, baseUrl);

        // always show success even if email not found
        // prevents email enumeration attacks
        model.addAttribute("message",
                "If an account exists with that email "
                        + "address a password reset link has "
                        + "been sent. Please check your inbox.");

        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam String token,
            Model model) {

        if (!passwordResetService.isValidToken(token)) {
            model.addAttribute("error",
                    "This reset link is invalid or has "
                            + "expired. Please request a new one.");
            return "auth/forgot-password";
        }

        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {

        // validate token first
        if (!passwordResetService.isValidToken(token)) {
            model.addAttribute("error",
                    "This reset link is invalid or has "
                            + "expired. Please request a new one.");
            return "auth/forgot-password";
        }

        // passwords must match
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error",
                    "Passwords do not match.");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        // minimum and maximum length
        if (newPassword.length() < 8
                || newPassword.length() > 64) {
            model.addAttribute("error",
                    "Password must be between 8 and "
                            + "64 characters.");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        // must contain uppercase and lowercase
        if (!newPassword.matches(".*[A-Z].*")
                || !newPassword.matches(".*[a-z].*")) {
            model.addAttribute("error",
                    "Password must contain both uppercase "
                            + "and lowercase letters.");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        // must contain a number
        if (!newPassword.matches(".*\\d.*")) {
            model.addAttribute("error",
                    "Password must contain at least "
                            + "one number.");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        // must contain a special character
        if (!newPassword.matches(".*[@$!%*?&].*")) {
            model.addAttribute("error",
                    "Password must contain at least one "
                            + "special character such as "
                            + "@ $ ! % * ? &");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        boolean success = passwordResetService
                .resetPassword(token, newPassword);

        if (success) {
            return "redirect:/auth/login"
                    + "?passwordReset=true";
        } else {
            model.addAttribute("error",
                    "This reset link is invalid or has "
                            + "expired. Please request a new one.");
            return "auth/forgot-password";
        }
    }
}
