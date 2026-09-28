package com.repovision.report;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.repovision.analysis.api.dto.DeveloperMetrics;
import com.repovision.analysis.api.dto.FileMetrics;
import com.repovision.analysis.application.DeveloperAnalyticsService;
import com.repovision.analysis.application.FileAnalyticsService;
import com.repovision.commit.application.CommitStore;
import com.repovision.commit.domain.Commit;
import com.repovision.intelligence.health.HealthDimension;
import com.repovision.intelligence.health.RepositoryHealthResponse;
import com.repovision.intelligence.health.RepositoryHealthService;
import com.repovision.intelligence.insight.InsightEngineService;
import com.repovision.intelligence.insight.InsightItem;
import com.repovision.intelligence.risk.RiskEngineService;
import com.repovision.intelligence.risk.RiskItem;
import com.repovision.intelligence.risk.RiskReportResponse;
import com.repovision.repository.application.RepositoryService;
import com.repovision.repository.api.dto.RepositoryResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private final RepositoryService repositoryService;
    private final CommitStore commitStore;
    private final FileAnalyticsService fileAnalyticsService;
    private final DeveloperAnalyticsService developerAnalyticsService;
    private final RiskEngineService riskEngineService;
    private final RepositoryHealthService repositoryHealthService;
    private final InsightEngineService insightEngineService;

    public ReportService(
            RepositoryService repositoryService,
            CommitStore commitStore,
            FileAnalyticsService fileAnalyticsService,
            DeveloperAnalyticsService developerAnalyticsService,
            RiskEngineService riskEngineService,
            RepositoryHealthService repositoryHealthService,
            InsightEngineService insightEngineService
    ) {
        this.repositoryService = repositoryService;
        this.commitStore = commitStore;
        this.fileAnalyticsService = fileAnalyticsService;
        this.developerAnalyticsService = developerAnalyticsService;
        this.riskEngineService = riskEngineService;
        this.repositoryHealthService = repositoryHealthService;
        this.insightEngineService = insightEngineService;
    }

    public byte[] exportCommitsCsv(UUID repositoryId) {
        List<Commit> commits = commitStore.findByRepositoryId(repositoryId);
        StringBuilder sb = new StringBuilder();
        sb.append("Hash,Author Name,Author Email,Authored At,Additions,Deletions,Churn,Subject\n");

        for (Commit c : commits) {
            sb.append(escapeCsv(c.getHash())).append(",")
                    .append(escapeCsv(c.getAuthorName())).append(",")
                    .append(escapeCsv(c.getAuthorEmail())).append(",")
                    .append(escapeCsv(c.getAuthoredAt() != null ? c.getAuthoredAt().toString() : "")).append(",")
                    .append(c.getAdditions()).append(",")
                    .append(c.getDeletions()).append(",")
                    .append(c.getAdditions() + c.getDeletions()).append(",")
                    .append(escapeCsv(c.getSubject())).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportFilesCsv(UUID repositoryId) {
        List<FileMetrics> files = fileAnalyticsService.computeFileMetrics(repositoryId);
        StringBuilder sb = new StringBuilder();
        sb.append("File Path,Modifications,Additions,Deletions,Churn,Contributors,First Modified,Last Modified,Avg Change Size,Hotspot Score\n");

        for (FileMetrics f : files) {
            sb.append(escapeCsv(f.filePath())).append(",")
                    .append(f.modificationCount()).append(",")
                    .append(f.additions()).append(",")
                    .append(f.deletions()).append(",")
                    .append(f.churn()).append(",")
                    .append(f.contributorCount()).append(",")
                    .append(escapeCsv(f.firstModified() != null ? f.firstModified().toString() : "")).append(",")
                    .append(escapeCsv(f.lastModified() != null ? f.lastModified().toString() : "")).append(",")
                    .append(f.averageChangeSize()).append(",")
                    .append(f.hotspotScore()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportDevelopersCsv(UUID repositoryId) {
        var analytics = developerAnalyticsService.getDeveloperAnalytics(repositoryId);
        StringBuilder sb = new StringBuilder();
        sb.append("Developer Name,Email,Commits,Additions,Deletions,Churn,Commit %,Churn %\n");

        for (DeveloperMetrics d : analytics.developers()) {
            sb.append(escapeCsv(d.authorName())).append(",")
                    .append(escapeCsv(d.authorEmail())).append(",")
                    .append(d.commitCount()).append(",")
                    .append(d.additions()).append(",")
                    .append(d.deletions()).append(",")
                    .append(d.churn()).append(",")
                    .append(Math.round(d.commitPercentage() * 10.0) / 10.0).append(",")
                    .append(Math.round(d.churnPercentage() * 10.0) / 10.0).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportRisksCsv(UUID repositoryId) {
        RiskReportResponse report = riskEngineService.analyzeRisks(repositoryId);
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Type,Severity,Title,Entity,Evidence,Description,Recommendation\n");

        for (RiskItem r : report.risks()) {
            sb.append(escapeCsv(r.id())).append(",")
                    .append(escapeCsv(r.type().name())).append(",")
                    .append(escapeCsv(r.severity().name())).append(",")
                    .append(escapeCsv(r.title())).append(",")
                    .append(escapeCsv(r.entity())).append(",")
                    .append(escapeCsv(r.evidence())).append(",")
                    .append(escapeCsv(r.description())).append(",")
                    .append(escapeCsv(r.recommendation())).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportHealthPdf(UUID repositoryId) {
        RepositoryResponse repo = repositoryService.getRepository(repositoryId);
        RepositoryHealthResponse health = repositoryHealthService.calculateHealth(repositoryId);
        RiskReportResponse riskReport = riskEngineService.analyzeRisks(repositoryId);
        List<InsightItem> insights = insightEngineService.generateInsights(repositoryId).insights();
        List<FileMetrics> topHotspots = fileAnalyticsService.getHotspots(repositoryId, 5);
        List<Commit> commits = commitStore.findByRepositoryId(repositoryId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(24, 30, 42));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(30, 41, 59));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(51, 65, 85));
            Font bodyBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(30, 41, 59));
            Font scoreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, new Color(14, 165, 233));

            // Header Banner
            Paragraph title = new Paragraph("RepoVision — Repository Health Report", titleFont);
            title.setSpacingAfter(4);
            document.add(title);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
            Paragraph subtitle = new Paragraph("Generated on " + formatter.format(Instant.now()) + " | Platform Architecture & Analytics", subtitleFont);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Repository Info Card Table
            PdfPTable repoTable = new PdfPTable(4);
            repoTable.setWidthPercentage(100);
            repoTable.setSpacingAfter(15);
            addInfoCell(repoTable, "Repository Name", repo.name(), bodyBoldFont, bodyFont);
            addInfoCell(repoTable, "Default Branch", repo.defaultBranch(), bodyBoldFont, bodyFont);
            addInfoCell(repoTable, "Total Commits", String.valueOf(commits.size()), bodyBoldFont, bodyFont);
            addInfoCell(repoTable, "Total Churn", String.valueOf(commits.stream().mapToLong(c -> c.getAdditions() + c.getDeletions()).sum()) + " lines", bodyBoldFont, bodyFont);
            document.add(repoTable);

            // Health Score Section
            PdfPTable healthBox = new PdfPTable(2);
            healthBox.setWidthPercentage(100);
            healthBox.setWidths(new float[]{1.2f, 2.8f});
            healthBox.setSpacingAfter(15);

            PdfPCell scoreCell = new PdfPCell();
            scoreCell.setBackgroundColor(new Color(248, 250, 252));
            scoreCell.setBorderColor(new Color(226, 232, 240));
            scoreCell.setPadding(12);
            scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph pScore = new Paragraph(String.valueOf(health.overallScore()), scoreFont);
            pScore.setAlignment(Element.ALIGN_CENTER);
            Paragraph pLabel = new Paragraph("/ 100\n" + health.status(), bodyBoldFont);
            pLabel.setAlignment(Element.ALIGN_CENTER);
            scoreCell.addElement(pScore);
            scoreCell.addElement(pLabel);
            healthBox.addCell(scoreCell);

            PdfPCell summaryCell = new PdfPCell();
            summaryCell.setBackgroundColor(new Color(248, 250, 252));
            summaryCell.setBorderColor(new Color(226, 232, 240));
            summaryCell.setPadding(10);
            Paragraph pSummaryTitle = new Paragraph("Executive Summary", bodyBoldFont);
            Paragraph pSummary = new Paragraph(health.summary(), bodyFont);
            summaryCell.addElement(pSummaryTitle);
            summaryCell.addElement(pSummary);
            healthBox.addCell(summaryCell);
            document.add(healthBox);

            // Dimension Breakdown Table
            Paragraph dimTitle = new Paragraph("Health Dimensions Breakdown", sectionTitleFont);
            dimTitle.setSpacingAfter(6);
            document.add(dimTitle);

            PdfPTable dimTable = new PdfPTable(4);
            dimTable.setWidthPercentage(100);
            dimTable.setWidths(new float[]{2.5f, 1f, 1f, 1.2f});
            dimTable.setSpacingAfter(15);

            addTableHeader(dimTable, "Dimension", bodyBoldFont);
            addTableHeader(dimTable, "Score", bodyBoldFont);
            addTableHeader(dimTable, "Weight", bodyBoldFont);
            addTableHeader(dimTable, "Status", bodyBoldFont);

            for (HealthDimension dim : health.dimensions()) {
                addTableCell(dimTable, dim.name(), bodyFont);
                addTableCell(dimTable, String.format("%.1f", dim.score()), bodyBoldFont);
                addTableCell(dimTable, String.format("%.0f%%", dim.weight() * 100), bodyFont);
                addTableCell(dimTable, dim.status(), bodyBoldFont);
            }
            document.add(dimTable);

            // Top Hotspots Table
            Paragraph hotspotTitle = new Paragraph("Top Maintenance Hotspots", sectionTitleFont);
            hotspotTitle.setSpacingAfter(6);
            document.add(hotspotTitle);

            PdfPTable hotspotTable = new PdfPTable(5);
            hotspotTable.setWidthPercentage(100);
            hotspotTable.setWidths(new float[]{3.5f, 1f, 1.2f, 1f, 1.2f});
            hotspotTable.setSpacingAfter(15);

            addTableHeader(hotspotTable, "File Path", bodyBoldFont);
            addTableHeader(hotspotTable, "Modifications", bodyBoldFont);
            addTableHeader(hotspotTable, "Total Churn", bodyBoldFont);
            addTableHeader(hotspotTable, "Authors", bodyBoldFont);
            addTableHeader(hotspotTable, "Score", bodyBoldFont);

            for (FileMetrics fm : topHotspots) {
                addTableCell(hotspotTable, fm.filePath(), bodyFont);
                addTableCell(hotspotTable, String.valueOf(fm.modificationCount()), bodyFont);
                addTableCell(hotspotTable, String.valueOf(fm.churn()), bodyFont);
                addTableCell(hotspotTable, String.valueOf(fm.contributorCount()), bodyFont);
                addTableCell(hotspotTable, String.format("%.1f", fm.hotspotScore()), bodyBoldFont);
            }
            document.add(hotspotTable);

            // Critical / High Risks Section
            Paragraph riskTitle = new Paragraph(String.format("Identified Risks (%d Total)", riskReport.totalRisks()), sectionTitleFont);
            riskTitle.setSpacingAfter(6);
            document.add(riskTitle);

            PdfPTable riskTable = new PdfPTable(4);
            riskTable.setWidthPercentage(100);
            riskTable.setWidths(new float[]{1.5f, 1.2f, 2.5f, 3.5f});
            riskTable.setSpacingAfter(15);

            addTableHeader(riskTable, "Severity", bodyBoldFont);
            addTableHeader(riskTable, "Risk Type", bodyBoldFont);
            addTableHeader(riskTable, "Entity", bodyBoldFont);
            addTableHeader(riskTable, "Evidence & Mitigation", bodyBoldFont);

            for (RiskItem risk : riskReport.risks().stream().limit(8).toList()) {
                addTableCell(riskTable, risk.severity().name(), bodyBoldFont);
                addTableCell(riskTable, risk.type().name(), bodyFont);
                addTableCell(riskTable, risk.entity(), bodyFont);
                addTableCell(riskTable, risk.evidence() + " -> " + risk.recommendation(), bodyFont);
            }
            document.add(riskTable);

            // Key Insights Section
            Paragraph insightTitle = new Paragraph("Key Engineering Insights & Actions", sectionTitleFont);
            insightTitle.setSpacingAfter(6);
            document.add(insightTitle);

            for (InsightItem insight : insights.stream().limit(4).toList()) {
                Paragraph p = new Paragraph("[" + insight.category().name() + " - " + insight.type().name() + "] " + insight.title(), bodyBoldFont);
                Paragraph desc = new Paragraph(insight.finding() + " " + insight.interpretation() + "\nRecommended Action: " + insight.action(), bodyFont);
                desc.setSpacingAfter(8);
                document.add(p);
                document.add(desc);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PDF report", e);
        }
    }

    private void addInfoCell(PdfPTable table, String label, String value, Font labelFont, Font valFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(6);
        cell.setBackgroundColor(new Color(241, 245, 249));
        cell.setBorderColor(new Color(203, 213, 225));
        cell.addElement(new Paragraph(label, labelFont));
        cell.addElement(new Paragraph(value != null ? value : "", valFont));
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String header, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(header, font));
        cell.setBackgroundColor(new Color(226, 232, 240));
        cell.setBorderColor(new Color(203, 213, 225));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
