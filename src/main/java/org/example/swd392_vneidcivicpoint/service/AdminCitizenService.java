package org.example.swd392_vneidcivicpoint.service;

import lombok.RequiredArgsConstructor;
import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.PointLedger;
import org.example.swd392_vneidcivicpoint.repository.CitizenRepository;
import org.example.swd392_vneidcivicpoint.repository.PointLedgerRepository;
import org.example.swd392_vneidcivicpoint.util.FiscalYearUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCitizenService {

    private final CitizenRepository citizenRepository;
    private final PointLedgerRepository ledgerRepository;
    private final PointEngineService pointEngineService;

    public List<Citizen> getAllCitizens() {
        return citizenRepository.findAll();
    }

    @Transactional
    public void adjustPoints(String cccdNumber, double points, String reason) {
        Citizen citizen = citizenRepository.findByCccdNumber(cccdNumber)
                .orElseThrow(() -> new RuntimeException("Citizen not found"));

        BigDecimal amount = BigDecimal.valueOf(points);
        int fiscalYear = FiscalYearUtil.getCurrentFiscalYear();

        // Record adjustment in ledger (UC-16 BR-08)
        PointLedger adjustment = new PointLedger();
        adjustment.setCitizen(citizen);
        // adjustment type is handled via transactionType, manually adding details to reason
        adjustment.setTransactionType(org.example.swd392_vneidcivicpoint.constants.TransactionType.ADJUSTMENT);
        adjustment.setPointsAwarded(amount);
        adjustment.setPointsOriginal(amount);
        adjustment.setFiscalYear(fiscalYear);
        adjustment.setRejectionReason(reason);
        adjustment.setCreatedAt(LocalDateTime.now());
        ledgerRepository.save(adjustment);

        // Update citizen balance
        citizen.setTotalPoints(citizen.getTotalPoints().add(amount));
        citizen.setFiscalYearPoints(citizen.getFiscalYearPoints().add(amount));
        
        // Re-evaluate rank (UC-17)
        pointEngineService.evaluateRank(citizen);
        
        citizenRepository.save(citizen);
    }
}
