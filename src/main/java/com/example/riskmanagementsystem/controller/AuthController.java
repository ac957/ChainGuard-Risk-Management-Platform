package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.UserProfileRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepo;
    private final UserProfileRepository profileRepo;
    private final OrganizationMemberRepository memberRepo;

    public AuthController(AuthService authService,
                          UserRepository userRepo,
                          UserProfileRepository profileRepo,
                          OrganizationMemberRepository memberRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.memberRepo = memberRepo;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           HttpServletRequest request) {

        try {
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException(
                        "Passwords do not match.");
            }

            authService.register(fullName, email, password);

            return "redirect:/auth/login?registered=true";

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("emailValue", email);
            request.setAttribute("fullNameValue", fullName);
            return "auth/register";
        }
    }

    // handles redirect logic after successful login
    @GetMapping("/post-login")
    public String postLogin(Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        boolean hasProfile = profileRepo
                .existsByUser_UserId(user.getUserId());
        boolean approved = memberRepo
                .existsByUser_UserIdAndStatus(
                        user.getUserId(), "APPROVED");
        boolean pending = memberRepo
                .existsByUser_UserIdAndStatus(
                        user.getUserId(), "PENDING");
        boolean rejected = memberRepo
                .existsByUser_UserIdAndStatus(
                        user.getUserId(), "REJECTED");

        // fully approved — go to dashboard
        if (approved) {
            return "redirect:/dashboard";
        }

        // pending — waiting for approval
        if (pending) {
            return "redirect:/auth/login?pending=true";
        }

        // check rejection count — block if 3 or more
        int rejectionCount = memberRepo
                .countByUser_UserIdAndStatus(
                        user.getUserId(), "REJECTED");

        if (rejectionCount >= 3) {
            return "redirect:/auth/login?restricted=true";
        }

        // rejected once or twice — can still try another org
        if (rejected) {
            return "redirect:/org/rejected";
        }

        // no profile yet
        if (!hasProfile) {
            return "redirect:/profile/setup";
        }

        // has profile but no org
        return "redirect:/profile/setup";
    }
}
