package com.repovision.analysis.api.dto;

import java.time.LocalDate;

public record CommitsByDate(
        LocalDate date,
        long count
) {
}