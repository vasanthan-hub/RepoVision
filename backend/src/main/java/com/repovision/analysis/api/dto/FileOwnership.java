package com.repovision.analysis.api.dto;

import java.util.List;

public record FileOwnership(
        String filePath,
        String module,
        String primaryContributorName,
        String primaryContributorEmail,
        double primaryContributorPercentage,
        long totalChurn,
        int contributorCount,
        String concentrationLevel,
        List<FileContributor> contributors
) {
}
