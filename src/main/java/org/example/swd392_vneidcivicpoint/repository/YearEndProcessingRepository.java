package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.YearEndProcessing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YearEndProcessingRepository extends JpaRepository<YearEndProcessing, Long> {
    boolean existsByCitizenAndFiscalYear(Citizen citizen, int fiscalYear);
    List<YearEndProcessing> findByFiscalYearAndStatus(int fiscalYear, String status);
}
