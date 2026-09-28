package com.repovision.analysis.api;

import com.repovision.analysis.api.dto.PeriodComparisonResponse;
import com.repovision.analysis.api.dto.WhatChangedResponse;
import com.repovision.analysis.application.ComparisonService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class ComparisonController {

    private final ComparisonService comparisonService;

    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @GetMapping("/{repositoryId}/analytics/comparison")
    public PeriodComparisonResponse getComparison(
            @PathVariable UUID repositoryId,
            @RequestParam(defaultValue = "30") int days
    ) {
        return comparisonService.comparePeriods(repositoryId, days);
    }

    @GetMapping("/{repositoryId}/intelligence/what-changed")
    public WhatChangedResponse getWhatChanged(
            @PathVariable UUID repositoryId,
            @RequestParam(defaultValue = "30") int days
    ) {
        return comparisonService.getWhatChanged(repositoryId, days);
    }
}
