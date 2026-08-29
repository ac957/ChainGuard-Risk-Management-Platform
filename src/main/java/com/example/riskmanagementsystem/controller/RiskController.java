package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.dto.RiskEducation;
import com.example.riskmanagementsystem.model.*;
import com.example.riskmanagementsystem.repo.CategoryRepository;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.UserProfileRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.AIGuidanceService;
import com.example.riskmanagementsystem.service.MitigationAssignmentService;
import com.example.riskmanagementsystem.service.RiskService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/risks")
public class RiskController {

    private final RiskService riskService;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final OrganizationMemberRepository memberRepo;
    private final MitigationAssignmentService assignmentService;
    private final UserProfileRepository profileRepo;
    private final AIGuidanceService aiGuidanceService;

    public RiskController(RiskService riskService,
                          UserRepository userRepo,
                          CategoryRepository categoryRepo,
                          OrganizationMemberRepository memberRepo,
                          MitigationAssignmentService assignmentService,
                          UserProfileRepository profileRepo,
                          AIGuidanceService aiGuidanceService) {
        this.riskService = riskService;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.memberRepo = memberRepo;
        this.assignmentService = assignmentService;
        this.profileRepo = profileRepo;
        this.aiGuidanceService = aiGuidanceService;
    }

    @GetMapping("/create")
    public String createRiskPage(Authentication auth,
                                 Model model) {
        model.addAttribute("categories", categoryRepo.findAll());
        return "risk/create";
    }

