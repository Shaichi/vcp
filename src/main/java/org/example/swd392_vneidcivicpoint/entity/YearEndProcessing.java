package org.example.swd392_vneidcivicpoint.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "YEAR_END_PROCESSING")
public class YearEndProcessing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "processing_id")
    private Long processingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "previous_total_points", nullable = false, precision = 10, scale = 2)
    private BigDecimal previousTotalPoints;

    @Column(name = "carryover_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal carryoverAmount;

    @Column(name = "retention_rate_applied", nullable = false, precision = 5, scale = 2)
    private BigDecimal retentionRateApplied;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;

    @Column(nullable = false, length = 20)
    private String status = "SUCCESS";

    @PrePersist
    protected void onCreate() {
        processedAt = LocalDateTime.now();
    }
}
