package org.example.swd392_vneidcivicpoint.config;

import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.RankTier;
import org.example.swd392_vneidcivicpoint.entity.ScoringRule;
import org.example.swd392_vneidcivicpoint.repository.CitizenRepository;
import org.example.swd392_vneidcivicpoint.repository.RankTierRepository;
import org.example.swd392_vneidcivicpoint.repository.ScoringRuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DemoDataConfig {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Create an in-memory user for demo login using the mocked CCCD
        UserDetails demoUser = User.builder()
                .username("001099000123")
                .password(passwordEncoder.encode("password"))
                .roles("CITIZEN")
                .build();
                
        UserDetails adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(demoUser, adminUser);
    }

    @Bean
    public CommandLineRunner initDemoDatabase(CitizenRepository citizenRepository, 
                                              ScoringRuleRepository ruleRepository,
                                              RankTierRepository rankTierRepository) {
        return args -> {
            // Seed a citizen record if none exist
            if (citizenRepository.count() == 0) {
                Citizen demoCitizen = new Citizen();
                demoCitizen.setCccdNumber("001099000123");
                demoCitizen.setFullName("Nguyễn Văn Dân Demo");
                demoCitizen.setDateOfBirth(LocalDate.of(1990, 5, 10));
                demoCitizen.setTotalPoints(new BigDecimal("150.00"));
                demoCitizen.setFiscalYearPoints(new BigDecimal("50.00"));
                demoCitizen.setCurrentRank(org.example.swd392_vneidcivicpoint.constants.RankType.ACTIVE);
                
                citizenRepository.save(demoCitizen);
                System.out.println("Demo Citizen data initialized!");
            }

            // Seed scoring rules if none exist
            if (ruleRepository.count() == 0) {
                ScoringRule r1 = new ScoringRule();
                r1.setActivityCode("DONATE_BLOOD");
                r1.setActivityCategory(org.example.swd392_vneidcivicpoint.constants.ActivityCategory.HEALTHCARE);
                r1.setActivityName("Hiến máu nhân đạo");
                r1.setPointValue(50);
                r1.setValidFrom(LocalDate.of(2024, 1, 1).atStartOfDay());
                r1.setApprovalStatus("APPROVED");
                
                ScoringRule r2 = new ScoringRule();
                r2.setActivityCode("PAY_TAX_ON_TIME");
                r2.setActivityCategory(org.example.swd392_vneidcivicpoint.constants.ActivityCategory.FINANCIAL);
                r2.setActivityName("Hoàn thành nghĩa vụ Thuế đúng hạn");
                r2.setPointValue(20);
                r2.setValidFrom(LocalDate.of(2024, 1, 1).atStartOfDay());
                r2.setApprovalStatus("APPROVED");

                ruleRepository.saveAll(java.util.List.of(r1, r2));
                System.out.println("Demo Scoring Rules initialized!");
            }

            // Seed rank tiers if none exist
            if (rankTierRepository.count() == 0) {
                RankTier t1 = new RankTier();
                t1.setRankName("UNRANKED");
                t1.setMinPoints(BigDecimal.ZERO);
                t1.setMaxPoints(new BigDecimal("99.99"));
                t1.setValidFrom(LocalDate.of(2024, 1, 1).atStartOfDay());
                t1.setApprovalStatus("APPROVED");

                RankTier t2 = new RankTier();
                t2.setRankName("BASIC");
                t2.setMinPoints(new BigDecimal("100.00"));
                t2.setMaxPoints(new BigDecimal("349.99"));
                t2.setValidFrom(LocalDate.of(2024, 1, 1).atStartOfDay());
                t2.setApprovalStatus("APPROVED");

                RankTier t3 = new RankTier();
                t3.setRankName("ACTIVE");
                t3.setMinPoints(new BigDecimal("350.00"));
                t3.setMaxPoints(null); // No upper limit
                t3.setValidFrom(LocalDate.of(2024, 1, 1).atStartOfDay());
                t3.setApprovalStatus("APPROVED");

                rankTierRepository.saveAll(java.util.List.of(t1, t2, t3));
                System.out.println("Demo Rank Tiers initialized!");
            }
        };
    }
}
