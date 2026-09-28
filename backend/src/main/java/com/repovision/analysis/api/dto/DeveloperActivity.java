package com.repovision.analysis.api.dto;

import java.time.LocalDate;

public record DeveloperActivity(

        LocalDate date,

        long commitCount

) {}