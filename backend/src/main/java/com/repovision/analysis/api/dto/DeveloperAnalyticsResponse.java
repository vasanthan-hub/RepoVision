package com.repovision.analysis.api.dto;

import java.util.List;

public record DeveloperAnalyticsResponse(

        int totalDevelopers,

        List<DeveloperMetrics> developers

) {}