package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.model.Organization;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.model.UserProfile;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.OrganizationRepository;
import com.example.riskmanagementsystem.repo.UserProfileRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.OrganizationService;
import com.example.riskmanagementsystem.service.ProfileSetupService;
import com.example.riskmanagementsystem.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileSetupController {

    private final UserRepository userRepo;
    private final UserProfileRepository profileRepo;
    private final OrganizationRepository orgRepo;
    private final OrganizationMemberRepository memberRepo;
    private final UserProfileService profileService;
    private final OrganizationService orgService;
    private final ProfileSetupService profileSetupService;

    public ProfileSetupController(UserRepository userRepo,
                                  UserProfileRepository profileRepo,
                                  OrganizationRepository orgRepo,
                                  OrganizationMemberRepository memberRepo,
                                  UserProfileService profileService,
                                  OrganizationService orgService,
            ProfileSetupService profileSetupService) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.orgRepo = orgRepo;
        this.memberRepo = memberRepo;
        this.profileService = profileService;
        this.orgService = orgService;
        this.profileSetupService = profileSetupService;
    }

    // GET — show the combined setup page
    @GetMapping("/setup")
    public String showSetup(Authentication auth,
                            HttpServletRequest request) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        // if already fully set up, send to dashboard
        boolean approved = memberRepo
                .existsByUser_UserIdAndStatus(
                        user.getUserId(), "APPROVED");
        boolean hasProfile = profileRepo
                .existsByUser_UserId(user.getUserId());

        if (approved && hasProfile) {
            return "redirect:/dashboard";
        }

        // if pending, block access
        boolean pending = memberRepo
                .existsByUser_UserIdAndStatus(
                        user.getUserId(), "PENDING");
        if (pending) {
            return "redirect:/auth/login?pending=true";
        }

        // pre-fill profile fields if profile already exists
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

        request.setAttribute("orgs", orgRepo.findAll());
        return "profile/setup";
    }

    // POST — handles profile + org setup in one submission
    @PostMapping("/setup")
    public String handleSetup(
            @RequestParam String fullName,
            @RequestParam String jobRole,
            @RequestParam String department,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam String orgAction,
            @RequestParam(required = false) String orgId,
            @RequestParam(required = false) String orgName,
            @RequestParam(required = false) String specialisation,
            Authentication auth,
            HttpServletRequest request) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        try {
            // validate personal details via service
            profileService.saveOrUpdateProfile(
                    user, fullName, jobRole,
                    department, phoneNumber);

            // validate org setup via service
            profileSetupService.validateOrgSetup(
                    orgAction, orgId, orgName);

            // handle org action
            if ("create".equalsIgnoreCase(orgAction)) {
                orgService.createOrganization(
                        user, orgName, specialisation);
                return "redirect:/dashboard";
            } else {
                orgService.requestToJoin(
                        user, Long.parseLong(orgId));
                return "redirect:/auth/login?pending=true";
            }

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            request.setAttribute("error", e.getMessage());
            request.setAttribute("fullName", fullName);
            request.setAttribute("jobRole", jobRole);
            request.setAttribute("department", department);
            request.setAttribute("phoneNumber", phoneNumber);
            request.setAttribute("orgAction", orgAction);
            request.setAttribute("orgId", orgId);
            request.setAttribute("orgName", orgName);
            request.setAttribute("specialisation", specialisation);
            request.setAttribute("orgs", orgRepo.findAll());

            return "profile/setup";
        }
    }
}