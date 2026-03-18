package org.example.swd392_vneidcivicpoint.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankTierDto {
    private Long rankId;
    
    @NotBlank(message = "Rank name is required (e.g., ACTIVE, BASIC, UNRANKED)")
    private String rankName;
    
    @DecimalMin(value = "0.0", message = "Minimum points must be >= 0")
    private BigDecimal minPoints;
    
    private BigDecimal maxPoints; // Can be null for the highest tier
    
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}
