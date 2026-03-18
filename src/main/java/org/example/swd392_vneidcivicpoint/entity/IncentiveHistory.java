package org.example.swd392_vneidcivicpoint.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "INCENTIVE_HISTORY")
public class IncentiveHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ExportBatch batch;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "tax_type", nullable = false, length = 50)
    private String taxType;

    @Column(name = "reduction_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal reductionRate;

    @Column(name = "rank_name_at_export", nullable = false, length = 20)
    private String rankNameAtExport;

    @Column(name = "confirmation_reference", length = 255)
    private String confirmationReference;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "confirmation_source", length = 100)
    private String confirmationSource;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
