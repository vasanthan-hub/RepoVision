package com.repovision.analysis.api.dto;

public record FileCoupling(
        String fileA,
        String fileB,
        int coChangeCount,
        int fileAChanges,
        int fileBChanges,
        double couplingPercentage,
        String strength
) {
}
