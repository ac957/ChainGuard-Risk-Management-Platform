package com.example.riskmanagementsystem.repo;

import com.example.riskmanagementsystem.model.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    boolean existsByOrganization_OrgIdAndUser_UserId(Long orgId, Long userId);

    // True if user is an APPROVED ORG_ADMIN for the given org
    boolean existsByUser_UserIdAndOrganization_OrgIdAndStatusAndRole_Name(
            Long userId, Long orgId, String status, String roleName
    );

    // Find the org memberships where this user is an APPROVED ORG_ADMIN
    List<OrganizationMember> findByUser_UserIdAndStatusAndRole_Name(
            Long userId, String status, String roleName
    );

    // Pending requests for a specific org (newest first)
    List<OrganizationMember> findByOrganization_OrgIdAndStatusOrderByJoinedAtDesc(
            Long orgId, String status
    );

    boolean existsByUser_UserIdAndStatus(Long userId, String status);

    // Finds the first organisation membership for a user with a specific status (e.g. APPROVED)
    // Used to get the organisation the user belongs to when creating risks
    Optional<OrganizationMember> findFirstByUser_UserIdAndStatus(Long userId, String status);

    // get all approved members in one organisation
    List<OrganizationMember> findByOrganization_OrgIdAndStatus(Long orgId, String status);

    int countByUser_UserIdAndStatus(Long userId, String status);
}
