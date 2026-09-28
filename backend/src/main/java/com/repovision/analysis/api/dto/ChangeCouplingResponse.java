package com.repovision.analysis.api.dto;

import java.util.List;

public record ChangeCouplingResponse(
        int totalCoupledPairs,
        int strongCouplingCount,
        List<FileCoupling> couplings
) {
}
