package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.model.MitigationAssignment;
import com.example.riskmanagementsystem.model.OrganizationMember;
import com.example.riskmanagementsystem.model.Risk;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.UserProfileRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final UserRepository userRepo;
    private final OrganizationMemberRepository memberRepo;
    private final RiskService riskService;
    private final UserProfileService profileService;
    private final UserProfileRepository profileRepo;
    private final MitigationAssignmentService assignmentService;
    private final NotificationService notificationService;

    public DashboardController(
            UserRepository userRepo,
            OrganizationMemberRepository memberRepo,
            RiskService riskService,
            UserProfileService profileService,
            UserProfileRepository profileRepo,
            MitigationAssignmentService assignmentService,
            NotificationService notificationService) {
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.riskService = riskService;
        this.profileService = profileService;
        this.profileRepo = profileRepo;
        this.assignmentService = assignmentService;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException(
                        "Logged in user not found."));

        if (!profileRepo.existsByUser_UserId(user.getUserId())) {
            return "redirect:/profile/create";
        }

        OrganizationMember membership = memberRepo
                .findFirstByUser_UserIdAndStatus(
                        user.getUserId(), "APPROVED")
                .orElseThrow(() -> new
                        IllegalStateException(
                        "No approved organisation found."));

        Long orgId = membership.getOrganization().getOrgId();
        String systemRole = membership.getRole().getName();

        List<Risk> risks = new ArrayList<>();
        String recentRisksHeading;

        if ("USER".equalsIgnoreCase(systemRole)) {
            List<MitigationAssignment> myAssignments =
                    assignmentService.getAssignmentsForUser(
                            user.getUserId());
            for (MitigationAssignment assignment : myAssignments) {
                if (assignment.getRisk() != null) {
                    risks.add(assignment.getRisk());
                }
            }
            recentRisksHeading = "Your 5 Most Recent Mitigation Tasks";

        } else if ("MANAGER".equalsIgnoreCase(systemRole)) {
            List<Risk> ownedRisks = riskService
                    .getRisksByOrganization(orgId)
                    .stream()
                    .filter(r -> r.getRiskOwner() != null
                            && r.getRiskOwner().getUserId()
                            .equals(user.getUserId()))
                    .collect(Collectors.toList());

            List<Risk> assignedRisks = new ArrayList<>();
            assignmentService.getAssignmentsForUser(
                            user.getUserId())
                    .forEach(a -> {
                        if (a.getRisk() != null) {
                            assignedRisks.add(a.getRisk());
                        }
                    });

            risks.addAll(ownedRisks);
            for (Risk r : assignedRisks) {
                boolean alreadyAdded = risks.stream()
                        .anyMatch(existing -> existing
                                .getRiskId()
                                .equals(r.getRiskId()));
                if (!alreadyAdded) {
                    risks.add(r);
                }
            }

            recentRisksHeading =
                    "Your 5 Most Recent Owned and Assigned Risks";

        } else {
            risks = riskService.getRisksByOrganization(orgId);
            recentRisksHeading = "5 Most Recently Submitted Risks";
        }

        Map<Long, MitigationAssignment> assignmentsByRiskId =
                new HashMap<>();
        for (Risk risk : risks) {
            MitigationAssignment assignment =
                    assignmentService.getAssignmentForRisk(
                            risk.getRiskId());
            if (assignment != null) {
                assignmentsByRiskId.put(
                        risk.getRiskId(), assignment);
            }
        }

        List<Risk> recentRisks = risks.stream()
                .filter(r -> r.getCreatedAt() != null)
                .sorted(Comparator.comparing(Risk::getCreatedAt)
                        .reversed())
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("totalRisks", risks.stream()
                .filter(r -> !"CLOSED".equalsIgnoreCase(r.getStatus()))
                .count());
        model.addAttribute("highSeverityCount", risks.stream()
                .filter(r -> r.getSeverityScore() >= 15
                        && !"CLOSED".equalsIgnoreCase(r.getStatus()))
                .count());
        model.addAttribute("unresolvedCount", risks.stream()
                .filter(r -> !"RESOLVED".equalsIgnoreCase(r.getStatus())
                        && !"CLOSED".equalsIgnoreCase(r.getStatus()))
                .count());
        model.addAttribute("resolvedCount", risks.stream()
                .filter(r -> "RESOLVED".equalsIgnoreCase(r.getStatus())
                        || "CLOSED".equalsIgnoreCase(r.getStatus()))
                .count());

        model.addAttribute("risks", risks);
        model.addAttribute("recentRisks", recentRisks);
        model.addAttribute("recentRisksHeading", recentRisksHeading);
        model.addAttribute("assignmentsByRiskId", assignmentsByRiskId);
        model.addAttribute("currentUserId", user.getUserId());
        model.addAttribute("recentNotifications",
                notificationService.getNotificationsForUser(
                                user.getUserId()).stream()
                        .limit(5)
                        .collect(Collectors.toList()));

        return "dashboard";
    }
}