package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.PointLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {
    List<PointLedger> findByCitizenOrderByCreatedAtDesc(Citizen citizen);

    @Query("SELECT COALESCE(SUM(pl.pointsAwarded), 0) FROM PointLedger pl WHERE pl.citizen = :citizen AND pl.fiscalYear = :fiscalYear")
    BigDecimal sumPointsByFiscalYear(@Param("citizen") Citizen citizen, @Param("fiscalYear") int fiscalYear);
}
