package com.example.riskmanagementsystem.service;

import com.example.riskmanagementsystem.model.MitigationAssignment;
import com.example.riskmanagementsystem.model.Risk;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo.MitigationAssignmentRepository;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.RiskRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MitigationAssignmentService {

    private final MitigationAssignmentRepository assignmentRepo;
    private final RiskRepository riskRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final RiskService riskService;
    private final OrganizationMemberRepository memberRepo;

    public MitigationAssignmentService(MitigationAssignmentRepository assignmentRepo,
                                       RiskRepository riskRepo,
                                       UserRepository userRepo,
                                       NotificationService notificationService,
                                       RiskService riskService,
                                       OrganizationMemberRepository memberRepo) {
        this.assignmentRepo = assignmentRepo;
        this.riskRepo = riskRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
        this.riskService = riskService;
        this.memberRepo = memberRepo;
    }

    // assign a mitigation action to a user for a specific risk
    public void assignAction(Long riskId,
                             Long assignedToUserId,
                             Long assignedByUserId,
                             String actionDetails,
                             LocalDate deadline) {

        if (actionDetails == null || actionDetails.trim().isBlank()) {
            throw new IllegalArgumentException(
                    "Action details are required.");
        }

        if (actionDetails.trim().length() < 20) {
            throw new IllegalArgumentException(
                    "Action details must be at least 20 characters. "
                            + "Please be more specific.");
        }

        if (deadline == null) {
            throw new IllegalArgumentException(
                    "A deadline is required.");
        }

        if (deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Deadline cannot be in the past.");
        }

        Risk risk = riskRepo.findById(riskId)
                .orElseThrow(() -> new IllegalArgumentException("Risk not found."));

        User assignedTo = userRepo.findById(assignedToUserId)
                .orElseThrow(() -> new IllegalArgumentException("Assigned user not found."));

        User assignedBy = userRepo.findById(assignedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Assigning user not found."));

        MitigationAssignment assignment = new MitigationAssignment();
        assignment.setRisk(risk);
        assignment.setAssignedTo(assignedTo);
        assignment.setAssignedBy(assignedBy);
        assignment.setActionDetails(actionDetails);
        assignment.setDeadline(deadline);
        assignment.setStatus("ASSIGNED");

        assignmentRepo.save(assignment);

        // automatically move risk to MITIGATING
        // when first assignment is created
        riskService.markRiskAsMitigating(riskId);
    }

    // get a specific assignment by its ID
    public MitigationAssignment getAssignmentById(Long assignmentId) {
        return assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new
                        IllegalArgumentException(
                        "Assignment not found."));
    }

    // get the assignment linked to a specific risk (if it exists)
    public MitigationAssignment getAssignmentForRisk(Long riskId) {
        return assignmentRepo.findFirstByRisk_RiskId(riskId).orElse(null);
    }

    // get all assignments assigned to a specific user
    public List<MitigationAssignment> getAssignmentsForUser(Long userId) {

        // used for employee dashboard (only show their tasks)
        return assignmentRepo.findByAssignedTo_UserId(userId);
    }

    @Transactional
    public MitigationAssignment saveAssignment(
            MitigationAssignment assignment) {

        // if deadline is in the future and status is OVERDUE
        // reset it back to IN_PROGRESS
        if (assignment.getDeadline() != null
                && assignment.getDeadline().isAfter(
                LocalDate.now())
                && "OVERDUE".equalsIgnoreCase(
                assignment.getStatus())) {
            assignment.setStatus("IN_PROGRESS");
        }

        // if deadline has passed and status is not DONE
        // mark as OVERDUE
        if (assignment.getDeadline() != null
                && assignment.getDeadline().isBefore(
                LocalDate.now())
                && !"DONE".equalsIgnoreCase(
                assignment.getStatus())) {
            assignment.setStatus("OVERDUE");
        }

        MitigationAssignment saved = assignmentRepo.save(assignment);

        // if assignment is marked as DONE check if the risk
        // can be automatically resolved
        if ("DONE".equalsIgnoreCase(saved.getStatus())) {
            riskService.checkAndResolveRisk(
                    saved.getRisk().getRiskId());
        }

        return saved;
    }

    // checks if an assignment is overdue and updates status
    // and notifies immediately
    public void checkAndMarkOverdue(
            MitigationAssignment assignment) {

        LocalDate today = LocalDate.now();

        if (assignment.getDeadline() != null
                && assignment.getDeadline().isBefore(today)
                && !"DONE".equalsIgnoreCase(
                assignment.getStatus())
                && !"OVERDUE".equalsIgnoreCase(
                assignment.getStatus())) {

            assignment.setStatus("OVERDUE");
            assignmentRepo.save(assignment);

            // notify the assigned user
            notificationService.createNotification(
                    assignment.getAssignedTo(),
                    "Your mitigation task for risk '"
                            + assignment.getRisk().getRiskTitle()
                            + "' is now overdue. "
                            + "Please update your status immediately.",
                    "OVERDUE"
            );

            // notify the risk owner if one exists
            if (assignment.getRisk().getRiskOwner() != null) {
                notificationService.createNotification(
                        assignment.getRisk().getRiskOwner(),
                        "Mitigation task assigned to "
                                + assignment.getAssignedTo()
                                .getEmail()
                                + " for risk '"
                                + assignment.getRisk()
                                .getRiskTitle()
                                + "' is now overdue.",
                        "OVERDUE"
                );
            }

            // notify the organisation admin
            Long orgId = assignment.getRisk()
                    .getOrganization().getOrgId();

            memberRepo.findByOrganization_OrgIdAndStatus(
                            orgId, "APPROVED")
                    .stream()
                    .filter(m -> m.getRole() != null
                            && "ADMIN".equalsIgnoreCase(
                            m.getRole().getName()))
                    .findFirst()
                    .ifPresent(adminMember ->
                            notificationService.createNotification(
                                    adminMember.getUser(),
                                    "Mitigation task assigned to "
                                            + assignment.getAssignedTo()
                                            .getEmail()
                                            + " for risk '"
                                            + assignment.getRisk()
                                            .getRiskTitle()
                                            + "' is now overdue.",
                                    "OVERDUE"
                            ));
        }
    }
}