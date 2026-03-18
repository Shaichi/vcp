package org.example.swd392_vneidcivicpoint.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.swd392_vneidcivicpoint.constants.ActivityCategory;
import org.example.swd392_vneidcivicpoint.constants.RuleStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringRuleDto {
    private Long ruleId;
    
    @NotBlank(message = "Activity code is required")
    private String activityCode;
    
    private ActivityCategory activityCategory;
    
    @NotBlank(message = "Activity name is required")
    private String activityName;
    
    @Min(value = 1, message = "Point value must be greater than 0")
    private Integer pointValue;
    
    // Frequency Limit fields
    private Integer frequencyLimit;
    private String frequencyPeriod; // LIFETIME, ANNUAL, MONTHLY, WEEKLY
    
    private RuleStatus status; // ACTIVE, INACTIVE
    private String approvalStatus;
    
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}
