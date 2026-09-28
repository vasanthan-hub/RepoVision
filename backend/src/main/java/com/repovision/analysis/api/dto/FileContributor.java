package com.repovision.analysis.api.dto;

public record FileContributor(
        String name,
        String email,
        long commitsCount,
        long linesChanged,
        double percentage
) {
}
