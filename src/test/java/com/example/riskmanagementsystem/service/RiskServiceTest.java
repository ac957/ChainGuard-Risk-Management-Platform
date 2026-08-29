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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RiskServiceTest {

    @Mock private RiskRepository riskRepo;
    @Mock private OrganizationMemberRepository memberRepo;
    @Mock private NotificationService notificationService;

    @Mock private MitigationAssignmentRepository assignmentRepo;

    @InjectMocks private RiskService riskService;

    private Risk validRisk;
    private Organization org;
    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        org = new Organization();
        org.setOrgId(1L);

        category = new Category();
        category.setCategoryId(1L);
        category.setName("Supplier Risk");

        validRisk = new Risk();
        validRisk.setRiskTitle("Valid Risk Title");
        validRisk.setDescription(
                "This is a valid description that is long enough");
        validRisk.setLikelihood(3);
        validRisk.setImpact(4);
        validRisk.setCategory(category);
        validRisk.setOrganization(org);
        validRisk.setSubmittedBy(user);

        when(riskRepo.save(any(Risk.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(memberRepo.findByOrganization_OrgIdAndStatus(
                any(), any())).thenReturn(List.of());
    }

    @Test
    void saveRisk_shouldThrowWhenTitleIsNull() {
        validRisk.setRiskTitle(null);
        assertThrows(IllegalArgumentException.class,
                () -> riskService.saveRisk(validRisk));
    }

    @Test
    void saveRisk_shouldThrowWhenTitleTooShort() {
        validRisk.setRiskTitle("Hi");
        assertThrows(IllegalArgumentException.class,
                () -> riskService.saveRisk(validRisk));
    }

    @Test
    void saveRisk_shouldThrowWhenDescriptionIsNull() {
        validRisk.setDescription(null);
        assertThrows(IllegalArgumentException.class,
                () -> riskService.saveRisk(validRisk));
    }

    @Test
    void saveRisk_shouldThrowWhenDescriptionTooShort() {
        validRisk.setDescription("Too short");
        assertThrows(IllegalArgumentException.class,
                () -> riskService.saveRisk(validRisk));
    }

    @Test
    void saveRisk_shouldThrowWhenCategoryIsNull() {
        validRisk.setCategory(null);
        assertThrows(IllegalArgumentException.class,
                () -> riskService.saveRisk(validRisk));
    }

    @Test
    void saveRisk_shouldCalculateSeverityScore() {
        Risk saved = riskService.saveRisk(validRisk);
        assertEquals(12, saved.getSeverityScore());
    }

    @Test
    void saveRisk_shouldSetStatusToNew() {
        Risk saved = riskService.saveRisk(validRisk);
        assertEquals("NEW", saved.getStatus());
    }

    @Test
    void saveRisk_shouldPassWithValidFields() {
        assertDoesNotThrow(() -> riskService.saveRisk(validRisk));
    }
    @Test
    void updateRiskStatus_shouldThrowWhenResolvingWithNoAssignments() {
        Risk risk = new Risk();
        risk.setRiskId(1L);
        risk.setStatus("MITIGATING");
        risk.setOrganization(org);

        when(riskRepo.findById(1L))
                .thenReturn(Optional.of(risk));
        when(assignmentRepo.findByRisk_RiskId(1L))
                .thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> riskService.updateRiskStatus(
                        1L, "RESOLVED", user.getUserId()));
    }

    @Test
    void updateRiskStatus_shouldThrowWhenClosingUnresolvedRisk() {
        Risk risk = new Risk();
        risk.setRiskId(1L);
        risk.setStatus("MITIGATING");
        risk.setOrganization(org);

        when(riskRepo.findById(1L))
                .thenReturn(Optional.of(risk));

        assertThrows(IllegalArgumentException.class,
                () -> riskService.updateRiskStatus(
                        1L, "CLOSED", user.getUserId()));
    }
}