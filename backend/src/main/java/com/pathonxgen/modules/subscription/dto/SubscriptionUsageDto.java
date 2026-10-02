package com.pathonxgen.modules.subscription.dto;

import java.time.Instant;
import java.util.Map;

public record SubscriptionUsageDto(
        String planCode,
        String planName,
        String branchScope,
        String volumeTierCode,
        String volumeTierName,
        Integer monthlyPatientLimit,
        Integer currentMonthPatientCount,
        Double usagePercentage,
        Boolean isUnlimited,
        Map<String, Boolean> features,
        String status,
        Instant renewsAt
) {}
