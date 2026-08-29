package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.model.OrganizationMember;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.OrganizationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/org")
public class OrganizationController {

    private final OrganizationService orgService;
    private final UserRepository userRepo;
    private final OrganizationMemberRepository memberRepo;

    public OrganizationController(
            OrganizationService orgService,
            UserRepository userRepo,
            OrganizationMemberRepository memberRepo) {
        this.orgService = orgService;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
    }

    @GetMapping("/admin/requests")
    public String adminRequests(Authentication auth,
                                HttpServletRequest request) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String email = auth.getName();
        User admin = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        List<OrganizationMember> pending = orgService
                .getPendingRequestsForAdmin(admin);
        request.setAttribute("pending", pending);

        return "org/admin-requests";
    }

    @PostMapping("/admin/approve")
    public String approve(
            @RequestParam Long membershipId,
            @RequestParam String roleName,
            Authentication auth,
            HttpServletRequest request) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        try {
            String email = auth.getName();
            User admin = userRepo.findByEmail(email)
                    .orElseThrow(() -> new
                            IllegalStateException(
                            "User not found."));

            orgService.approveMembership(
                    admin, membershipId, roleName);

            return "redirect:/org/admin/requests?approved=true";

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            request.setAttribute("error", e.getMessage());

            String email = auth.getName();
            User admin = userRepo.findByEmail(email)
                    .orElseThrow(() -> new
                            IllegalStateException(
                            "User not found."));

            request.setAttribute("pending",
                    orgService.getPendingRequestsForAdmin(admin));

            return "org/admin-requests";
        }
    }

    @PostMapping("/admin/reject")
    public String reject(
            @RequestParam Long membershipId,
            Authentication auth,
            HttpServletRequest request) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        try {
            String email = auth.getName();
            User admin = userRepo.findByEmail(email)
                    .orElseThrow(() -> new
                            IllegalStateException(
                            "User not found."));

            orgService.rejectMembership(admin, membershipId);

            return "redirect:/org/admin/requests?memberRejected=true";

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            request.setAttribute("error", e.getMessage());

            String email = auth.getName();
            User admin = userRepo.findByEmail(email)
                    .orElseThrow(() -> new
                            IllegalStateException(
                            "User not found."));

            request.setAttribute("pending",
                    orgService.getPendingRequestsForAdmin(admin));

            return "org/admin-requests";
        }
    }

    @GetMapping("/rejected")
    public String rejectedPage(Authentication auth,
                               HttpServletRequest request) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        boolean rejected = memberRepo
                .existsByUser_UserIdAndStatus(
                        user.getUserId(), "REJECTED");

        if (!rejected) {
            return "redirect:/auth/post-login";
        }

        return "org/rejected";
    }
}