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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MitigationAssignmentServiceTest {

    @Mock private MitigationAssignmentRepository assignmentRepo;
    @Mock private RiskRepository riskRepo;
    @Mock private UserRepository userRepo;
    @Mock private NotificationService notificationService;
    @Mock private RiskService riskService;
    @Mock private OrganizationMemberRepository memberRepo;

    @InjectMocks
    private MitigationAssignmentService assignmentService;

    private Risk risk;
    private User assignedTo;
    private User assignedBy;
    private MitigationAssignment assignment;

    @BeforeEach
    void setUp() {
        risk = new Risk();
        risk.setRiskId(1L);
        risk.setRiskTitle("Test Risk");

        assignedTo = new User();
        assignedTo.setUserId(2L);

        assignedBy = new User();
        assignedBy.setUserId(3L);

        assignment = new MitigationAssignment();
        assignment.setRisk(risk);
        assignment.setAssignedTo(assignedTo);
        assignment.setAssignedBy(assignedBy);
        assignment.setActionDetails(
                "This is a valid action detail description");
        assignment.setDeadline(
                LocalDate.now().plusDays(7));
        assignment.setStatus("ASSIGNED");

        when(riskRepo.findById(1L))
                .thenReturn(Optional.of(risk));
        when(userRepo.findById(2L))
                .thenReturn(Optional.of(assignedTo));
        when(userRepo.findById(3L))
                .thenReturn(Optional.of(assignedBy));
        when(assignmentRepo.save(any(
                MitigationAssignment.class)))
                .thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void assignAction_shouldThrowWhenActionDetailsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.assignAction(
                        1L, 2L, 3L, null,
                        LocalDate.now().plusDays(7)));
    }

    @Test
    void assignAction_shouldThrowWhenActionDetailsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.assignAction(
                        1L, 2L, 3L, "   ",
                        LocalDate.now().plusDays(7)));
    }

    @Test
    void assignAction_shouldThrowWhenActionDetailsTooShort() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.assignAction(
                        1L, 2L, 3L, "Too short",
                        LocalDate.now().plusDays(7)));
    }

    @Test
    void assignAction_shouldThrowWhenDeadlineIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.assignAction(
                        1L, 2L, 3L,
                        "This is a valid action detail description",
                        null));
    }

    @Test
    void assignAction_shouldThrowWhenDeadlineIsInPast() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.assignAction(
                        1L, 2L, 3L,
                        "This is a valid action detail description",
                        LocalDate.now().minusDays(1)));
    }

    @Test
    void assignAction_shouldPassWithValidInputs() {
        assertDoesNotThrow(() ->
                assignmentService.assignAction(
                        1L, 2L, 3L,
                        "This is a valid action detail description",
                        LocalDate.now().plusDays(7)));
    }

    @Test
    void saveAssignment_shouldResetOverdueWhenDeadlineExtended() {
        assignment.setStatus("OVERDUE");
        assignment.setDeadline(LocalDate.now().plusDays(7));
        MitigationAssignment saved =
                assignmentService.saveAssignment(assignment);
        assertEquals("IN_PROGRESS", saved.getStatus());
    }

    @Test
    void saveAssignment_shouldMarkOverdueWhenDeadlinePassed() {
        assignment.setStatus("ASSIGNED");
        assignment.setDeadline(
                LocalDate.now().minusDays(1));
        MitigationAssignment saved =
                assignmentService.saveAssignment(assignment);
        assertEquals("OVERDUE", saved.getStatus());
    }

    @Test
    void saveAssignment_shouldNotChangeStatusWhenDone() {
        assignment.setStatus("DONE");
        assignment.setDeadline(
                LocalDate.now().minusDays(1));
        MitigationAssignment saved =
                assignmentService.saveAssignment(assignment);
        assertEquals("DONE", saved.getStatus());
    }

    @Test
    void saveAssignment_shouldAutoResolveRiskWhenDone() {
        assignment.setStatus("DONE");
        assignmentService.saveAssignment(assignment);
        verify(riskService, times(1))
                .checkAndResolveRisk(1L);
    }
}