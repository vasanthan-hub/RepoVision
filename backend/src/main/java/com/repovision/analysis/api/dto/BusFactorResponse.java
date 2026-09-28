package com.repovision.analysis.api.dto;

import java.util.List;

public record BusFactorResponse(
        int busFactor,
        int singleContributorFilesCount,
        int highConcentrationFilesCount,
        int totalFiles,
        List<DeveloperFileCount> keyDevelopers,
        List<String> evidence
) {
}
