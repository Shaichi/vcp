package org.example.swd392_vneidcivicpoint.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.swd392_vneidcivicpoint.dto.ScoringRuleDto;
import org.example.swd392_vneidcivicpoint.repository.CitizenRepository;
import org.example.swd392_vneidcivicpoint.repository.RankTierRepository;
import org.example.swd392_vneidcivicpoint.service.AdminRuleService;
import org.example.swd392_vneidcivicpoint.service.AdminCitizenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminRuleService ruleService;
    private final AdminCitizenService citizenService;
    private final CitizenRepository citizenRepository;
    private final RankTierRepository rankTierRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalCitizens", citizenRepository.count());
        model.addAttribute("activeRulesCount", ruleService.getAllActiveRules().size());
        model.addAttribute("rankTiersCount", rankTierRepository.count());
        return "admin/dashboard";
    }

    @GetMapping("/rules")
    public String listRules(Model model) {
        model.addAttribute("activePage", "rules");
        model.addAttribute("rules", ruleService.getAllRules());
        // For the creation modal form
        model.addAttribute("newRule", new ScoringRuleDto());
        return "admin/rules/list";
    }

    @PostMapping("/rules/create")
    public String createRule(@Valid @ModelAttribute("newRule") ScoringRuleDto ruleDto, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu quy tắc không hợp lệ.");
            return "redirect:/admin/rules";
        }
        
        try {
            ruleService.submitRuleConfiguration(ruleDto);
            redirectAttributes.addFlashAttribute("success", "Quy tắc đã được gửi để chờ phê duyệt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/rules";
    }

    @PostMapping("/rules/{id}/approve")
    public String approveRule(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        ruleService.approveRule(id);
        redirectAttributes.addFlashAttribute("success", "Quy tắc đã được phê duyệt và đang hoạt động.");
        return "redirect:/admin/rules";
    }

    @PostMapping("/rules/{id}/reject")
    public String rejectRule(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        ruleService.rejectRule(id);
        redirectAttributes.addFlashAttribute("success", "Quy tắc đã bị từ chối.");
        return "redirect:/admin/rules";
    }

    @PostMapping("/rules/{id}/delete")
    public String softDeleteRule(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        ruleService.softDeleteRule(id);
        redirectAttributes.addFlashAttribute("success", "Quy tắc đã bị ngừng hoạt động (xóa mềm).");
        return "redirect:/admin/rules";
    }

    // Citizen Management (UC-07)
    @GetMapping("/citizens")
    public String listCitizens(Model model) {
        model.addAttribute("activePage", "citizens");
        model.addAttribute("citizens", citizenService.getAllCitizens());
        return "admin/citizens/list";
    }

    @PostMapping("/citizens/adjust-points")
    public String adjustPoints(@RequestParam("cccdNumber") String cccdNumber,
                               @RequestParam("points") double points,
                               @RequestParam("reason") String reason,
                               RedirectAttributes redirectAttributes) {
        try {
            citizenService.adjustPoints(cccdNumber, points, reason);
            redirectAttributes.addFlashAttribute("success", "Điều chỉnh điểm thành công cho công dân " + cccdNumber);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi điều chỉnh điểm: " + e.getMessage());
        }
        return "redirect:/admin/citizens";
    }
}
