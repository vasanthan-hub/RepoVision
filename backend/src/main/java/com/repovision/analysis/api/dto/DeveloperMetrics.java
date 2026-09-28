package com.repovision.analysis.api.dto;

import java.util.List;

public record DeveloperMetrics(

        String authorName,

        String authorEmail,

        long commitCount,

        long additions,

        long deletions,

        long churn,

        double commitPercentage,

        double churnPercentage,

        List<DeveloperActivity> activityByDate

) {}