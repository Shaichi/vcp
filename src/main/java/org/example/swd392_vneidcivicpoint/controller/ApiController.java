package org.example.swd392_vneidcivicpoint.controller;

import org.example.swd392_vneidcivicpoint.dto.AuthRequest;
import org.example.swd392_vneidcivicpoint.dto.AuthResponse;
import org.example.swd392_vneidcivicpoint.constants.IngestionStatus;
import org.example.swd392_vneidcivicpoint.constants.RuleStatus;
import org.example.swd392_vneidcivicpoint.constants.TransactionType;
import org.example.swd392_vneidcivicpoint.entity.ActivityLog;
import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.PointLedger;
import org.example.swd392_vneidcivicpoint.entity.ScoringRule;
import org.example.swd392_vneidcivicpoint.repository.ActivityLogRepository;
import org.example.swd392_vneidcivicpoint.repository.CitizenRepository;
import org.example.swd392_vneidcivicpoint.repository.PointLedgerRepository;
import org.example.swd392_vneidcivicpoint.repository.ScoringRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*") // Cho phép Flutter kết nối
public class ApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private PointLedgerRepository pointLedgerRepository;

    @Autowired
    private ScoringRuleRepository scoringRuleRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(AuthResponse.builder()
                    .username(authentication.getName())
                    .roles(roles)
                    .status("SUCCESS")
                    .message("Login successful")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(AuthResponse.builder()
                    .status("FAILED")
                    .message("Invalid username or password")
                    .build());
        }
    }

    @GetMapping("/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData(@RequestParam(required = false) String cccd) {
        String username;
        if (cccd != null && !cccd.isEmpty()) {
            username = cccd;
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                 return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
            }
            username = auth.getName();
        }
        
        System.out.println("[DEBUG_LOG] Fetching dashboard for user: " + username);

        Optional<Citizen> citizenOpt = citizenRepository.findByCccdNumber(username);
        if (citizenOpt.isEmpty()) {
            System.out.println("[DEBUG_LOG] Citizen not found for: " + username);
            return ResponseEntity.status(404).body(Map.of("message", "Citizen not found"));
        }

        Citizen citizen = citizenOpt.get();
        List<PointLedger> ledgers = pointLedgerRepository.findByCitizenOrderByCreatedAtDesc(citizen);

        Map<String, Object> profile = new HashMap<>();
        profile.put("fullName", citizen.getFullName());
        String masked = citizen.getCccdNumber().length() >= 6 
                ? citizen.getCccdNumber().substring(0, 6) + "******" 
                : "******";
        profile.put("maskedCccd", masked);
        profile.put("status", citizen.getAccountStatus() != null ? citizen.getAccountStatus().name() : "ACTIVE");
        profile.put("currentRank", citizen.getCurrentRank() != null ? citizen.getCurrentRank().name() : "UNRANKED");
        profile.put("rankDisplayName", citizen.getCurrentRank() != null ? citizen.getCurrentRank().getDisplayName() : "Chưa xếp hạng");
        profile.put("totalPoints", citizen.getTotalPoints());
        profile.put("fiscalYearPoints", citizen.getFiscalYearPoints());
        
        double progress = 0;
        if (citizen.getTotalPoints() != null) {
            progress = citizen.getTotalPoints().doubleValue() / 1000.0 * 100.0;
        }
        if (progress > 100) progress = 100;
        profile.put("rankProgressBar", String.format("%.0f%%", progress));

        List<Map<String, Object>> recentLedgers = ledgers.stream().limit(10).map(l -> {
            Map<String, Object> m = new HashMap<>();
            m.put("createdAt", l.getCreatedAt() != null ? l.getCreatedAt().format(formatter) : "N/A");
            m.put("activityName", (l.getScoringRule() != null) ? l.getScoringRule().getActivityName() : "Hoạt động");
            m.put("transactionType", l.getTransactionType() != null ? l.getTransactionType().name() : "EARNED");
            m.put("typeDisplayName", l.getTransactionType() != null ? l.getTransactionType().getDisplayName() : "Tích lũy");
            m.put("pointsAwarded", l.getPointsAwarded());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("profile", profile);
        response.put("recentLedgers", recentLedgers);
        
        System.out.println("[DEBUG_LOG] Data prepared successfully for " + username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test/simulate-blood-donation")
    @Transactional
    public ResponseEntity<Map<String, Object>> simulateBloodDonation(@RequestParam String cccd) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("[DEBUG_LOG] Simulating blood donation for: " + cccd);
            
            // 1. Tìm công dân
            Optional<Citizen> citizenOpt = citizenRepository.findByCccdNumber(cccd);
            if (citizenOpt.isEmpty()) {
                System.out.println("[DEBUG_LOG] Citizen not found: " + cccd);
                return ResponseEntity.status(404).body(Map.of("status", "FAILED", "message", "Citizen not found: " + cccd));
            }
            Citizen citizen = citizenOpt.get();

            // 2. Tìm quy tắc hiến máu
            Optional<ScoringRule> ruleOpt = scoringRuleRepository.findByActivityCodeAndStatusAndValidToIsNull("DONATE_BLOOD", RuleStatus.ACTIVE);
            if (ruleOpt.isEmpty()) {
                System.out.println("[DEBUG_LOG] ScoringRule DONATE_BLOOD not found!");
                return ResponseEntity.status(500).body(Map.of("status", "FAILED", "message", "DONATE_BLOOD rule not found or not active in DB"));
            }
            ScoringRule rule = ruleOpt.get();

            // 3. Tạo ActivityLog
            ActivityLog log = new ActivityLog();
            log.setCitizen(citizen);
            log.setActivityCode(rule.getActivityCode());
            log.setSourceSystem("HOSPITAL_SYS_TEST");
            log.setExternalReference("REF-" + System.currentTimeMillis());
            log.setPartnerId("PARTNER-001");
            log.setActivityDate(LocalDateTime.now());
            log.setFiscalYear(LocalDateTime.now().getYear());
            log.setIngestionStatus(IngestionStatus.VALID);
            log.setForwardedToEngine(true);
            activityLogRepository.save(log);

            // 4. Tạo PointLedger
            PointLedger ledger = new PointLedger();
            ledger.setCitizen(citizen);
            ledger.setActivityLog(log);
            ledger.setScoringRule(rule);
            ledger.setTransactionType(TransactionType.EARNED);
            ledger.setPointsAwarded(new BigDecimal(rule.getPointValue()));
            ledger.setPointsOriginal(new BigDecimal(rule.getPointValue()));
            ledger.setFiscalYear(log.getFiscalYear());
            pointLedgerRepository.save(ledger);

            // 5. Cập nhật điểm cho Citizen
            if (citizen.getTotalPoints() == null) citizen.setTotalPoints(BigDecimal.ZERO);
            if (citizen.getFiscalYearPoints() == null) citizen.setFiscalYearPoints(BigDecimal.ZERO);
            
            citizen.setTotalPoints(citizen.getTotalPoints().add(ledger.getPointsAwarded()));
            citizen.setFiscalYearPoints(citizen.getFiscalYearPoints().add(ledger.getPointsAwarded()));
            citizenRepository.save(citizen);

            System.out.println("[DEBUG_LOG] Donation simulated! Added: " + rule.getPointValue());
            
            response.put("status", "SUCCESS");
            response.put("message", "Simulated blood donation successfully!");
            response.put("addedPoints", rule.getPointValue());
            response.put("newTotalPoints", citizen.getTotalPoints());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[DEBUG_LOG] ERROR in simulateBloodDonation: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("status", "FAILED", "message", "Error: " + e.getMessage()));
        }
    }
}
