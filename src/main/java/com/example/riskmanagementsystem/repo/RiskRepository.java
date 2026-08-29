package com.example.riskmanagementsystem.repo;

import com.example.riskmanagementsystem.model.Risk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskRepository extends JpaRepository<Risk, Long> {

    List<Risk> findByOrganization_OrgId(Long orgId);

    List<Risk> findBySubmittedBy_UserId(Long userId);

    List<Risk> findByRiskOwner_UserId(Long userId);

}