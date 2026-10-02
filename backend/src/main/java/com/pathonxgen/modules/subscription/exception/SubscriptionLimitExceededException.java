package com.pathonxgen.modules.subscription.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class SubscriptionLimitExceededException extends RuntimeException {

    private final String limitType;
    private final Integer currentUsage;
    private final Integer maxLimit;

    public SubscriptionLimitExceededException(String message, String limitType, Integer currentUsage, Integer maxLimit) {
        super(message);
        this.limitType = limitType;
        this.currentUsage = currentUsage;
        this.maxLimit = maxLimit;
    }

    public String getLimitType() {
        return limitType;
    }

    public Integer getCurrentUsage() {
        return currentUsage;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }
}
