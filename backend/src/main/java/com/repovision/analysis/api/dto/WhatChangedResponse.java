package com.repovision.analysis.api.dto;

import java.util.List;

public record WhatChangedResponse(
        int days,
        String periodLabel,
        List<String> highlights,
        List<String> detailedChanges
) {
}
