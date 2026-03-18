package org.example.swd392_vneidcivicpoint.controller;

import lombok.RequiredArgsConstructor;
import org.example.swd392_vneidcivicpoint.dto.CitizenProfileDto;
import org.example.swd392_vneidcivicpoint.service.CitizenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/citizen")
@RequiredArgsConstructor
public class CitizenController {

    private final CitizenService citizenService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // In a real app, retrieve CCCD from Authentication principal
        // For this demo, we can mock it or retrieve it from standard User details
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String cccd = auth.getName(); // Assumes username is the CCCD

        try {
            CitizenProfileDto profile = citizenService.getCitizenProfile(cccd);
            model.addAttribute("profile", profile);
            return "citizen/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải hồ sơ: " + e.getMessage());
            return "error";
        }
    }
}
