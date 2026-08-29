package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.dto.AIGuidance;
import com.example.riskmanagementsystem.model.*;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.RiskRepository;
import com.example.riskmanagementsystem.repo.UserProfileRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.AIGuidanceService;
import com.example.riskmanagementsystem.service.MitigationAssignmentService;
import com.example.riskmanagementsystem.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MitigationAssignmentController {

    private final MitigationAssignmentService assignmentService;
    private final RiskRepository riskRepo;
    private final UserRepository userRepo;
    private final OrganizationMemberRepository memberRepo;
    private final UserProfileRepository profileRepo;
    private final AIGuidanceService aiGuidanceService;
    private final NotificationService notificationService;

    public MitigationAssignmentController(
            MitigationAssignmentService assignmentService,
            RiskRepository riskRepo,
            UserRepository userRepo,
            OrganizationMemberRepository memberRepo,
            UserProfileRepository profileRepo,
            AIGuidanceService aiGuidanceService,
            NotificationService notificationService) {
        this.assignmentService = assignmentService;
        this.riskRepo = riskRepo;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.profileRepo = profileRepo;
        this.aiGuidanceService = aiGuidanceService;
        this.notificationService = notificationService;
    }

    // only general users can be assigned mitigation tasks
    // managers oversee risks, they do not carry out tasks
    private List<UserProfile> buildAssigneeProfiles(Long orgId) {
        List<Long> userIds = memberRepo
                .findByOrganization_OrgIdAndStatus(orgId, "APPROVED")
                .stream()
                .filter(m -> m.getRole() != null
                        && "USER".equalsIgnoreCase(
                        m.getRole().getName()))
                .map(m -> m.getUser().getUserId())
                .toList();

        return profileRepo.findByUser_UserIdIn(userIds);
    }

    // checks if the current user is allowed to assign
    // mitigation for this risk:
    // - risk owner (manager) can always assign
    // - admin can only assign if no risk owner has been set
    private boolean canAssignMitigation(Risk risk,
                                        User currentUser,
                                        String systemRole) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(systemRole);
        boolean isRiskOwner = risk.getRiskOwner() != null
                && risk.getRiskOwner().getUserId()
                .equals(currentUser.getUserId());

        if (isRiskOwner) return true;
        if (isAdmin && risk.getRiskOwner() == null) return true;
        return false;
    }

    @GetMapping("/risks/{riskId}/assign/ai-guidance")
    public String generateAiGuidance(
            @PathVariable Long riskId,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes) {

        Risk risk = riskRepo.findById(riskId)
                .orElseThrow(() -> new
                        RuntimeException("Risk not found"));

        String email = auth.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        RuntimeException("User not found"));

        OrganizationMember membership = memberRepo
                .findFirstByUser_UserIdAndStatus(
                        currentUser.getUserId(), "APPROVED")
                .orElseThrow(() -> new
                        RuntimeException("Not approved"));

        String systemRole = membership.getRole().getName();
        Long orgId = membership.getOrganization().getOrgId();

        // enforce server-side — redirect if not allowed
        if (!canAssignMitigation(risk, currentUser, systemRole)) {
            redirectAttributes.addFlashAttribute("error",
                    "Mitigation assignment is managed by "
                            + "the assigned risk owner.");
            return "redirect:/risks";
        }

        List<UserProfile> profiles = buildAssigneeProfiles(orgId);

        AIGuidance aiGuidance =
                aiGuidanceService.generateGuidance(risk, profiles);

        if (risk.getRiskOwner() != null) {
            profileRepo.findByUser_UserId(
                            risk.getRiskOwner().getUserId())
                    .ifPresent(p -> model.addAttribute(
                            "riskOwnerName", p.getFullName()));
        }

        model.addAttribute("risk", risk);
        model.addAttribute("profiles", profiles);
        model.addAttribute("aiGuidance", aiGuidance);
        model.addAttribute("currentDate",
                java.time.LocalDate.now().toString());

        return "risk/assign";
    }

    @GetMapping("/risks/{riskId}/assign")
    public String assignRisk(@PathVariable Long riskId,
                             Authentication auth,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        Risk risk = riskRepo.findById(riskId)
                .orElseThrow(() -> new
                        RuntimeException("Risk not found"));

        String email = auth.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        RuntimeException("User not found"));

        OrganizationMember membership = memberRepo
                .findFirstByUser_UserIdAndStatus(
                        currentUser.getUserId(), "APPROVED")
                .orElseThrow(() -> new
                        RuntimeException("User not approved"));

        String systemRole = membership.getRole().getName();
        Long orgId = membership.getOrganization().getOrgId();

        // enforce server-side — redirect if not allowed
        if (!canAssignMitigation(risk, currentUser, systemRole)) {
            redirectAttributes.addFlashAttribute("error",
                    "Mitigation assignment is managed by "
                            + "the assigned risk owner.");
            return "redirect:/risks";
        }

        List<UserProfile> profiles = buildAssigneeProfiles(orgId);

        if (risk.getRiskOwner() != null) {
            profileRepo.findByUser_UserId(
                            risk.getRiskOwner().getUserId())
                    .ifPresent(p -> model.addAttribute(
                            "riskOwnerName", p.getFullName()));
        }

        model.addAttribute("risk", risk);
        model.addAttribute("profiles", profiles);
        model.addAttribute("currentDate",
                java.time.LocalDate.now().toString());

        return "risk/assign";
    }

    @PostMapping("/risks/{riskId}/assign")
    public String assignRisk(@PathVariable Long riskId,
                             @RequestParam Long assignedToUserId,
                             @RequestParam String actionDetails,
                             @RequestParam(required = false)
                             String deadline,
                             Authentication auth,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        Risk risk = riskRepo.findById(riskId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Risk not found."));

        String email = auth.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException(
                        "Logged in user not found."));

        OrganizationMember membership = memberRepo
                .findFirstByUser_UserIdAndStatus(
                        currentUser.getUserId(), "APPROVED")
                .orElseThrow(() -> new
                        IllegalStateException(
                        "User is not in an approved organisation."));

        String systemRole = membership.getRole().getName();
        Long orgId = membership.getOrganization().getOrgId();

        // enforce server-side — redirect if not allowed
        if (!canAssignMitigation(risk, currentUser, systemRole)) {
            redirectAttributes.addFlashAttribute("error",
                    "Mitigation assignment is managed by "
                            + "the assigned risk owner.");
            return "redirect:/risks";
        }

        try {
            LocalDate parsedDeadline = null;
            if (deadline != null && !deadline.isBlank()) {
                parsedDeadline = LocalDate.parse(deadline);
            }

            assignmentService.assignAction(
                    riskId,
                    assignedToUserId,
                    currentUser.getUserId(),
                    actionDetails,
                    parsedDeadline
            );

            return "redirect:/risks?assigned=true";

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            List<UserProfile> profiles =
                    buildAssigneeProfiles(orgId);

            request.setAttribute("error", e.getMessage());
            request.setAttribute("risk", risk);
            request.setAttribute("profiles", profiles);

            return "risk/assign";
        }
    }

    @GetMapping("/assignments/{assignmentId}/edit")
    public String editAssignmentPage(
            @PathVariable Long assignmentId,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes) {

        String email = auth.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException(
                        "Logged in user not found."));

        MitigationAssignment assignment =
                assignmentService.getAssignmentById(assignmentId);

        OrganizationMember membership = memberRepo
                .findFirstByUser_UserIdAndStatus(
                        currentUser.getUserId(), "APPROVED")
                .orElseThrow(() -> new
                        IllegalStateException(
                        "User not approved."));

        String systemRole = membership.getRole().getName();
        Long orgId = membership.getOrganization().getOrgId();

        boolean isAdmin = "ADMIN".equalsIgnoreCase(systemRole);
        boolean isManager = "MANAGER".equalsIgnoreCase(systemRole);

        // general users can only edit their own assignment
        if (!isAdmin && !isManager
                && !assignment.getAssignedTo().getUserId()
                .equals(currentUser.getUserId())) {
            redirectAttributes.addFlashAttribute("error",
                    "You can only update your own assignment.");
            return "redirect:/risks";
        }

        if (isAdmin || isManager) {
            List<UserProfile> profiles =
                    buildAssigneeProfiles(orgId);
            model.addAttribute("profiles", profiles);
        }

        model.addAttribute("currentDate",
                java.time.LocalDate.now().toString());
        model.addAttribute("assignment", assignment);
        model.addAttribute("isAdminOrManager",
                isAdmin || isManager);

        return "assignments/edit";
    }

    @PostMapping("/assignments/{assignmentId}/edit")
    public String updateAssignment(
            @PathVariable Long assignmentId,
            @RequestParam String status,
            @RequestParam(required = false) String actionDetails,
            @RequestParam(required = false) String deadline,
            @RequestParam(required = false) Long assignedToUserId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            String email = auth.getName();
            User currentUser = userRepo.findByEmail(email)
                    .orElseThrow(() -> new
                            IllegalStateException(
                            "Logged in user not found."));

            OrganizationMember membership = memberRepo
                    .findFirstByUser_UserIdAndStatus(
                            currentUser.getUserId(), "APPROVED")
                    .orElseThrow(() -> new
                            IllegalStateException(
                            "User not approved."));

            String systemRole = membership.getRole().getName();
            boolean isAdmin = "ADMIN"
                    .equalsIgnoreCase(systemRole);
            boolean isManager = "MANAGER"
                    .equalsIgnoreCase(systemRole);

            MitigationAssignment assignment =
                    assignmentService.getAssignmentById(
                            assignmentId);

            if (!isAdmin && !isManager
                    && !assignment.getAssignedTo().getUserId()
                    .equals(currentUser.getUserId())) {
                redirectAttributes.addFlashAttribute("error",
                        "You can only update your own assignment.");
                return "redirect:/risks";
            }

            // track if assignee is changing
            boolean reassigned = false;
            User previousAssignee = assignment.getAssignedTo();
            User newAssignee = null;

            if (isAdmin || isManager) {

                if (actionDetails != null
                        && !actionDetails.isBlank()) {
                    assignment.setActionDetails(actionDetails);
                }

                if (deadline != null && !deadline.isBlank()) {
                    assignment.setDeadline(
                            LocalDate.parse(deadline));
                }

                if (assignedToUserId != null
                        && !assignedToUserId.equals(
                        previousAssignee.getUserId())) {
                    newAssignee = userRepo
                            .findById(assignedToUserId)
                            .orElseThrow(() -> new
                                    IllegalStateException(
                                    "Assignee not found."));
                    assignment.setAssignedTo(newAssignee);
                    reassigned = true;
                }
            }

            // update status for all roles
            if (!"OVERDUE".equalsIgnoreCase(
                    assignment.getStatus())
                    || "DONE".equalsIgnoreCase(status)) {
                assignment.setStatus(status);
            }

            assignmentService.saveAssignment(assignment);

            String riskTitle = assignment.getRisk()
                    .getRiskTitle();

            // notify new assignee if reassigned
            if (reassigned && newAssignee != null) {
                notificationService.createNotification(
                        newAssignee,
                        "You have been assigned a mitigation "
                                + "task for risk '"
                                + riskTitle
                                + "'. Please review the action "
                                + "details and complete the task "
                                + "by the deadline.",
                        "RISK_ASSIGNED"
                );

                notificationService.createNotification(
                        previousAssignee,
                        "Your mitigation task for risk '"
                                + riskTitle
                                + "' has been reassigned to "
                                + "another team member.",
                        "ASSIGNMENT_UPDATED"
                );
            }

            if (!reassigned && (isAdmin || isManager)
                    && actionDetails != null
                    && !actionDetails.isBlank()) {
                notificationService.createNotification(
                        assignment.getAssignedTo(),
                        "The action details for your mitigation "
                                + "task on risk '"
                                + riskTitle
                                + "' have been updated. Please "
                                + "review the latest instructions.",
                        "ASSIGNMENT_UPDATED"
                );
            }

            if ("DONE".equalsIgnoreCase(status)
                    && assignment.getRisk().getRiskOwner()
                    != null) {
                notificationService.createNotification(
                        assignment.getRisk().getRiskOwner(),
                        "The mitigation task for risk '"
                                + riskTitle
                                + "' has been marked as Done. "
                                + "You can now review and resolve "
                                + "the risk.",
                        "TASK_COMPLETED"
                );
            }

            redirectAttributes.addFlashAttribute("success",
                    "Assignment updated successfully.");
            return "redirect:/risks?assignmentUpdated=true";

        } catch (IllegalArgumentException
                 | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error",
                    e.getMessage());
            return "redirect:/assignments/"
                    + assignmentId + "/edit";
        }
    }
}