    @PostMapping("/create")
    public String createRisk(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "false") boolean showEducation, // ADD THIS
            @ModelAttribute Risk risk,
            Authentication auth,
            Model model) {

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        OrganizationMember membership = memberRepo
                .findFirstByUser_UserIdAndStatus(
                        user.getUserId(), "APPROVED")
                .orElseThrow(() -> new
                        IllegalStateException(
                        "User is not linked to an "
                                + "approved organisation."));

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new
                        IllegalArgumentException(
                        "Category not found."));

        risk.setSubmittedBy(user);
        risk.setCategory(category);
        risk.setOrganization(membership.getOrganization());

        try {
            Risk savedRisk = riskService.saveRisk(risk);

            if (showEducation) {                                         // ADD THIS
                return "redirect:/risks/" + savedRisk.getRiskId() + "/education";
            }
            return "redirect:/risks?submitted=true";                     // ADD THIS

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryRepo.findAll());
            return "risk/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editRiskForm(@PathVariable Long id,
                               Authentication auth,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        Risk risk = riskService.getRiskById(id)
                .orElseThrow(() -> new
                        IllegalStateException("Risk not found."));

        if (!risk.getSubmittedBy().getUserId()
                .equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("error",
                    "You can only edit risks you submitted.");
            return "redirect:/risks";
        }

        if (!"NEW".equalsIgnoreCase(risk.getStatus())) {
            redirectAttributes.addFlashAttribute("error",
                    "This risk can no longer be edited as "
                            + "it is already under review.");
            return "redirect:/risks";
        }

        model.addAttribute("risk", risk);
        model.addAttribute("categories", categoryRepo.findAll());
        return "risk/edit";
    }

    @PostMapping("/{id}/edit")
    public String editRiskSubmit(
            @PathVariable Long id,
            @RequestParam String riskTitle,
            @RequestParam String description,
            @RequestParam int likelihood,
            @RequestParam int impact,
            @RequestParam Long categoryId,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes) {

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        Risk risk = riskService.getRiskById(id)
                .orElseThrow(() -> new
                        IllegalStateException("Risk not found."));

        if (!risk.getSubmittedBy().getUserId()
                .equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("error",
                    "You can only edit risks you submitted.");
            return "redirect:/risks";
        }

        if (!"NEW".equalsIgnoreCase(risk.getStatus())) {
            redirectAttributes.addFlashAttribute("error",
                    "This risk can no longer be edited as "
                            + "it is already under review.");
            return "redirect:/risks";
        }

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new
                        IllegalArgumentException(
                        "Category not found."));

        risk.setRiskTitle(riskTitle);
        risk.setDescription(description);
        risk.setLikelihood(likelihood);
        risk.setImpact(impact);
        risk.setSeverityScore(likelihood * impact);
        risk.setCategory(category);

        try {
            riskService.updateRisk(risk);
            redirectAttributes.addFlashAttribute("success",
                    "Risk updated successfully.");
            return "redirect:/risks";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("risk", risk);
            model.addAttribute("categories", categoryRepo.findAll());
            return "risk/edit";
        }
    }

    @PostMapping("/{riskId}/status")
    public String updateStatus(
            @PathVariable Long riskId,
            @RequestParam String status,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        try {
            riskService.updateRiskStatus(
                    riskId, status, user.getUserId());
            redirectAttributes.addFlashAttribute(
                    "statusUpdated", true);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "statusError", e.getMessage());
        }

        return "redirect:/risks";
    }

    @PostMapping("/{riskId}/owner")
    public String assignOwner(
            @PathVariable Long riskId,
            @RequestParam(required = false) Long ownerUserId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        if (ownerUserId == null) {
            return "redirect:/risks";
        }

        String email = auth.getName();
        User admin = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        riskService.assignRiskOwner(riskId, ownerUserId, admin);

        return "redirect:/risks?ownerAssigned=true";
    }

    @GetMapping("/{riskId}/education")
    public String riskEducationPage(
            @PathVariable Long riskId,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes) {

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        Risk risk = riskService.getRiskById(riskId)
                .orElseThrow(() -> new
                        IllegalStateException("Risk not found."));

        if (!risk.getSubmittedBy().getUserId()
                .equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("error",
                    "You can only view education for "
                            + "risks you submitted.");
            return "redirect:/risks";
        }

        RiskEducation education =
                aiGuidanceService.generateRiskEducation(risk);

        model.addAttribute("risk", risk);
        model.addAttribute("education", education);

        return "risk/education";
    }

    @GetMapping
    public String riskList(Authentication auth, Model model) {

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        OrganizationMember membership = memberRepo
                .findFirstByUser_UserIdAndStatus(
                        user.getUserId(), "APPROVED")
                .orElseThrow(() -> new
                        IllegalStateException(
                        "No approved organisation found."));

        Long orgId = membership.getOrganization().getOrgId();
        String systemRole = membership.getRole().getName();

        List<Risk> risks = new ArrayList<>();
        if ("USER".equalsIgnoreCase(systemRole)) {

            List<Risk> submittedRisks = riskService
                    .getRisksSubmittedByUser(user.getUserId());

            List<Risk> assignedRisks = new ArrayList<>();
            assignmentService.getAssignmentsForUser(user.getUserId())
                    .forEach(a -> {
                        if (a.getRisk() != null)
                            assignedRisks.add(a.getRisk());
                    });

            risks.addAll(submittedRisks);
            for (Risk r : assignedRisks) {
                boolean alreadyAdded = risks.stream()
                        .anyMatch(existing -> existing.getRiskId()
                                .equals(r.getRiskId()));
                if (!alreadyAdded) {
                    risks.add(r);
                }
            }

        } else {
            risks = riskService.getRisksByOrganization(orgId);
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

        for (MitigationAssignment assignment :
                assignmentsByRiskId.values()) {
            assignmentService.checkAndMarkOverdue(assignment);
        }

        if ("ADMIN".equalsIgnoreCase(systemRole)) {
            List<UserProfile> managerProfiles = memberRepo
                    .findByOrganization_OrgIdAndStatus(
                            orgId, "APPROVED")
                    .stream()
                    .filter(m -> m.getRole() != null
                            && "MANAGER".equalsIgnoreCase(
                            m.getRole().getName()))
                    .map(m -> profileRepo
                            .findByUser_UserId(
                                    m.getUser().getUserId())
                            .orElse(null))
                    .filter(p -> p != null)
                    .toList();

            model.addAttribute("managerProfiles", managerProfiles);
        }

        List<UserProfile> ownerProfiles = profileRepo
                .findByUser_UserIdIn(
                        risks.stream()
                                .filter(r -> r.getRiskOwner() != null)
                                .map(r -> r.getRiskOwner().getUserId())
                                .distinct()
                                .toList());

        Map<Long, String> ownerNamesByUserId = ownerProfiles.stream()
                .collect(java.util.stream.Collectors.toMap(
                        p -> p.getUser().getUserId(),
                        p -> p.getFullName()
                ));

        List<UserProfile> submittedByProfiles = profileRepo
                .findByUser_UserIdIn(
                        risks.stream()
                                .filter(r -> r.getSubmittedBy() != null)
                                .map(r -> r.getSubmittedBy().getUserId())
                                .distinct()
                                .toList());

        Map<Long, String> submittedByNames = submittedByProfiles
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        p -> p.getUser().getUserId(),
                        p -> p.getFullName()
                ));

        model.addAttribute("ownerNamesByUserId", ownerNamesByUserId);
        model.addAttribute("submittedByNames", submittedByNames);
        model.addAttribute("risks", risks);
        model.addAttribute("assignmentsByRiskId", assignmentsByRiskId);
        model.addAttribute("currentUserId", user.getUserId());
        model.addAttribute("categories", categoryRepo.findAll());

        return "risk/list";
    }
}