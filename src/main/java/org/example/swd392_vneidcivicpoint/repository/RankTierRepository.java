package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.RankTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankTierRepository extends JpaRepository<RankTier, Long> {
    List<RankTier> findByValidToIsNullAndApprovalStatus(String approvalStatus);
}
