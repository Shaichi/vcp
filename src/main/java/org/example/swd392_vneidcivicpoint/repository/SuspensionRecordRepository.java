package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.SuspensionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuspensionRecordRepository extends JpaRepository<SuspensionRecord, Long> {
    Optional<SuspensionRecord> findByCitizenAndIsActiveTrue(Citizen citizen);
    List<SuspensionRecord> findByIsActiveTrueAndAutoResumeDateBefore(LocalDateTime dateTime);
}
