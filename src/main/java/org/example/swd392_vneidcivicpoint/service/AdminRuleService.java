package org.example.swd392_vneidcivicpoint.service;

import lombok.RequiredArgsConstructor;
import org.example.swd392_vneidcivicpoint.constants.RuleStatus;
import org.example.swd392_vneidcivicpoint.dto.ScoringRuleDto;
import org.example.swd392_vneidcivicpoint.entity.ScoringRule;
import org.example.swd392_vneidcivicpoint.repository.ScoringRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRuleService {

    private final ScoringRuleRepository ruleRepository;

    public List<ScoringRuleDto> getAllActiveRules() {
        return ruleRepository.findAll().stream()
                .filter(rule -> RuleStatus.ACTIVE.equals(rule.getStatus()) && rule.getValidTo() == null)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public List<ScoringRuleDto> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void submitRuleConfiguration(ScoringRuleDto dto) {
        // As per UC-05, this creates a Pending record for Maker-Checker
        ScoringRule newRule = new ScoringRule();
        newRule.setActivityCode(dto.getActivityCode());
        newRule.setActivityCategory(dto.getActivityCategory());
        newRule.setActivityName(dto.getActivityName());
        newRule.setPointValue(dto.getPointValue());
        newRule.setFrequencyLimit(dto.getFrequencyLimit());
        newRule.setFrequencyPeriod(dto.getFrequencyPeriod());
        
        // Ensure new rules wait for approval
        newRule.setApprovalStatus("PENDING");
        newRule.setStatus(RuleStatus.ACTIVE); 
        newRule.setValidFrom(LocalDateTime.now());
        
        ruleRepository.save(newRule);
    }
    
    @Transactional
    public void approveRule(Long ruleId) {
        Optional<ScoringRule> ruleOpt = ruleRepository.findById(ruleId);
        if (ruleOpt.isPresent()) {
            ScoringRule rule = ruleOpt.get();
            // If this is an update to an existing activity code, expire the old active one
            deactivatePreviousVersion(rule.getActivityCode(), rule.getRuleId());
            
            rule.setApprovalStatus("APPROVED");
            rule.setValidFrom(LocalDateTime.now());
            ruleRepository.save(rule);
        }
    }
    
    @Transactional
    public void rejectRule(Long ruleId) {
        ruleRepository.findById(ruleId).ifPresent(rule -> {
            rule.setApprovalStatus("REJECTED");
            rule.setStatus(RuleStatus.INACTIVE); // Ensure it's off
            ruleRepository.save(rule);
        });
    }

    @Transactional
    public void softDeleteRule(Long ruleId) {
        ruleRepository.findById(ruleId).ifPresent(rule -> {
            rule.setStatus(RuleStatus.INACTIVE);
            rule.setValidTo(LocalDateTime.now());
            ruleRepository.save(rule);
        });
    }
    
    private void deactivatePreviousVersion(String activityCode, Long currentRuleId) {
        ruleRepository.findAll().stream()
            .filter(r -> r.getActivityCode().equals(activityCode))
            .filter(r -> "APPROVED".equals(r.getApprovalStatus()))
            .filter(r -> RuleStatus.ACTIVE.equals(r.getStatus()))
            .filter(r -> r.getValidTo() == null)
            .filter(r -> !r.getRuleId().equals(currentRuleId))
            .forEach(r -> {
                r.setValidTo(LocalDateTime.now());
                ruleRepository.save(r);
            });
    }

    private ScoringRuleDto mapToDto(ScoringRule rule) {
        return ScoringRuleDto.builder()
                .ruleId(rule.getRuleId())
                .activityCode(rule.getActivityCode())
                .activityCategory(rule.getActivityCategory())
                .activityName(rule.getActivityName())
                .pointValue(rule.getPointValue())
                .frequencyLimit(rule.getFrequencyLimit())
                .frequencyPeriod(rule.getFrequencyPeriod())
                .status(rule.getStatus())
                .approvalStatus(rule.getApprovalStatus())
                .validFrom(rule.getValidFrom())
                .validTo(rule.getValidTo())
                .build();
    }
}
