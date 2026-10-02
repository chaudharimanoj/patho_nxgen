package com.pathonxgen.modules.scheduler.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Example scheduled job. Runs per-tenant in a real implementation by
 * iterating active tenants and setting TenantContext before each run -
 * scheduled jobs sit outside the request-scoped TenantFilter so they must
 * resolve tenant context manually.
 */
@Component
public class InventoryExpiryCheckJob {

    @Scheduled(cron = "0 0 6 * * *")
    public void checkExpiringReagents() {
        // TODO: for each active tenant, set TenantContext, query inventory
        // items nearing expiry, publish notification events, clear context.
    }
}
