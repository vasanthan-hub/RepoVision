package com.repovision.analysis.api.dto;

public record DeveloperFileCount(
        String developerName,
        String developerEmail,
        int primarilyOwnedFilesCount,
        double percentageOfCodebase,
        long totalContributionsChurn
) {
}
