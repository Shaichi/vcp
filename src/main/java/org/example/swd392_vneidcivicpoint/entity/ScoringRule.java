package org.example.swd392_vneidcivicpoint.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.swd392_vneidcivicpoint.constants.ActivityCategory;
import org.example.swd392_vneidcivicpoint.constants.RuleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "SCORING_RULE")
public class ScoringRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "activity_code", nullable = false, length = 50)
    private String activityCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_category", nullable = false, length = 100)
    private ActivityCategory activityCategory;

    @Column(name = "activity_name", nullable = false, length = 255)
    private String activityName;

    @Column(name = "point_value", nullable = false)
    private Integer pointValue;

    @Column(name = "frequency_limit")
    private Integer frequencyLimit;

    @Column(name = "frequency_period", length = 20)
    private String frequencyPeriod; // LIFETIME, ANNUAL, MONTHLY, WEEKLY

    @Column(name = "category_annual_cap", precision = 10, scale = 2)
    private BigDecimal categoryAnnualCap;

    @Column(name = "overall_annual_cap", precision = 10, scale = 2)
    private BigDecimal overallAnnualCap;

    @Column(name = "retention_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal retentionRate = new BigDecimal("30.00");

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RuleStatus status = RuleStatus.ACTIVE;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AdminUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private AdminUser approvedBy;

    @Column(name = "approval_status", nullable = false, length = 20)
    private String approvalStatus = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
