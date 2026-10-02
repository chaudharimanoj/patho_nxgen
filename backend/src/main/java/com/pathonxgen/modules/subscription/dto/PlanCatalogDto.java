package com.pathonxgen.modules.subscription.dto;

import com.pathonxgen.modules.subscription.entity.SubscriptionPlan;
import com.pathonxgen.modules.subscription.entity.VolumeTier;

import java.util.List;

public record PlanCatalogDto(
        List<SubscriptionPlan> plans,
        List<VolumeTier> volumeTiers
) {}
