package org.example.swd392_vneidcivicpoint.service;

import lombok.RequiredArgsConstructor;
import org.example.swd392_vneidcivicpoint.dto.CitizenProfileDto;
import org.example.swd392_vneidcivicpoint.dto.PointLedgerDto;
import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.PointLedger;
import org.example.swd392_vneidcivicpoint.exception.CitizenNotFoundException;
import org.example.swd392_vneidcivicpoint.repository.CitizenRepository;
import org.example.swd392_vneidcivicpoint.repository.PointLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitizenService {

    private final CitizenRepository citizenRepository;
    private final PointLedgerRepository pointLedgerRepository;

    public Citizen findByCccd(String cccd) {
        return citizenRepository.findByCccdNumber(cccd)
                .orElseThrow(() -> new CitizenNotFoundException("Citizen not found with CCCD: " + cccd));
    }

    public CitizenProfileDto getCitizenProfile(String cccd) {
        Citizen citizen = findByCccd(cccd);
        
        String maskedCccd = "*****" + citizen.getCccdNumber().substring(Math.max(0, citizen.getCccdNumber().length() - 4));
        
        // Calculate dynamic progress bar percentage
        // Mock logic: 100 points is next tier for unranked, 350 for basic, 1000 for active
        BigDecimal nextTierPoints = new BigDecimal("100.0");
        if (citizen.getCurrentRank().name().equals("BASIC")) nextTierPoints = new BigDecimal("350.0");
        if (citizen.getCurrentRank().name().equals("ACTIVE")) nextTierPoints = new BigDecimal("1000.0");

        int percentage = 100;
        if (citizen.getTotalPoints().compareTo(nextTierPoints) < 0 && nextTierPoints.compareTo(BigDecimal.ZERO) > 0) {
             percentage = citizen.getTotalPoints().multiply(new BigDecimal("100"))
                     .divide(nextTierPoints, 0, RoundingMode.HALF_UP).intValue();
        }

        List<PointLedger> ledgers = pointLedgerRepository.findByCitizenOrderByCreatedAtDesc(citizen);
        List<PointLedgerDto> ledgerDtos = ledgers.stream().limit(10).map(ledger -> {
            String activityDisplayName = "Hoạt động hệ thống";
            if (ledger.getScoringRule() != null) {
                activityDisplayName = ledger.getScoringRule().getActivityName();
            } else if (ledger.getTransactionType() == org.example.swd392_vneidcivicpoint.constants.TransactionType.ADJUSTMENT) {
                activityDisplayName = "Điều chỉnh số điểm";
            }
            
            return PointLedgerDto.builder()
                .ledgerId(ledger.getLedgerId())
                .activityName(activityDisplayName)
                .transactionType(ledger.getTransactionType())
                .pointsAwarded(ledger.getPointsAwarded())
                .createdAt(ledger.getCreatedAt())
                .rejectionReason(ledger.getRejectionReason())
                .build();
        }).collect(Collectors.toList());

        return CitizenProfileDto.builder()
                .fullName(citizen.getFullName())
                .maskedCccd(maskedCccd)
                .currentRank(citizen.getCurrentRank())
                .totalPoints(citizen.getTotalPoints())
                .fiscalYearPoints(citizen.getFiscalYearPoints())
                .status(citizen.getAccountStatus())
                .rankProgressBar(percentage + "%")
                .recentLedgers(ledgerDtos)
                .build();
    }
}
