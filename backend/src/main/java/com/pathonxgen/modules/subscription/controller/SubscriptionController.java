package com.pathonxgen.modules.subscription.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.core.tenant.TenantContext;
import com.pathonxgen.modules.subscription.dto.PlanCatalogDto;
import com.pathonxgen.modules.subscription.dto.SubscriptionUsageDto;
import com.pathonxgen.modules.subscription.dto.UpdateSubscriptionRequest;
import com.pathonxgen.modules.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Subscription Management", description = "Tenant subscription tier, volume limits, and plan catalog management")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/api/v1/tenant/subscription")
    @Operation(summary = "Get current tenant subscription details, feature flags, and patient volume usage")
    public ApiResponse<SubscriptionUsageDto> getCurrentTenantSubscription(
            @RequestParam(required = false) UUID tenantId) {
        if (tenantId == null) {
            tenantId = TenantContext.getTenantId();
        }
        if (tenantId == null) {
            // Default to demo tenant UUID if no tenant context is set (e.g. Platform Super Admin)
            tenantId = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000001");
        }
        return ApiResponse.ok(subscriptionService.getTenantSubscriptionUsage(tenantId));
    }

    @GetMapping("/api/v1/platform/subscriptions/tenants/{tenantId}")
    @Operation(summary = "Get subscription details, feature flags, and volume usage for a specific tenant")
    public ApiResponse<SubscriptionUsageDto> getTenantSubscriptionById(@PathVariable UUID tenantId) {
        return ApiResponse.ok(subscriptionService.getTenantSubscriptionUsage(tenantId));
    }

    @GetMapping("/api/v1/platform/subscriptions/catalog")
    @Operation(summary = "Get available subscription plans and volume tier matrix catalog")
    public ApiResponse<PlanCatalogDto> getCatalog() {
        return ApiResponse.ok(subscriptionService.getCatalog());
    }

    @PutMapping("/api/v1/platform/subscriptions/tenants/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Update or upgrade a tenant's subscription tier and volume tier")
    public ApiResponse<SubscriptionUsageDto> updateTenantSubscription(
            @PathVariable UUID tenantId,
            @Valid @RequestBody UpdateSubscriptionRequest request) {
        return ApiResponse.ok(
                subscriptionService.updateTenantSubscription(tenantId, request),
                "Tenant subscription updated successfully"
        );
    }
}
