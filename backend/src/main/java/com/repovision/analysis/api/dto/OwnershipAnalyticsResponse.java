package com.repovision.analysis.api.dto;

import java.util.List;

public record OwnershipAnalyticsResponse(
        int totalFiles,
        BusFactorResponse busFactor,
        List<FileOwnership> fileOwnerships
) {
}
