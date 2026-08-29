package com.example.riskmanagementsystem.service;

import com.example.riskmanagementsystem.model.*;
import com.example.riskmanagementsystem.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepo;
    @Mock private UserRepository userRepo;
    @Mock private MitigationAssignmentRepository assignmentRepo;
    @Mock private OrganizationMemberRepository memberRepo;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Organization org;
    private Risk risk;
    private OrganizationMember adminMember;
    private OrganizationMember managerMember;
    private Role adminRole;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        org = new Organization();
        org.setOrgId(1L);

        adminRole = new Role();
        adminRole.setName("ADMIN");

        managerRole = new Role();
        managerRole.setName("MANAGER");

        User adminUser = new User();
        adminUser.setUserId(2L);

        User managerUser = new User();
        managerUser.setUserId(3L);

        adminMember = new OrganizationMember();
        adminMember.setUser(adminUser);
        adminMember.setRole(adminRole);
        adminMember.setOrganization(org);

        managerMember = new OrganizationMember();
        managerMember.setUser(managerUser);
        managerMember.setRole(managerRole);
        managerMember.setOrganization(org);

        risk = new Risk();
        risk.setRiskId(1L);
        risk.setRiskTitle("Test Risk");
        risk.setOrganization(org);
        risk.setSubmittedBy(user);

        when(notificationRepo.save(any(Notification.class)))
                .thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void createNotification_shouldSaveNotification() {
        notificationService.createNotification(
                user, "Test message", "RISK_SUBMITTED");
        verify(notificationRepo, times(1))
                .save(any(Notification.class));
    }

    @Test
    void createNotification_shouldSetReadToFalse() {
        notificationService.createNotification(
                user, "Test message", "RISK_SUBMITTED");
        verify(notificationRepo).save(
                argThat(n -> !n.isRead()));
    }

    @Test
    void notifyRiskSubmitted_shouldNotifyAdminAndManagerForHighSeverity() {
        risk.setSeverityScore(15);
        when(memberRepo.findByOrganization_OrgIdAndStatus(
                1L, "APPROVED"))
                .thenReturn(List.of(adminMember, managerMember));
        notificationService.notifyRiskSubmitted(risk);
        verify(notificationRepo, times(2))
                .save(any(Notification.class));
    }

    @Test
    void notifyRiskSubmitted_shouldOnlyNotifyManagerForMediumSeverity() {
        risk.setSeverityScore(10);
        when(memberRepo.findByOrganization_OrgIdAndStatus(
                1L, "APPROVED"))
                .thenReturn(List.of(adminMember, managerMember));
        notificationService.notifyRiskSubmitted(risk);
        verify(notificationRepo, times(1))
                .save(any(Notification.class));
    }

    @Test
    void notifyRiskSubmitted_shouldOnlyNotifyManagerForLowSeverity() {
        risk.setSeverityScore(4);
        when(memberRepo.findByOrganization_OrgIdAndStatus(
                1L, "APPROVED"))
                .thenReturn(List.of(adminMember, managerMember));
        notificationService.notifyRiskSubmitted(risk);
        verify(notificationRepo, times(1))
                .save(any(Notification.class));
    }

    @Test
    void notifyRiskSubmitted_shouldUseHighRiskTypeForHighSeverity() {
        risk.setSeverityScore(20);
        when(memberRepo.findByOrganization_OrgIdAndStatus(
                1L, "APPROVED"))
                .thenReturn(List.of(adminMember));
        notificationService.notifyRiskSubmitted(risk);
        verify(notificationRepo).save(
                argThat(n -> "HIGH_RISK_SUBMITTED"
                        .equals(n.getType())));
    }

    @Test
    void notifyRiskSubmitted_shouldUseModerateTypeForMediumSeverity() {
        risk.setSeverityScore(10);
        when(memberRepo.findByOrganization_OrgIdAndStatus(
                1L, "APPROVED"))
                .thenReturn(List.of(managerMember));
        notificationService.notifyRiskSubmitted(risk);
        verify(notificationRepo).save(
                argThat(n -> "MODERATE_RISK_SUBMITTED"
                        .equals(n.getType())));
    }

    @Test
    void notifyRiskSubmitted_shouldUseLowTypeForLowSeverity() {
        risk.setSeverityScore(4);
        when(memberRepo.findByOrganization_OrgIdAndStatus(
                1L, "APPROVED"))
                .thenReturn(List.of(managerMember));
        notificationService.notifyRiskSubmitted(risk);
        verify(notificationRepo).save(
                argThat(n -> "LOW_RISK_SUBMITTED"
                        .equals(n.getType())));
    }

    @Test
    void markAllAsRead_shouldMarkAllUnreadNotifications() {
        Notification n1 = new Notification();
        n1.setRead(false);
        Notification n2 = new Notification();
        n2.setRead(false);
        when(notificationRepo
                .findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n1, n2));
        notificationService.markAllAsRead(1L);
        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
    }

    @Test
    void markAllAsRead_shouldDoNothingWhenNoUnread() {
        when(notificationRepo
                .findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());
        notificationService.markAllAsRead(1L);
        verify(notificationRepo, never())
                .save(any(Notification.class));
    }
}
