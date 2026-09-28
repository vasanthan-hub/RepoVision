package com.repovision.intelligence.risk;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class RiskController {

    private final RiskEngineService riskEngineService;

    public RiskController(RiskEngineService riskEngineService) {
        this.riskEngineService = riskEngineService;
    }

    @GetMapping("/{repositoryId}/intelligence/risks")
    public RiskReportResponse getRisks(@PathVariable UUID repositoryId) {
        return riskEngineService.analyzeRisks(repositoryId);
    }
}
