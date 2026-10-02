package com.pathonxgen.modules.tenantadmin.controller;

import com.pathonxgen.common.ApiResponse;
import com.pathonxgen.modules.subscription.dto.UpdateSubscriptionRequest;
import com.pathonxgen.modules.subscription.service.SubscriptionService;
import com.pathonxgen.modules.tenantadmin.dto.TenantProvisionRequest;
import com.pathonxgen.modules.tenantadmin.entity.Tenant;
import com.pathonxgen.modules.tenantadmin.repository.TenantRepository;
import com.pathonxgen.modules.tenantadmin.service.TenantProvisioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Platform Control Plane endpoint for Client/Organization Management.
 */
@RestController
@RequestMapping("/api/v1/platform/tenants")
@Tag(name = "Tenant Admin", description = "Platform tenant management and provisioning")
public class TenantAdminController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TenantAdminController.class);
    private final TenantProvisioningService provisioningService;
    private final TenantRepository tenantRepository;
    private final JdbcTemplate jdbcTemplate;
    private final com.pathonxgen.core.security.KeycloakAdminClientService keycloakAdminClientService;
    private final SubscriptionService subscriptionService;

    public TenantAdminController(TenantProvisioningService provisioningService,
                                 TenantRepository tenantRepository,
                                 JdbcTemplate jdbcTemplate,
                                 com.pathonxgen.core.security.KeycloakAdminClientService keycloakAdminClientService,
                                 SubscriptionService subscriptionService) {
        this.provisioningService = provisioningService;
        this.tenantRepository = tenantRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.keycloakAdminClientService = keycloakAdminClientService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ApiResponse<List<Tenant>> getAllTenants() {
        List<Tenant> tenants = tenantRepository.findAll();
        log.info("[TenantAdminController] Fetched {} registered tenants from DB", tenants.size());
        return ApiResponse.ok(tenants, "Tenants retrieved successfully");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Tenant> provision(@Valid @RequestBody TenantProvisionRequest request) {
        log.info("[TenantAdminController] Provisioning new tenant: slug='{}', legalName='{}'", request.slug(), request.legalName());
        return ApiResponse.ok(provisioningService.provision(request), "Tenant provisioning started");
    }

    @GetMapping("/{id}")
    public ApiResponse<Tenant> getTenantById(@PathVariable UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + id));
        return ApiResponse.ok(tenant, "Tenant retrieved successfully");
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Tenant> updateTenant(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + id));

        if (body.containsKey("legalName") && body.get("legalName") != null && !body.get("legalName").isBlank()) {
            tenant.setLegalName(body.get("legalName").trim());
        }

        if (body.containsKey("slug") && body.get("slug") != null && !body.get("slug").isBlank()) {
            String newSlug = body.get("slug").trim().toLowerCase();
            if (!newSlug.equals(tenant.getSlug()) && tenantRepository.existsBySlug(newSlug)) {
                throw new IllegalArgumentException("Tenant slug already in use: " + newSlug);
            }
            tenant.setSlug(newSlug);
        }

        if (body.containsKey("tenantCode") && body.get("tenantCode") != null && !body.get("tenantCode").isBlank()) {
            String newCode = body.get("tenantCode").trim().toUpperCase();
            if (!newCode.equals(tenant.getTenantCode()) && tenantRepository.existsByTenantCode(newCode)) {
                throw new IllegalArgumentException("Tenant code already in use: " + newCode);
            }
            tenant.setTenantCode(newCode);
        }

        if (body.containsKey("domain") && body.get("domain") != null && !body.get("domain").isBlank()) {
            String newDomain = body.get("domain").trim().toLowerCase();
            if (!newDomain.equals(tenant.getDomain()) && tenantRepository.existsByDomain(newDomain)) {
                throw new IllegalArgumentException("Domain already in use: " + newDomain);
            }
            tenant.setDomain(newDomain);
        }

        boolean subscriptionChanged = false;
        String planCode = tenant.getSubscriptionPlan() != null ? tenant.getSubscriptionPlan() : "STARTER";
        String volumeCode = "TOWN";

        if (body.containsKey("subscriptionPlan") && body.get("subscriptionPlan") != null && !body.get("subscriptionPlan").isBlank()) {
            planCode = body.get("subscriptionPlan").trim().toUpperCase();
            tenant.setSubscriptionPlan(planCode);
            subscriptionChanged = true;
        }

        if (body.containsKey("volumeTierCode") && body.get("volumeTierCode") != null && !body.get("volumeTierCode").isBlank()) {
            volumeCode = body.get("volumeTierCode").trim().toUpperCase();
            subscriptionChanged = true;
        }

        if (subscriptionChanged) {
            subscriptionService.updateTenantSubscription(id, new UpdateSubscriptionRequest(planCode, volumeCode));
        }

        if (body.containsKey("status") && body.get("status") != null && !body.get("status").isBlank()) {
            Tenant.TenantStatus newStatus = Tenant.TenantStatus.valueOf(body.get("status").trim().toUpperCase());
            tenant.setStatus(newStatus);
            if (newStatus == Tenant.TenantStatus.SUSPENDED || newStatus == Tenant.TenantStatus.DEACTIVATED) {
                keycloakAdminClientService.logoutAllRealmSessions(tenant.getKeycloakRealm());
                log.warn("[TenantAdminController] Organization '{}' ({}) updated to {}. Triggered automatic logout across all active devices for realm '{}'",
                        tenant.getLegalName(), tenant.getSlug(), newStatus, tenant.getKeycloakRealm());
            }
        }

        tenant.setUpdatedAt(java.time.Instant.now());
        Tenant saved = tenantRepository.save(tenant);
        log.info("[TenantAdminController] Updated tenant '{}' (ID: {}) in database", saved.getSlug(), saved.getId());
        return ApiResponse.ok(saved, "Tenant updated successfully");
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ApiResponse<Tenant> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + id));
        String statusStr = body.get("status");
        if (statusStr != null) {
            Tenant.TenantStatus oldStatus = tenant.getStatus();
            Tenant.TenantStatus newStatus = Tenant.TenantStatus.valueOf(statusStr.toUpperCase());
            tenant.setStatus(newStatus);
            tenant.setUpdatedAt(java.time.Instant.now());
            tenantRepository.save(tenant);

            if (newStatus == Tenant.TenantStatus.SUSPENDED || newStatus == Tenant.TenantStatus.DEACTIVATED) {
                keycloakAdminClientService.logoutAllRealmSessions(tenant.getKeycloakRealm());
                log.warn("[TenantAdminController] UPDATED tenant '{}' (ID: {}) status: {} -> {}. Triggered automatic logout across all active devices for realm '{}'",
                        tenant.getSlug(), id, oldStatus, newStatus, tenant.getKeycloakRealm());
            }
        }
        return ApiResponse.ok(tenant, "Tenant status updated to " + tenant.getStatus());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<String> deleteTenant(@PathVariable UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + id));
        
        String tenantSlug = tenant.getSlug();

        // 1. Clean up dependent records in public control plane tables to prevent FK violations
        jdbcTemplate.update("DELETE FROM public.platform_billing_invoices WHERE tenant_id = ?", id);
        jdbcTemplate.update("DELETE FROM public.tenant_subscriptions WHERE tenant_id = ?", id);
        jdbcTemplate.update("DELETE FROM public.tenant_monthly_usage WHERE tenant_id = ?", id);
        jdbcTemplate.update("DELETE FROM public.subscriptions WHERE tenant_id = ?", id);
        jdbcTemplate.update("DELETE FROM public.user_profiles WHERE tenant_id = ?", id);
        jdbcTemplate.update("DELETE FROM public.tenant_configurations WHERE tenant_id = ?", id);
        jdbcTemplate.update("DELETE FROM public.audit_log WHERE tenant_id = ?", id);

        // 2. Remove tenant entry from central registry
        tenantRepository.delete(tenant);
        log.warn("[TenantAdminController] DELETED tenant '{}' (ID: {}) and all dependent records from database", tenantSlug, id);
        return ApiResponse.ok("Tenant deleted successfully", "Tenant deleted successfully");
    }
}
