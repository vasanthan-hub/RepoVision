package com.repovision.analysis.api.dto;

import java.util.List;

public record FileAnalyticsResponse(
        int totalFiles,
        List<FileMetrics> files
) {
}
