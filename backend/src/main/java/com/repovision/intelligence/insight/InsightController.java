package com.repovision.intelligence.insight;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class InsightController {

    private final InsightEngineService insightEngineService;

    public InsightController(InsightEngineService insightEngineService) {
        this.insightEngineService = insightEngineService;
    }

    @GetMapping("/{repositoryId}/intelligence/insights")
    public InsightReportResponse getInsights(@PathVariable UUID repositoryId) {
        return insightEngineService.generateInsights(repositoryId);
    }
}
