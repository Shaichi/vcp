package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.RankHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankHistoryRepository extends JpaRepository<RankHistory, Long> {
    List<RankHistory> findByCitizenOrderByEvaluatedAtDesc(Citizen citizen);
}
