package com.repovision.analysis.api.dto;

public record MetricDelta(
        String metricName,
        double currentValue,
        double previousValue,
        double absoluteChange,
        double percentageChange,
        String unit,
        String trend
) {
    public static MetricDelta of(String metricName, double current, double previous, String unit) {
        double abs = current - previous;
        double pct;
        if (previous == 0) {
            pct = current > 0 ? 100.0 : 0.0;
        } else {
            pct = Math.round((abs / previous * 100.0) * 10.0) / 10.0;
        }

        String trend;
        if (abs > 0.0001) trend = "UP";
        else if (abs < -0.0001) trend = "DOWN";
        else trend = "FLAT";

        return new MetricDelta(
                metricName,
                Math.round(current * 10.0) / 10.0,
                Math.round(previous * 10.0) / 10.0,
                Math.round(abs * 10.0) / 10.0,
                pct,
                unit,
                trend
        );
    }
}
