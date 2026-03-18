package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.IncentivePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncentivePolicyRepository extends JpaRepository<IncentivePolicy, Long> {
    List<IncentivePolicy> findByValidToIsNullAndApprovalStatusAndStatus(String approvalStatus, String status);
}
