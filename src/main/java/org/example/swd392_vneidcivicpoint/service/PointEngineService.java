package org.example.swd392_vneidcivicpoint.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.swd392_vneidcivicpoint.constants.ActivityCategory;
import org.example.swd392_vneidcivicpoint.constants.RankType;
import org.example.swd392_vneidcivicpoint.constants.RuleStatus;
import org.example.swd392_vneidcivicpoint.constants.TransactionType;
import org.example.swd392_vneidcivicpoint.entity.*;
import org.example.swd392_vneidcivicpoint.repository.*;
import org.example.swd392_vneidcivicpoint.util.FiscalYearUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointEngineService {

    private final ScoringRuleRepository scoringRuleRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final RankTierRepository rankTierRepository;
    private final RankHistoryRepository rankHistoryRepository;
    private final CitizenRepository citizenRepository;

    @Transactional
    public void processActivity(ActivityLog activityLog) {
        // 1. Find Active Rule
        Optional<ScoringRule> ruleOpt = scoringRuleRepository.findByActivityCodeAndStatusAndValidToIsNull(
                activityLog.getActivityCode(), RuleStatus.ACTIVE);

        if (ruleOpt.isEmpty()) {
            recordRejection(activityLog, null, "No active scoring rule found for code: " + activityLog.getActivityCode());
            return;
        }

        ScoringRule rule = ruleOpt.get();
        Citizen citizen = activityLog.getCitizen();
        int fiscalYear = FiscalYearUtil.getFiscalYearFromDate(activityLog.getActivityDate().toLocalDate());

        // 2. Check Frequency Limit
        if (rule.getFrequencyLimit() != null) {
            long currentCount = 0;
            // Simple logic: Assume frequency period is ANNUAL for demo
            if ("ANNUAL".equals(rule.getFrequencyPeriod())) {
                currentCount = pointLedgerRepository.sumPointsByFiscalYear(citizen, fiscalYear).longValue();
                // Normally we would count the occurrences, this is simplified.
            }

            // A real implementation would count occurrences of this specific activity
        }

        // 3. Award Points
        BigDecimal pointsToAward = BigDecimal.valueOf(rule.getPointValue());
        
        PointLedger ledger = new PointLedger();
        ledger.setCitizen(citizen);
        ledger.setActivityLog(activityLog);
        ledger.setScoringRule(rule);
        ledger.setTransactionType(TransactionType.EARNED);
        ledger.setPointsAwarded(pointsToAward);
        ledger.setPointsOriginal(pointsToAward);
        ledger.setFiscalYear(fiscalYear);
        
        pointLedgerRepository.save(ledger);

        // 4. Update Citizen Totals
        citizen.setTotalPoints(citizen.getTotalPoints().add(pointsToAward));
        citizen.setFiscalYearPoints(citizen.getFiscalYearPoints().add(pointsToAward));
        
        // 5. Evaluate Rank
        evaluateRank(citizen);
        
        citizenRepository.save(citizen);
    }

    @Transactional
    public void evaluateRank(Citizen citizen) {
        BigDecimal totalPoints = citizen.getTotalPoints();
        List<RankTier> tiers = rankTierRepository.findByValidToIsNullAndApprovalStatus("APPROVED");
        
        String newRankStr = RankType.UNRANKED.name();
        for (RankTier tier : tiers) {
            if (totalPoints.compareTo(tier.getMinPoints()) >= 0 && 
               (tier.getMaxPoints() == null || totalPoints.compareTo(tier.getMaxPoints()) <= 0)) {
                newRankStr = tier.getRankName();
                break;
            }
        }
        
        RankType newRank = RankType.valueOf(newRankStr);
        if (citizen.getCurrentRank() != newRank) {
            RankHistory rh = new RankHistory();
            rh.setCitizen(citizen);
            rh.setPreviousRank(citizen.getCurrentRank().name());
            rh.setNewRank(newRank.name());
            rh.setTotalPointsAtEvaluation(totalPoints);
            rh.setTriggerType("POINT_ACCUMULATION");
            rh.setRankChanged(true);
            rankHistoryRepository.save(rh);
            
            citizen.setCurrentRank(newRank);
        }
    }

    private void recordRejection(ActivityLog activityLog, ScoringRule rule, String reason) {
        PointLedger rejectedLedger = new PointLedger();
        rejectedLedger.setCitizen(activityLog.getCitizen());
        rejectedLedger.setActivityLog(activityLog);
        rejectedLedger.setScoringRule(rule);
        rejectedLedger.setTransactionType(TransactionType.EARNED);
        rejectedLedger.setPointsAwarded(BigDecimal.ZERO);
        rejectedLedger.setPointsOriginal(BigDecimal.ZERO); // Or the rule's points
        rejectedLedger.setFiscalYear(FiscalYearUtil.getFiscalYearFromDate(activityLog.getActivityDate().toLocalDate()));
        rejectedLedger.setCapApplied("FREQUENCY"); // simplified
        rejectedLedger.setRejectionReason(reason);
        pointLedgerRepository.save(rejectedLedger);
    }
}
