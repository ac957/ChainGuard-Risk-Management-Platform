package com.example.riskmanagementsystem.repo;

import com.example.riskmanagementsystem.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByOrgName(String orgName);
}
