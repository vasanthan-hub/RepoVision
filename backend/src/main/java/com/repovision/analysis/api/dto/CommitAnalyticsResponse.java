package com.repovision.analysis.api.dto;

import java.util.List;

public record CommitAnalyticsResponse(

        long totalCommits,

        long totalAdditions,

        long totalDeletions,

        long totalChurn,

        double averageCommitSize,

        double averageAdditionsPerCommit,

        double averageDeletionsPerCommit,

        CommitSummary largestCommit,

        double averageCommitsPerActiveDay,

        List<CommitsByDate> commitsByDate

) {}