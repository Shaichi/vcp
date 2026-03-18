package org.example.swd392_vneidcivicpoint.dto;

import lombok.Builder;
import lombok.Data;
import org.example.swd392_vneidcivicpoint.constants.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PointLedgerDto {
    private Long ledgerId;
    private String activityName;
    private TransactionType transactionType;
    private BigDecimal pointsAwarded;
    private LocalDateTime createdAt;
    private String rejectionReason;
}
