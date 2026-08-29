package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.model.UserProfile;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private final UserRepository userRepo;
    private final UserProfileService profileService;

    public UserProfileController(UserRepository userRepo,
                                 UserProfileService profileService) {
        this.userRepo = userRepo;
        this.profileService = profileService;
    }

    // GET — view or edit existing profile
    @GetMapping("/edit")
    public String editProfilePage(Authentication auth,
                                  HttpServletRequest request) {

        // check if user is logged in
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        // get logged-in user's email
        String email = auth.getName();

        // find user in database
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        // pre-fill form with existing profile data
        UserProfile existing = profileService
                .getProfileByUserId(user.getUserId());

        if (existing != null) {
            request.setAttribute("fullName",
                    existing.getFullName());
            request.setAttribute("jobRole",
                    existing.getJobRole());
            request.setAttribute("department",
                    existing.getDepartment());
            request.setAttribute("phoneNumber",
                    existing.getPhoneNumber());
        }

        return "profile/edit";
    }

    // POST — save updated profile details
    @PostMapping("/edit")
    public String saveEditedProfile(
            @RequestParam String fullName,
            @RequestParam String jobRole,
            @RequestParam String department,
            @RequestParam(required = false) String phoneNumber,
            Authentication auth,
            HttpServletRequest request) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        try {
            String email = auth.getName();
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new
                            IllegalStateException("User not found."));

            if (fullName == null || fullName.isBlank()) {
                throw new IllegalArgumentException(
                        "Full name is required.");
            }
            if (jobRole == null || jobRole.isBlank()) {
                throw new IllegalArgumentException(
                        "Job role is required.");
            }
            if (department == null || department.isBlank()) {
                throw new IllegalArgumentException(
                        "Department is required.");
            }

            profileService.saveOrUpdateProfile(
                    user, fullName, jobRole,
                    department, phoneNumber
            );

            return "redirect:/profile/edit?saved=true";

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            request.setAttribute("error", e.getMessage());
            request.setAttribute("fullName", fullName);
            request.setAttribute("jobRole", jobRole);
            request.setAttribute("department", department);
            request.setAttribute("phoneNumber", phoneNumber);

            return "profile/edit";
        }
    }
}