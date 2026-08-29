package com.example.riskmanagementsystem.repo;

import com.example.riskmanagementsystem.model.MitigationAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MitigationAssignmentRepository extends JpaRepository<MitigationAssignment, Long> {

    List<MitigationAssignment> findByAssignedTo_UserId(Long userId);

    List<MitigationAssignment> findByRisk_RiskId(Long riskId);
    Optional<MitigationAssignment> findFirstByRisk_RiskId(Long riskId);
}