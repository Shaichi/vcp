package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.ActivityLog;
import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Optional<ActivityLog> findByExternalReferenceAndPartnerId(String externalReference, String partnerId);
    long countByCitizenAndActivityCodeAndFiscalYear(Citizen citizen, String activityCode, int fiscalYear);
    List<ActivityLog> findByCitizenAndActivityCodeAndFiscalYear(Citizen citizen, String activityCode, int fiscalYear);
}
