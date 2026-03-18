package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.constants.RuleStatus;
import org.example.swd392_vneidcivicpoint.entity.ScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoringRuleRepository extends JpaRepository<ScoringRule, Long> {
    Optional<ScoringRule> findByActivityCodeAndStatusAndValidToIsNull(String code, RuleStatus status);
    List<ScoringRule> findByStatusAndApprovalStatus(RuleStatus status, String approvalStatus);
}
