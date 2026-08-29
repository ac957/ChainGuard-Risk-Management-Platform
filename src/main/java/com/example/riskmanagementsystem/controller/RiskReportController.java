package com.example.riskmanagementsystem.controller;

import com.example.riskmanagementsystem.model.*;
import com.example.riskmanagementsystem.repo
        .OrganizationMemberRepository;
import com.example.riskmanagementsystem.repo.UserRepository;
import com.example.riskmanagementsystem.service
        .MitigationAssignmentService;
import com.example.riskmanagementsystem.service.RiskService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/risks/export")
public class RiskReportController {

    private final RiskService riskService;
    private final UserRepository userRepo;
    private final OrganizationMemberRepository memberRepo;
    private final MitigationAssignmentService assignmentService;

    public RiskReportController(
            RiskService riskService,
            UserRepository userRepo,
            OrganizationMemberRepository memberRepo,
            MitigationAssignmentService assignmentService) {
        this.riskService = riskService;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public void exportPdf(
            Authentication auth,
            HttpServletResponse response,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category)
            throws IOException, DocumentException {

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
        String orgName = membership.getOrganization().getOrgName();
        List<Risk> risks = riskService.getRisksByOrganization(orgId);

        // apply filters
        if (severity != null && !severity.isEmpty()) {
            risks = risks.stream().filter(r -> {
                int score = r.getSeverityScore() != null
                        ? r.getSeverityScore() : 0;
                return switch (severity.toUpperCase()) {
                    case "HIGH" -> score >= 15;
                    case "MEDIUM" -> score >= 8 && score < 15;
                    case "LOW" -> score < 8;
                    default -> true;
                };
            }).collect(java.util.stream.Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            risks = risks.stream()
                    .filter(r -> status.equalsIgnoreCase(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (category != null && !category.isEmpty()) {
            risks = risks.stream()
                    .filter(r -> r.getCategory() != null
                            && category.equalsIgnoreCase(
                            r.getCategory().getName()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // build filter label for report
        StringBuilder filterLabel = new StringBuilder();
        if (severity != null && !severity.isEmpty())
            filterLabel.append("Severity: ").append(severity).append("   ");
        if (status != null && !status.isEmpty())
            filterLabel.append("Status: ").append(status).append("   ");
        if (category != null && !category.isEmpty())
            filterLabel.append("Category: ").append(category);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"ChainGuard_Risk_Report_"
                        + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        + ".pdf\"");

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // colours matching site
        BaseColor teal        = new BaseColor(15, 118, 110);
        BaseColor tealLight   = new BaseColor(240, 253, 250);
        BaseColor tealBorder  = new BaseColor(94, 234, 212);
        BaseColor tealMid     = new BaseColor(13, 148, 136);
        BaseColor white       = BaseColor.WHITE;
        BaseColor darkText    = new BaseColor(17, 17, 17);
        BaseColor mutedText   = new BaseColor(85, 85, 85);
        BaseColor borderGray  = new BaseColor(220, 220, 220);

        // fonts
        Font titleFont = new Font(
                Font.FontFamily.HELVETICA, 18,
                Font.BOLD, white);
        Font subFont = new Font(
                Font.FontFamily.HELVETICA, 10,
                Font.NORMAL, new BaseColor(204, 251, 241));
        Font headingFont = new Font(
                Font.FontFamily.HELVETICA, 12,
                Font.BOLD, new BaseColor(19, 78, 74));
        Font normalFont = new Font(
                Font.FontFamily.HELVETICA, 9,
                Font.NORMAL, darkText);
        Font mutedFont = new Font(
                Font.FontFamily.HELVETICA, 8,
                Font.NORMAL, mutedText);
        Font tableHeaderFont = new Font(
                Font.FontFamily.HELVETICA, 9,
                Font.BOLD, white);
        Font badgeHighFont = new Font(
                Font.FontFamily.HELVETICA, 9,
                Font.BOLD, new BaseColor(153, 27, 27));
        Font badgeMedFont = new Font(
                Font.FontFamily.HELVETICA, 9,
                Font.BOLD, new BaseColor(146, 64, 14));
        Font badgeLowFont = new Font(
                Font.FontFamily.HELVETICA, 9,
                Font.BOLD, new BaseColor(15, 118, 110));
        Font footerFont = new Font(
                Font.FontFamily.HELVETICA, 8,
                Font.ITALIC, mutedText);
        Font filterFont = new Font(
                Font.FontFamily.HELVETICA, 9,
                Font.ITALIC, new BaseColor(13, 148, 136));

        // header banner
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(teal);
        headerCell.setPadding(16);
        headerCell.setBorder(Rectangle.NO_BORDER);
        Paragraph headerContent = new Paragraph();
        headerContent.add(new Chunk(
                "ChainGuard — Risk Register Report\n", titleFont));
        headerContent.add(new Chunk(
                orgName + "   |   Generated: "
                        + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "dd MMM yyyy HH:mm")),
                subFont));
        headerCell.addElement(headerContent);
        headerTable.addCell(headerCell);
        document.add(headerTable);

        // filter label
        if (filterLabel.length() > 0) {
            document.add(Chunk.NEWLINE);
            Paragraph filterPara = new Paragraph(
                    "Filters applied: " + filterLabel,
                    filterFont);
            filterPara.setSpacingAfter(4);
            document.add(filterPara);
        }

        document.add(Chunk.NEWLINE);

        // summary statistics
        List<Risk> finalRisks = risks;
        long totalRisks = finalRisks.stream()
                .filter(r -> !"CLOSED".equalsIgnoreCase(
                        r.getStatus())).count();
        long highCount = finalRisks.stream()
                .filter(r -> r.getSeverityScore() != null
                        && r.getSeverityScore() >= 15
                        && !"CLOSED".equalsIgnoreCase(
                        r.getStatus())).count();
        long resolvedCount = finalRisks.stream()
                .filter(r -> "RESOLVED".equalsIgnoreCase(
                        r.getStatus())
                        || "CLOSED".equalsIgnoreCase(
                        r.getStatus())).count();
        long closedCount = finalRisks.stream()
                .filter(r -> "CLOSED".equalsIgnoreCase(
                        r.getStatus())).count();

        PdfPTable statsTable = new PdfPTable(4);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingAfter(16);
        addStatCell(statsTable, "Total Active Risks",
                String.valueOf(totalRisks), tealLight,
                tealBorder, teal);
        addStatCell(statsTable, "High Severity",
                String.valueOf(highCount),
                new BaseColor(254, 242, 242),
                new BaseColor(252, 165, 165),
                new BaseColor(153, 27, 27));
        addStatCell(statsTable, "Resolved / Closed",
                String.valueOf(resolvedCount),
                tealLight, tealBorder, tealMid);
        addStatCell(statsTable, "Formally Closed",
                String.valueOf(closedCount),
                new BaseColor(245, 245, 245),
                borderGray, mutedText);
        document.add(statsTable);

        // section heading
        Paragraph sectionHeading = new Paragraph(
                "Full Risk Register", headingFont);
        sectionHeading.setSpacingAfter(8);
        document.add(sectionHeading);

        if (risks.isEmpty()) {
            document.add(new Paragraph(
                    "No risks found matching the selected filters.",
                    mutedFont));
        } else {
            // risk table
            PdfPTable riskTable = new PdfPTable(8);
            riskTable.setWidthPercentage(100);
            riskTable.setWidths(new float[]{
                    3f, 2f, 1.2f, 1.2f, 1.2f,
                    1.5f, 1.5f, 2f});

            String[] headers = {
                    "Risk Title", "Category",
                    "Likelihood", "Impact",
                    "Severity", "Status",
                    "Risk Owner", "Mitigation Status"};

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(
                        new Phrase(header, tableHeaderFont));
                cell.setBackgroundColor(teal);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorderColor(tealBorder);
                riskTable.addCell(cell);
            }

            boolean alternate = false;
            for (Risk risk : risks) {
                BaseColor rowBg = alternate
                        ? tealLight : white;
                alternate = !alternate;

                int score = risk.getSeverityScore() != null
                        ? risk.getSeverityScore() : 0;

                Font severityFont;
                if (score >= 15) severityFont = badgeHighFont;
                else if (score >= 8) severityFont = badgeMedFont;
                else severityFont = badgeLowFont;

                String riskStatus = risk.getStatus() != null
                        ? risk.getStatus() : "-";
                Font statusFont;
                if ("RESOLVED".equalsIgnoreCase(riskStatus)
                        || "CLOSED".equalsIgnoreCase(riskStatus)) {
                    statusFont = badgeLowFont;
                } else if ("MITIGATING".equalsIgnoreCase(riskStatus)
                        || "UNDER REVIEW".equalsIgnoreCase(riskStatus)) {
                    statusFont = badgeMedFont;
                } else {
                    statusFont = badgeHighFont;
                }

                MitigationAssignment assignment =
                        assignmentService.getAssignmentForRisk(
                                risk.getRiskId());
                String mitigationStatus = assignment != null
                        ? assignment.getStatus() : "Unassigned";

                String ownerName = risk.getRiskOwner() != null
                        ? risk.getRiskOwner().getEmail() : "-";

                addRiskCell(riskTable, risk.getRiskTitle(),
                        normalFont, rowBg, tealBorder);
                addRiskCell(riskTable,
                        risk.getCategory() != null
                                ? risk.getCategory().getName() : "-",
                        normalFont, rowBg, tealBorder);
                addRiskCell(riskTable,
                        String.valueOf(risk.getLikelihood()),
                        normalFont, rowBg, tealBorder);
                addRiskCell(riskTable,
                        String.valueOf(risk.getImpact()),
                        normalFont, rowBg, tealBorder);
                addRiskCell(riskTable,
                        String.valueOf(score),
                        severityFont, rowBg, tealBorder);
                addRiskCell(riskTable,
                        riskStatus, statusFont, rowBg, tealBorder);
                addRiskCell(riskTable,
                        ownerName, normalFont, rowBg, tealBorder);
                addRiskCell(riskTable,
                        mitigationStatus, normalFont,
                        rowBg, tealBorder);
            }
            document.add(riskTable);
        }

        document.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
                "This report was generated by ChainGuard "
                        + "Risk Management System. "
                        + "It includes all risks across the full "
                        + "risk lifecycle including formally closed "
                        + "risks, in accordance with ISO 31000 "
                        + "audit trail requirements.",
                footerFont);
        document.add(footer);
        document.close();
    }

    private void addStatCell(PdfPTable table,
                             String label,
                             String value,
                             BaseColor bg,
                             BaseColor border,
                             BaseColor valueColor) {
        Font labelFont = new Font(
                Font.FontFamily.HELVETICA, 8,
                Font.NORMAL, new BaseColor(85, 85, 85));
        Font valueFont = new Font(
                Font.FontFamily.HELVETICA, 16,
                Font.BOLD, valueColor);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setPadding(10);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(border);
        cell.addElement(new Paragraph(value, valueFont));
        cell.addElement(new Paragraph(label, labelFont));
        table.addCell(cell);
    }

    private void addRiskCell(PdfPTable table,
                             String text,
                             Font font,
                             BaseColor bg,
                             BaseColor border) {
        PdfPCell cell = new PdfPCell(
                new Phrase(text != null ? text : "-", font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setBorderColor(border);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}
