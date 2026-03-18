package org.example.swd392_vneidcivicpoint.controller;

import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.repository.CitizenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/*
 * Bỏ qua Spring Security authentication tạm thời cho API demo mapping.
 * Bạn có thể truy cập http://localhost:8080/demo/db-test
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private CitizenRepository citizenRepository;

    @GetMapping("/db-test")
    public ResponseEntity<Map<String, Object>> testDatabaseMapping() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Số lượng Citizen hiện có trong DB (Kiểm tra kết nối và mapping read)
            long count = citizenRepository.count();

            // 2. Thử tạo 1 Citizen giả để test thao tác write
            Citizen mockCitizen = new Citizen();
            // CCCD must be exactly 12 chars
            String rawId = "001099000123" + count;
            if (rawId.length() > 12) {
                rawId = rawId.substring(rawId.length() - 12);
            }
            mockCitizen.setCccdNumber(rawId);
            mockCitizen.setFullName("Nguyễn Văn Demo " + count);
            mockCitizen.setDateOfBirth(LocalDate.of(1990, 1, 1));
            
            citizenRepository.save(mockCitizen);

            response.put("status", "SUCCESS");
            response.put("message", "Database connected and mapping works perfectly!");
            response.put("total_citizens", count + 1);
            response.put("latest_saved_name", mockCitizen.getFullName());
            
        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
