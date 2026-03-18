package org.example.swd392_vneidcivicpoint.dto;

import lombok.Builder;
import lombok.Data;
import org.example.swd392_vneidcivicpoint.constants.AccountStatus;
import org.example.swd392_vneidcivicpoint.constants.RankType;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CitizenProfileDto {
    private String fullName;
    private String maskedCccd;
    private RankType currentRank;
    private BigDecimal totalPoints;
    private BigDecimal fiscalYearPoints;
    private AccountStatus status;
    private String rankProgressBar;
    private List<PointLedgerDto> recentLedgers;
}
