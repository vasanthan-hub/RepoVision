package com.repovision.report;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{repositoryId}/reports/csv/commits")
    public ResponseEntity<byte[]> getCommitsCsv(@PathVariable UUID repositoryId) {
        byte[] csv = reportService.exportCommitsCsv(repositoryId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"repovision-commits-" + repositoryId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/{repositoryId}/reports/csv/files")
    public ResponseEntity<byte[]> getFilesCsv(@PathVariable UUID repositoryId) {
        byte[] csv = reportService.exportFilesCsv(repositoryId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"repovision-files-" + repositoryId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/{repositoryId}/reports/csv/developers")
    public ResponseEntity<byte[]> getDevelopersCsv(@PathVariable UUID repositoryId) {
        byte[] csv = reportService.exportDevelopersCsv(repositoryId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"repovision-developers-" + repositoryId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/{repositoryId}/reports/csv/risks")
    public ResponseEntity<byte[]> getRisksCsv(@PathVariable UUID repositoryId) {
        byte[] csv = reportService.exportRisksCsv(repositoryId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"repovision-risks-" + repositoryId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/{repositoryId}/reports/pdf")
    public ResponseEntity<byte[]> getHealthPdf(@PathVariable UUID repositoryId) {
        byte[] pdf = reportService.exportHealthPdf(repositoryId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"repovision-health-report-" + repositoryId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
