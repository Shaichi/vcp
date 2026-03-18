package org.example.swd392_vneidcivicpoint.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "EXPORT_BATCH")
public class ExportBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "export_date", nullable = false)
    private LocalDateTime exportDate;

    @Column(name = "total_records", nullable = false)
    private Integer totalRecords;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "is_supplementary", nullable = false)
    private Boolean isSupplementary = false;

    @Column(name = "triggered_by", nullable = false, length = 20)
    private String triggeredBy; // SCHEDULED, ADMIN_MANUAL

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
