package com.pathonxgen.modules.reporting.dto;

public record OperationalKpiResponse(long dailyTests, long activePatients, double avgTurnaroundHours, double revenueToday) {
}
