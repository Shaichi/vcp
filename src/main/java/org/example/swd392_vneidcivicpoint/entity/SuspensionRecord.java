package org.example.swd392_vneidcivicpoint.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.swd392_vneidcivicpoint.constants.SuspensionType;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "SUSPENSION_RECORD")
public class SuspensionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suspension_id")
    private Long suspensionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @Enumerated(EnumType.STRING)
    @Column(name = "suspension_type", nullable = false, length = 20)
    private SuspensionType suspensionType;

    @Column(name = "reason_code", nullable = false, length = 100)
    private String reasonCode;

    @Column(name = "violation_level", length = 20)
    private String violationLevel;

    @Column(name = "suspended_at", nullable = false)
    private LocalDateTime suspendedAt;

    @Column(name = "auto_resume_date")
    private LocalDateTime autoResumeDate;

    @Column(name = "resumed_at")
    private LocalDateTime resumedAt;

    @Column(name = "resume_source", length = 20)
    private String resumeSource;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
