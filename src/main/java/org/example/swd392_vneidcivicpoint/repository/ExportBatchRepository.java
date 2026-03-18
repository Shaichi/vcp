package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.ExportBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportBatchRepository extends JpaRepository<ExportBatch, Long> {
    List<ExportBatch> findByFiscalYearOrderByExportDateDesc(int fiscalYear);
}
