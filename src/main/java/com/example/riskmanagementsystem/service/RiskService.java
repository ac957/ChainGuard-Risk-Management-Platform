package com.example.riskmanagementsystem.service;

import com.example.riskmanagementsystem.model.MitigationAssignment;
import com.example.riskmanagementsystem.model.OrganizationMember;
import com.example.riskmanagementsystem.model.Risk;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo.MitigationAssignmentRepository;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.RiskRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RiskService {

    private final RiskRepository riskRepo;
    private final NotificationService notificationService;
    private final MitigationAssignmentRepository assignmentRepo;
    private final UserRepository userRepo;
    private final OrganizationMemberRepository memberRepo;
    private final EmailService emailService;

    public RiskService(RiskRepository riskRepo,
                       NotificationService notificationService,
                       MitigationAssignmentRepository assignmentRepo,
                       UserRepository userRepo,
                       OrganizationMemberRepository memberRepo,
                       EmailService emailService) {
        this.riskRepo = riskRepo;
        this.notificationService = notificationService;
        this.assignmentRepo = assignmentRepo;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.emailService = emailService;
    }

    // validates fields shared by saveRisk and updateRisk
    private void validateRiskFields(Risk risk) {

        if (risk.getRiskTitle() == null
                || risk.getRiskTitle().trim().length() < 5) {
            throw new IllegalArgumentException(
                    "Risk title must be at least 5 characters.");
        }
        if (!risk.getRiskTitle().trim().matches(".*[a-zA-Z].*")) {
            throw new IllegalArgumentException(
                    "Risk title must contain meaningful text.");
        }
        if (risk.getDescription() == null
                || risk.getDescription().trim().length() < 20) {
            throw new IllegalArgumentException(
                    "Description must be at least 20 characters. "
                            + "Please provide more detail.");
        }
        if (risk.getCategory() == null) {
            throw new IllegalArgumentException(
                    "Please select a risk category.");
        }
    }

    // used for creating a new risk only
    // triggers notifications and email alerts
    @Transactional
    public Risk saveRisk(Risk risk) {
        validateRiskFields(risk);

        int score = risk.getLikelihood() * risk.getImpact();
        risk.setSeverityScore(score);

        boolean isNew = (risk.getRiskId() == null); // ← capture BEFORE save
        if (isNew) {
            risk.setStatus("NEW");
        }

        Risk savedRisk = riskRepo.save(risk);

        if (isNew) {                                 // ← now always correct
            notificationService.notifyRiskSubmitted(savedRisk);

            if (savedRisk.getSeverityScore() >= 15) {
                try {
                    Long orgId = savedRisk.getOrganization().getOrgId();
                    memberRepo.findByOrganization_OrgIdAndStatus(orgId, "APPROVED")
                            .stream()
                            .filter(m -> m.getRole() != null
                                    && "ADMIN".equalsIgnoreCase(m.getRole().getName()))
                            .findFirst()
                            .ifPresent(adminMember -> {
                                String adminEmail = adminMember.getUser().getEmail();
                                String category = savedRisk.getCategory() != null
                                        ? savedRisk.getCategory().getName()
                                        : "Uncategorised";
                                emailService.sendHighSeverityAlert(
                                        adminEmail,
                                        "Admin",
                                        savedRisk.getOrganization().getOrgName(),
                                        savedRisk.getRiskTitle(),
                                        category,
                                        savedRisk.getLikelihood(),
                                        savedRisk.getImpact(),
                                        savedRisk.getSeverityScore(),
                                        savedRisk.getSubmittedBy().getEmail()
                                );
                            });
                } catch (Exception e) {
                    System.err.println("Could not send high severity alert: " + e.getMessage());
                }
            }
        }                                           // ← outer if closes here

        return savedRisk;
    }

    // used for editing an existing risk
    // updates fields only — no notifications triggered
    @Transactional
    public Risk updateRisk(Risk risk) {
        validateRiskFields(risk);
        int score = risk.getLikelihood() * risk.getImpact();
        risk.setSeverityScore(score);
        return riskRepo.save(risk);
    }

    // find a single risk by id
    public Optional<Risk> getRiskById(Long id) {
        return riskRepo.findById(id);
    }

    // manually update risk status
    @Transactional
    public void updateRiskStatus(Long riskId,
                                 String newStatus,
                                 Long currentUserId) {

        Risk risk = riskRepo.findById(riskId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Risk not found."));

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Status is required.");
        }

        List<String> validStatuses = List.of(
                "NEW", "UNDER REVIEW", "MITIGATING",
                "RESOLVED", "CLOSED");

        if (!validStatuses.contains(newStatus.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid status: " + newStatus);
        }

        // cannot resolve a risk with no assignments
        // or assignments that are not done
        if ("RESOLVED".equalsIgnoreCase(newStatus)) {
            List<MitigationAssignment> assignments =
                    assignmentRepo.findByRisk_RiskId(riskId);
            if (assignments.isEmpty()) {
                throw new IllegalArgumentException(
                        "Cannot resolve a risk with no mitigation "
                                + "assignments. Please assign mitigation "
                                + "actions first.");
            }
            boolean allDone = assignments.stream()
                    .allMatch(a -> "DONE".equalsIgnoreCase(
                            a.getStatus()));
            if (!allDone) {
                throw new IllegalArgumentException(
                        "Cannot resolve a risk until all mitigation "
                                + "assignments are marked as Done.");
            }
        }

        if ("MITIGATING".equalsIgnoreCase(newStatus)) {
            List<MitigationAssignment> assignments =
                    assignmentRepo.findByRisk_RiskId(riskId);
            if (assignments.isEmpty()) {
                throw new IllegalArgumentException(
                        "Cannot set status to Mitigating with no "
                                + "mitigation assignments. Please assign "
                                + "mitigation actions first.");
            }
        }

        // cannot close unless already resolved
        if ("CLOSED".equalsIgnoreCase(newStatus)
                && !"RESOLVED".equalsIgnoreCase(
                risk.getStatus())) {
            throw new IllegalArgumentException(
                    "Cannot close a risk that has not "
                            + "been resolved first.");
        }
        risk.setStatus(newStatus);
        riskRepo.save(risk);
    }

    // automatically resolves risk when all assignments done
    @Transactional
    public void checkAndResolveRisk(Long riskId) {

        Risk risk = riskRepo.findById(riskId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Risk not found."));

        // dont overwrite if already resolved
        if ("RESOLVED".equalsIgnoreCase(risk.getStatus())) {
            return;
        }

        List<MitigationAssignment> assignments =
                assignmentRepo.findByRisk_RiskId(riskId);

        // if there are no assignments dont auto resolve
        if (assignments.isEmpty()) {
            return;
        }

        // check if every assignment is DONE
        boolean allDone = assignments.stream()
                .allMatch(a -> "DONE".equalsIgnoreCase(
                        a.getStatus()));

        if (allDone) {
            risk.setStatus("RESOLVED");
            riskRepo.save(risk);

            // notify the person who submitted the risk
            notificationService.createNotification(
                    risk.getSubmittedBy(),
                    "All mitigation tasks for risk '"
                            + risk.getRiskTitle()
                            + "' have been completed. "
                            + "The risk has been automatically resolved.",
                    "RISK_RESOLVED"
            );
        }
    }

    // also update risk status to MITIGATING
    // when first assignment is created
    @Transactional
    public void markRiskAsMitigating(Long riskId) {

        Risk risk = riskRepo.findById(riskId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Risk not found."));

        // only update if not already further along
        if ("NEW".equalsIgnoreCase(risk.getStatus())
                || "UNDER REVIEW".equalsIgnoreCase(
                risk.getStatus())) {
            risk.setStatus("MITIGATING");
            riskRepo.save(risk);
        }
    }

    public List<Risk> getRisksByOrganization(Long orgId) {
        return riskRepo.findByOrganization_OrgId(orgId);
    }
    public List<Risk> getRisksSubmittedByUser(Long userId) {
        return riskRepo.findBySubmittedBy_UserId(userId);
    }

    @Transactional
    public void assignRiskOwner(Long riskId, Long ownerUserId,
                                User admin) {

        Risk risk = riskRepo.findById(riskId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Risk not found."));

        User owner = userRepo.findById(ownerUserId)
                .orElseThrow(() -> new
                        IllegalArgumentException("User not found."));

        // verify the assigned owner is a manager in
        // the same organisation
        OrganizationMember ownerMembership = memberRepo
                .findFirstByUser_UserIdAndStatus(
                        ownerUserId, "APPROVED")
                .orElseThrow(() -> new
                        IllegalArgumentException(
                        "User is not an approved member."));

        if (!"MANAGER".equalsIgnoreCase(
                ownerMembership.getRole().getName())) {
            throw new IllegalArgumentException(
                    "Risk owner must be a manager.");
        }

        risk.setRiskOwner(owner);

        // move status to UNDER REVIEW when owner is assigned
        if ("NEW".equalsIgnoreCase(risk.getStatus())) {
            risk.setStatus("UNDER REVIEW");
        }

        riskRepo.save(risk);

        // notify the assigned manager
        notificationService.createNotification(
                owner,
                "You have been assigned as the risk owner for '"
                        + risk.getRiskTitle()
                        + "'. You are responsible for overseeing the "
                        + "mitigation of this risk.",
                "RISK_OWNER_ASSIGNED"
        );
    }
}