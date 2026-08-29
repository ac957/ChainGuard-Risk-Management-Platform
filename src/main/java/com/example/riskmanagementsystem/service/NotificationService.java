package com.example.riskmanagementsystem.service;

import com.example.riskmanagementsystem.model.*;
import com.example.riskmanagementsystem.repo.MitigationAssignmentRepository;
import com.example.riskmanagementsystem.repo.NotificationRepository;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final MitigationAssignmentRepository assignmentRepo;

    private final OrganizationMemberRepository memberRepo;

    public NotificationService(NotificationRepository notificationRepo,
                               UserRepository userRepo,
                               MitigationAssignmentRepository assignmentRepo,
                               OrganizationMemberRepository memberRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
        this.assignmentRepo = assignmentRepo;
        this.memberRepo = memberRepo;
    }

    // create a notification for a specific user
    public void createNotification(User user, String message, String type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        notificationRepo.save(notification);
    }

    // get all notifications for dashboard
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepo.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    // get unread notifications
    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepo.findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    // notify relevant organisation roles when a new risk is submitted
    public void notifyRiskSubmitted(Risk risk) {

        int score = risk.getSeverityScore();
        String message;
        String type;

        if (score >= 15) {
            message = "A new high-severity risk has been submitted: '"
                    + risk.getRiskTitle()
                    + "'. This risk requires immediate attention and mitigation planning.";
            type = "HIGH_RISK_SUBMITTED";

            // notify admins and managers
            notifyOrganisationRole(risk.getOrganization().getOrgId(), "ADMIN", message, type);
            notifyOrganisationRole(risk.getOrganization().getOrgId(), "MANAGER", message, type);

        } else if (score >= 8) {
            message = "A new moderate-severity risk has been submitted: '"
                    + risk.getRiskTitle()
                    + "'. Review is recommended to determine appropriate mitigation actions.";
            type = "MODERATE_RISK_SUBMITTED";

            // notify managers
            notifyOrganisationRole(risk.getOrganization().getOrgId(), "MANAGER", message, type);

        } else if (score >= 1) {
            message = "A new low-severity risk has been submitted: '"
                    + risk.getRiskTitle()
                    + "'. The risk has been recorded for monitoring and future review.";
            type = "LOW_RISK_SUBMITTED";

            // notify managers
            notifyOrganisationRole(risk.getOrganization().getOrgId(), "MANAGER", message, type);

        } else {
            return;
        }

        // always notify the submitter directly so users with the USER role
        // receive confirmation that their risk was received
        createNotification(
                risk.getSubmittedBy(),
                "Your risk '" + risk.getRiskTitle()
                        + "' has been submitted successfully and is awaiting review.",
                "RISK_SUBMITTED"
        );
    }

    // notify all approved members in an organisation with a specific system role
    private void notifyOrganisationRole(Long orgId, String roleName, String message, String type) {

        List<OrganizationMember> members = memberRepo.findByOrganization_OrgIdAndStatus(orgId, "APPROVED");

        for (OrganizationMember member : members) {
            if (member.getRole() != null
                    && member.getRole().getName() != null
                    && member.getRole().getName().equalsIgnoreCase(roleName)) {

                createNotification(member.getUser(), message, type);
            }
        }
    }

    // scheduled check for deadlines coming soon
    // runs method every day at 8am
    @Transactional
    @Scheduled(cron = "0 0 8 * * *")
    public void notifyDeadlineNearAssignments() {

        LocalDate targetDate = LocalDate.now().plusDays(2);

        List<MitigationAssignment> assignments = assignmentRepo.findAll();

        for (MitigationAssignment assignment : assignments) {
            if (assignment.getDeadline() != null
                    && assignment.getDeadline().isEqual(targetDate)
                    && !"DONE".equalsIgnoreCase(assignment.getStatus())) {

                String message = "Reminder: your mitigation task for risk '"
                        + assignment.getRisk().getRiskTitle()
                        + "' is due on "
                        + assignment.getDeadline();

                boolean alreadyExists = notificationRepo
                        .existsByUser_UserIdAndMessageAndType(
                                assignment.getAssignedTo().getUserId(),
                                message,
                                "DEADLINE_NEAR"
                        );

                if (!alreadyExists) {
                    createNotification(
                            assignment.getAssignedTo(),
                            message,
                            "DEADLINE_NEAR"
                    );
                }
            }
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepo
                .findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification n : unread) {
            n.setRead(true);
            notificationRepo.save(n);
        }
    }
}