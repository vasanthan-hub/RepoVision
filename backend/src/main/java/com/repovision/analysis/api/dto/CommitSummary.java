package com.repovision.analysis.api.dto;

public record CommitSummary(

        String hash,

        String subject,

        long additions,

        long deletions,

        long churn

) {}