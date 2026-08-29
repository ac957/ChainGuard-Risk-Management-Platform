package com.example.riskmanagementsystem.service;

import com.example.riskmanagementsystem.model.Organization;
import com.example.riskmanagementsystem.model.OrganizationMember;
import com.example.riskmanagementsystem.model.Role;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.OrganizationRepository;
import com.example.riskmanagementsystem.repo.RiskRepository;
import com.example.riskmanagementsystem.repo.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository orgRepo;
    private final OrganizationMemberRepository memberRepo;
    private final RoleRepository roleRepo;

    private final EmailService emailService;

    private final NotificationService notificationService;
    private final RiskRepository riskRepo;

    public OrganizationService(OrganizationRepository orgRepo,
                               OrganizationMemberRepository memberRepo,
                               RoleRepository roleRepo,
                               EmailService emailService,
                               NotificationService notificationService,
                               RiskRepository riskRepo) {
        this.orgRepo = orgRepo;
        this.memberRepo = memberRepo;
        this.roleRepo = roleRepo;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.riskRepo = riskRepo;
    }

    // CREATE ORG (creator is admin)
    @Transactional
    public Organization createOrganization(User creator, String orgName, String specialisation) {

        String cleanedName = orgName == null ? null : orgName.trim();
        String cleanedSpecialisation = specialisation == null ? null : specialisation.trim();

        if (cleanedName == null || cleanedName.isBlank()) {
            throw new IllegalArgumentException("Organisation name is required.");
        }
        if (orgRepo.existsByOrgName(cleanedName)) {
            throw new IllegalArgumentException("That organisation name is already in use.");
        }

        Organization org = new Organization();
        org.setOrgName(cleanedName);
        org.setSpecialisation(cleanedSpecialisation);
        org.setCreatedBy(creator);

        orgRepo.save(org);

        Role adminRole = roleRepo.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found in roles table."));

        OrganizationMember m = new OrganizationMember();
        m.setOrganization(org);
        m.setUser(creator);
        m.setRole(adminRole);
        m.setStatus("APPROVED");

        memberRepo.save(m);

        return org;
    }

    // JOIN ORG REQUEST (pending)
    @Transactional
    public void requestToJoin(User user, Long orgId) {

        if (orgId == null) {
            throw new IllegalArgumentException(
                    "Please select an organisation.");
        }

        Organization org = orgRepo.findById(orgId)
                .orElseThrow(() -> new
                        IllegalArgumentException(
                        "Organisation not found."));

        boolean alreadyMember = memberRepo
                .existsByOrganization_OrgIdAndUser_UserId(
                        orgId, user.getUserId());
        if (alreadyMember) {
            throw new IllegalArgumentException(
                    "You have already requested to join "
                            + "(or you are already a member of) "
                            + "this organisation.");
        }

        // check total rejection count across all organisations
        // block account after 3 rejections
        int rejectionCount = memberRepo
                .countByUser_UserIdAndStatus(
                        user.getUserId(), "REJECTED");

        if (rejectionCount >= 3) {
            throw new IllegalArgumentException(
                    "Your account has been restricted from "
                            + "making further join requests. You have "
                            + "been rejected from 3 organisations. "
                            + "Please contact support for assistance.");
        }

        Role userRole = roleRepo.findByName("USER")
                .orElseThrow(() -> new
                        IllegalStateException(
                        "USER role not found in roles table."));

        OrganizationMember m = new OrganizationMember();
        m.setOrganization(org);
        m.setUser(user);
        m.setRole(userRole);
        m.setStatus("PENDING");

        memberRepo.save(m);
    }

    // ADMIN: list pending requests
    @Transactional(readOnly = true)
    public List<OrganizationMember> getPendingRequestsForAdmin(User admin) {

        // Find organisations where this user is an APPROVED ADMIN
        List<OrganizationMember> adminMemberships =
                memberRepo.findByUser_UserIdAndStatusAndRole_Name(
                        admin.getUserId(), "APPROVED", "ADMIN"
                );

        List<OrganizationMember> pending = new ArrayList<>();

        // For each org, collect pending requests
        for (OrganizationMember adminMem : adminMemberships) {
            Long orgId = adminMem.getOrganization().getOrgId();
            pending.addAll(
                    memberRepo.findByOrganization_OrgIdAndStatusOrderByJoinedAtDesc(orgId, "PENDING")
            );
        }

        return pending;
    }

    @Transactional(readOnly = true)
    public long getPendingRequestsCount(User admin) {
        return getPendingRequestsForAdmin(admin).size();
    }


    // ADMIN: approve request + role
    @Transactional
    public void approveMembership(User admin, Long membershipId, String roleName) {

        if (membershipId == null) {
            throw new IllegalArgumentException("Missing membership ID.");
        }
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Please select a role.");
        }

        OrganizationMember m = memberRepo.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found."));

        if (!"PENDING".equalsIgnoreCase(m.getStatus())) {
            throw new IllegalArgumentException("This request is not pending.");
        }

        Long orgId = m.getOrganization().getOrgId();

        boolean isAdmin = memberRepo.existsByUser_UserIdAndOrganization_OrgIdAndStatusAndRole_Name(
                admin.getUserId(), orgId, "APPROVED", "ADMIN"
        );

        if (!isAdmin) {
            throw new IllegalArgumentException("You are not allowed to approve requests for this organisation.");
        }

        Role newRole = roleRepo.findByName(roleName.trim())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        // Only allow these assignments for now
        String rn = newRole.getName();
        if (!"MANAGER".equals(rn) && !"USER".equals(rn)) {
            throw new IllegalArgumentException("You can only assign MANAGER or USER.");
        }

        m.setRole(newRole);
        m.setStatus("APPROVED");
        memberRepo.save(m);

        // send approval email to the approved user
        try {
            String userEmail = m.getUser().getEmail();
            String orgName = m.getOrganization().getOrgName();
            String approvedRole = m.getRole().getName();

            // get full name if available, fall back to email
            String fullName = userEmail;
            emailService.sendApprovalEmail(
                    userEmail, fullName, orgName, approvedRole);
        } catch (Exception e) {
            System.err.println(
                    "Could not send approval email: " + e.getMessage());
        }
    }

    // ADMIN: reject request
    @Transactional
    public void rejectMembership(User admin, Long membershipId) {

        if (membershipId == null) {
            throw new IllegalArgumentException(
                    "Missing membership ID.");
        }

        OrganizationMember m = memberRepo.findById(membershipId)
                .orElseThrow(() -> new
                        IllegalArgumentException(
                        "Join request not found."));

        if (!"PENDING".equalsIgnoreCase(m.getStatus())) {
            throw new IllegalArgumentException(
                    "This request is not pending.");
        }

        Long orgId = m.getOrganization().getOrgId();

        boolean isAdmin = memberRepo
                .existsByUser_UserIdAndOrganization_OrgIdAndStatusAndRole_Name(
                        admin.getUserId(), orgId,
                        "APPROVED", "ADMIN");

        if (!isAdmin) {
            throw new IllegalArgumentException(
                    "You are not allowed to reject requests "
                            + "for this organisation.");
        }

        // set status to REJECTED — keeps record in database
        // so user cannot rejoin the same organisation
        m.setStatus("REJECTED");
        memberRepo.save(m);

        // send rejection email instead of in-system notification
        try {
            emailService.sendRejectionEmail(
                    m.getUser().getEmail(),
                    m.getOrganization().getOrgName());
        } catch (Exception e) {
            System.err.println(
                    "Failed to send rejection email: "
                            + e.getMessage());
        }
    }

    // get all approved members in the admin's organisation
    @Transactional(readOnly = true)
    public List<OrganizationMember> getApprovedMembers(User admin) {

        OrganizationMember adminMembership = memberRepo
                .findFirstByUser_UserIdAndStatus(
                        admin.getUserId(), "APPROVED")
                .orElseThrow(() -> new
                        IllegalStateException(
                        "Admin organisation not found."));

        Long orgId = adminMembership.getOrganization().getOrgId();

        return memberRepo
                .findByOrganization_OrgIdAndStatus(
                        orgId, "APPROVED")
                .stream()
                .filter(m -> !m.getUser().getUserId()
                        .equals(admin.getUserId()))
                .collect(java.util.stream.Collectors.toList());
    }

    // change a member's role
    @Transactional
    public void changeMemberRole(User admin,
                                 Long membershipId,
                                 String newRole) {

        OrganizationMember m = memberRepo
                .findById(membershipId)
                .orElseThrow(() -> new
                        IllegalArgumentException(
                        "Member not found."));

        Long orgId = m.getOrganization().getOrgId();

        boolean isAdmin = memberRepo
                .existsByUser_UserIdAndOrganization_OrgIdAndStatusAndRole_Name(
                        admin.getUserId(), orgId,
                        "APPROVED", "ADMIN");

        if (!isAdmin) {
            throw new IllegalArgumentException(
                    "You are not authorised to change "
                            + "roles in this organisation.");
        }

        if (!"USER".equals(newRole)
                && !"MANAGER".equals(newRole)) {
            throw new IllegalArgumentException(
                    "Invalid role. Must be USER or MANAGER.");
        }

        String oldRole = m.getRole().getName();
        Role role = roleRepo.findByName(newRole)
                .orElseThrow(() -> new
                        IllegalArgumentException(
                        "Role not found."));

        m.setRole(role);
        memberRepo.save(m);

        // if user is being demoted from manager to user
        // remove them as risk owner from any risks they own
        if ("USER".equalsIgnoreCase(newRole)
                && "MANAGER".equalsIgnoreCase(oldRole)) {
            riskRepo.findByRiskOwner_UserId(
                            m.getUser().getUserId())
                    .forEach(risk -> {
                        risk.setRiskOwner(null);
                        risk.setStatus("UNDER REVIEW");
                        riskRepo.save(risk);
                    });
        }

        // notify member of role change
        notificationService.createNotification(
                m.getUser(),
                "Your role in "
                        + m.getOrganization().getOrgName()
                        + " has been updated from "
                        + oldRole + " to " + newRole + ".",
                "ROLE_CHANGED"
        );

        // send email notification
        emailService.sendRoleChangeEmail(
                m.getUser().getEmail(),
                m.getOrganization().getOrgName(),
                oldRole,
                newRole);
    }

    // remove a member from the organisation
    @Transactional
    public void removeMember(User admin, Long membershipId) {

        OrganizationMember m = memberRepo
                .findById(membershipId)
                .orElseThrow(() -> new
                        IllegalArgumentException(
                        "Member not found."));

        Long orgId = m.getOrganization().getOrgId();

        boolean isAdmin = memberRepo
                .existsByUser_UserIdAndOrganization_OrgIdAndStatusAndRole_Name(
                        admin.getUserId(), orgId,
                        "APPROVED", "ADMIN");

        if (!isAdmin) {
            throw new IllegalArgumentException(
                    "You are not authorised to remove "
                            + "members from this organisation.");
        }

        String orgName = m.getOrganization().getOrgName();
        String memberEmail = m.getUser().getEmail();

        // send email before deleting
        emailService.sendRemovalEmail(
                memberEmail, orgName);

        // delete the membership record
        memberRepo.delete(m);
    }
}
