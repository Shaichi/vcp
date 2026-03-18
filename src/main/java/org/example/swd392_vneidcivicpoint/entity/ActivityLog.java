package org.example.swd392_vneidcivicpoint.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.swd392_vneidcivicpoint.constants.IngestionStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ACTIVITY_LOG")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @Column(name = "activity_code", nullable = false, length = 50)
    private String activityCode;

    @Column(name = "source_system", nullable = false, length = 50)
    private String sourceSystem;

    @Column(name = "external_reference", length = 255)
    private String externalReference;

    @Column(name = "partner_id", length = 100)
    private String partnerId;

    @Column(name = "activity_date", nullable = false)
    private LocalDateTime activityDate;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "ingestion_status", nullable = false, length = 30)
    private IngestionStatus ingestionStatus;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "forwarded_to_engine", nullable = false)
    private Boolean forwardedToEngine = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
