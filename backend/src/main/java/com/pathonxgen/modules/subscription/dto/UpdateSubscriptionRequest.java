package com.pathonxgen.modules.subscription.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSubscriptionRequest(
        @NotBlank String planCode,
        @NotBlank String volumeTierCode
) {}
