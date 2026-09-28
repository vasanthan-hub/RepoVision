package com.repovision.analysis.api;

import com.repovision.analysis.api.dto.BusFactorResponse;
import com.repovision.analysis.api.dto.OwnershipAnalyticsResponse;
import com.repovision.analysis.application.OwnershipAnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class OwnershipController {

    private final OwnershipAnalyticsService ownershipAnalyticsService;

    public OwnershipController(OwnershipAnalyticsService ownershipAnalyticsService) {
        this.ownershipAnalyticsService = ownershipAnalyticsService;
    }

    @GetMapping("/{repositoryId}/analytics/ownership")
    public OwnershipAnalyticsResponse getOwnershipAnalytics(@PathVariable UUID repositoryId) {
        return ownershipAnalyticsService.getOwnershipAnalytics(repositoryId);
    }

    @GetMapping("/{repositoryId}/analytics/ownership/bus-factor")
    public BusFactorResponse getBusFactor(@PathVariable UUID repositoryId) {
        return ownershipAnalyticsService.getBusFactor(repositoryId);
    }
}
