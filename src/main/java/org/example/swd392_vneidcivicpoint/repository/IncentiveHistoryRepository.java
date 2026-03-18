package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.ExportBatch;
import org.example.swd392_vneidcivicpoint.entity.IncentiveHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncentiveHistoryRepository extends JpaRepository<IncentiveHistory, Long> {
    boolean existsByCitizenAndBatchAndTaxType(Citizen citizen, ExportBatch batch, String taxType);
    List<IncentiveHistory> findByCitizen(Citizen citizen);
}
