package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.model.OrganizationMember;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.UserProfileRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.OrganizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/org/members")
public class OrgMembersController {

    private final OrganizationService orgService;
    private final UserRepository userRepo;
    private final UserProfileRepository profileRepo;
    private final OrganizationMemberRepository memberRepo;

    public OrgMembersController(
            OrganizationService orgService,
            UserRepository userRepo,
            UserProfileRepository profileRepo,
            OrganizationMemberRepository memberRepo) {
        this.orgService = orgService;
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.memberRepo = memberRepo;
    }

    @GetMapping
    public String membersPage(Authentication auth,
                              Model model) {

        String email = auth.getName();
        User admin = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        List<OrganizationMember> members =
                orgService.getApprovedMembers(admin);

        java.util.Map<Long, com.example.riskmanagementsystem
                .model.UserProfile> profileMap =
                new java.util.HashMap<>();

        for (OrganizationMember m : members) {
            profileRepo.findByUser_UserId(
                            m.getUser().getUserId())
                    .ifPresent(p -> profileMap.put(
                            m.getUser().getUserId(), p));
        }

        model.addAttribute("members", members);
        model.addAttribute("profileMap", profileMap);
        model.addAttribute("currentUserId", admin.getUserId());

        return "org/members";
    }

    @PostMapping("/change-role")
    public String changeRole(
            @RequestParam Long membershipId,
            @RequestParam String newRole,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String email = auth.getName();
        User admin = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        try {
            orgService.changeMemberRole(
                    admin, membershipId, newRole);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Member role updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/org/members";
    }

    @PostMapping("/remove")
    public String removeMember(
            @RequestParam Long membershipId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String email = auth.getName();
        User admin = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        try {
            orgService.removeMember(admin, membershipId);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Member removed successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/org/members";
    }
}