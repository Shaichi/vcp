package org.example.swd392_vneidcivicpoint.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "VULNERABLE_BONUS")
public class VulnerableBonus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bonus_id")
    private Long bonusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "bonus_points_awarded", nullable = false, precision = 10, scale = 2)
    private BigDecimal bonusPointsAwarded;

    @Column(name = "bonus_points_configured", nullable = false, precision = 10, scale = 2)
    private BigDecimal bonusPointsConfigured;

    @Column(name = "baseline_threshold", nullable = false, precision = 10, scale = 2)
    private BigDecimal baselineThreshold;

    @Column(name = "vulnerable_flags", nullable = false, length = 100)
    private String vulnerableFlags;

    @Column(name = "awarded_at", nullable = false, updatable = false)
    private LocalDateTime awardedAt;

    @PrePersist
    protected void onCreate() {
        awardedAt = LocalDateTime.now();
    }
}
