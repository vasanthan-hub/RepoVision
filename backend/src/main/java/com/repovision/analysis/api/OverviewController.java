package com.repovision.analysis.api;

import com.repovision.analysis.api.dto.RepositoryOverviewResponse;
import com.repovision.analysis.application.OverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class OverviewController {

    private final OverviewService overviewService;

    public OverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping("/{repositoryId}/overview")
    public RepositoryOverviewResponse getOverview(@PathVariable UUID repositoryId) {
        return overviewService.getOverview(repositoryId);
    }
}
