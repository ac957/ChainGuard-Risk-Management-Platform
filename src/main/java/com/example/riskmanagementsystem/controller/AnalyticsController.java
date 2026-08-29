package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.model.Risk;
import com.example.riskmanagementsystem.model.OrganizationMember;
import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.repo.OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service.AIGuidanceService;
import com.example.riskmanagementsystem.service.RiskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private final RiskService riskService;
    private final UserRepository userRepo;
    private final OrganizationMemberRepository memberRepo;
    private final AIGuidanceService aiGuidanceService;
    private final ObjectMapper objectMapper;

    public AnalyticsController(
            RiskService riskService,
            UserRepository userRepo,
            OrganizationMemberRepository memberRepo,
            AIGuidanceService aiGuidanceService,
            ObjectMapper objectMapper) {
        this.riskService = riskService;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.aiGuidanceService = aiGuidanceService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String analyticsPage(Authentication auth,
                                Model model) {

        // redirect to login if not authenticated
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new
                        IllegalStateException("User not found."));

        OrganizationMember membership = memberRepo
                .findFirstByUser_UserIdAndStatus(
                        user.getUserId(), "APPROVED")
                .orElseThrow(() -> new
                        IllegalStateException(
                        "No approved organisation found."));

        Long orgId = membership.getOrganization().getOrgId();
        List<Risk> risks =
                riskService.getRisksByOrganization(orgId);

        // if no risks submitted yet show empty state
        if (risks.isEmpty()) {
            model.addAttribute("noData", true);
            return "dashboard/analytics";
        }

        model.addAttribute("noData", false);

        long lowCount = risks.stream()
                .filter(r -> r.getSeverityScore() < 8)
                .count();
        long mediumCount = risks.stream()
                .filter(r -> r.getSeverityScore() >= 8
                        && r.getSeverityScore() < 15)
                .count();
        long highCount = risks.stream()
                .filter(r -> r.getSeverityScore() >= 15)
                .count();

        model.addAttribute("lowCount", lowCount);
        model.addAttribute("mediumCount", mediumCount);
        model.addAttribute("highCount", highCount);

        Map<String, Long> statusCounts = risks.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus() == null
                                ? "UNKNOWN" : r.getStatus(),
                        Collectors.counting()));

        model.addAttribute("statusNew",
                statusCounts.getOrDefault("NEW", 0L));
        model.addAttribute("statusUnderReview",
                statusCounts.getOrDefault("UNDER REVIEW", 0L));
        model.addAttribute("statusMitigating",
                statusCounts.getOrDefault("MITIGATING", 0L));
        model.addAttribute("statusResolved",
                statusCounts.getOrDefault("RESOLVED", 0L));
        model.addAttribute("statusClosed",
                statusCounts.getOrDefault("CLOSED", 0L));

        Map<String, Long> categoryCounts = risks.stream()
                .filter(r -> r.getCategory() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getCategory().getName(),
                        Collectors.counting()));

        List<String> categoryLabels = new ArrayList<>(
                categoryCounts.keySet());
        List<Long> categoryValues = categoryLabels.stream()
                .map(categoryCounts::get)
                .collect(Collectors.toList());

        try {
            model.addAttribute("categoryLabels",
                    objectMapper.writeValueAsString(
                            categoryLabels));
            model.addAttribute("categoryValues",
                    objectMapper.writeValueAsString(
                            categoryValues));
        } catch (Exception e) {
            model.addAttribute("categoryLabels", "[]");
            model.addAttribute("categoryValues", "[]");
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, Long> monthCounts = new LinkedHashMap<>();
        risks.stream()
                .filter(r -> r.getCreatedAt() != null)
                .sorted(Comparator.comparing(Risk::getCreatedAt))
                .forEach(r -> {
                    String month = r.getCreatedAt()
                            .format(formatter);
                    monthCounts.merge(month, 1L, Long::sum);
                });

        List<String> monthLabels = new ArrayList<>(
                monthCounts.keySet());
        List<Long> monthValues = new ArrayList<>(
                monthCounts.values());

        try {
            model.addAttribute("monthLabels",
                    objectMapper.writeValueAsString(monthLabels));
            model.addAttribute("monthValues",
                    objectMapper.writeValueAsString(monthValues));
        } catch (Exception e) {
            model.addAttribute("monthLabels", "[]");
            model.addAttribute("monthValues", "[]");
        }

        List<Map<String, Object>> heatmapData = risks.stream()
                .filter(r -> r.getLikelihood() != null
                        && r.getImpact() != null)
                .map(r -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("x", r.getLikelihood());
                    point.put("y", r.getImpact());
                    point.put("title", r.getRiskTitle());
                    point.put("score", r.getSeverityScore());
                    return point;
                })
                .collect(Collectors.toList());

        try {
            model.addAttribute("heatmapData",
                    objectMapper.writeValueAsString(heatmapData));
        } catch (Exception e) {
            model.addAttribute("heatmapData", "[]");
        }

        String analysisSummary = buildSummary(risks,
                lowCount, mediumCount, highCount,
                statusCounts, categoryCounts);
        model.addAttribute("analysisSummary", analysisSummary);
        model.addAttribute("currentUserId", user.getUserId());

        return "dashboard/analytics";
    }

    @PostMapping("/ai-analysis")
    @ResponseBody
    public String generateAnalysis(
            @RequestParam String summary) {
        return aiGuidanceService.generateAnalysis(summary);
    }

    private String buildSummary(List<Risk> risks,
                                long low, long medium, long high,
                                Map<String, Long> statusCounts,
                                Map<String, Long> categoryCounts) {
        StringBuilder sb = new StringBuilder();
        sb.append("Total risks: ").append(risks.size()).append(". ");
        sb.append("Severity: Low=").append(low)
                .append(", Medium=").append(medium)
                .append(", High=").append(high).append(". ");
        sb.append("Status breakdown: ");
        statusCounts.forEach((k, v) ->
                sb.append(k).append("=").append(v).append(" "));
        sb.append(". Top categories: ");
        categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>
                        comparingByValue().reversed())
                .limit(3)
                .forEach(e -> sb.append(e.getKey())
                        .append("=").append(e.getValue())
                        .append(" "));
        return sb.toString();
    }
}