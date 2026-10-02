package com.pathonxgen.modules.tenantadmin.service;

import com.pathonxgen.core.security.KeycloakAdminClientService;
import com.pathonxgen.modules.subscription.dto.UpdateSubscriptionRequest;
import com.pathonxgen.modules.subscription.service.SubscriptionService;
import com.pathonxgen.modules.tenantadmin.dto.TenantProvisionRequest;
import com.pathonxgen.modules.tenantadmin.entity.Tenant;
import com.pathonxgen.modules.tenantadmin.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates the tenant onboarding saga:
 * 1. slug & domain availability check
 * 2. Keycloak realm creation (per-tenant realm)
 * 3. persist Tenant record in central registry
 * 4. seed default tenant settings & subscription plan/volume tier
 * 5. create initial admin user profile
 */
@Service
public class TenantProvisioningService {

    private final TenantRepository tenantRepository;
    private final KeycloakAdminClientService keycloakAdminClientService;
    private final SubscriptionService subscriptionService;

    public TenantProvisioningService(TenantRepository tenantRepository,
                                     KeycloakAdminClientService keycloakAdminClientService,
                                     SubscriptionService subscriptionService) {
        this.tenantRepository = tenantRepository;
        this.keycloakAdminClientService = keycloakAdminClientService;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public Tenant provision(TenantProvisionRequest request) {
        if (tenantRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Tenant slug already in use: " + request.slug());
        }
        if (request.domain() != null && !request.domain().isBlank() && tenantRepository.existsByDomain(request.domain())) {
            throw new IllegalArgumentException("Tenant domain already in use: " + request.domain());
        }
        String keycloakRealm = "pathonxgen-tenant-" + request.slug();
        String tenantCode = request.tenantCode() != null && !request.tenantCode().isBlank() ? request.tenantCode() : request.slug().toUpperCase();
        String domain = request.domain() != null && !request.domain().isBlank() ? request.domain() : request.slug() + ".localhost";

        String adminEmail = request.adminEmail() != null && !request.adminEmail().isBlank()
                ? request.adminEmail() : "admin@" + request.slug() + ".com";

        // 1. Automate Keycloak Realm & Role Provisioning
        keycloakAdminClientService.provisionTenantRealm(keycloakRealm, request.slug(), adminEmail);

        // 2. Save Central Tenant Registry Record
        Tenant tenant = new Tenant(request.slug(), tenantCode, domain, request.legalName(), keycloakRealm);
        String planCode = request.subscriptionPlan() != null && !request.subscriptionPlan().isBlank()
                ? request.subscriptionPlan().toUpperCase() : "STARTER";
        tenant.setSubscriptionPlan(planCode);
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        Tenant savedTenant = tenantRepository.save(tenant);

        // 3. Provision Subscription Plan & Volume Tier record
        String volumeCode = request.volumeTierCode() != null && !request.volumeTierCode().isBlank()
                ? request.volumeTierCode().toUpperCase() : "TOWN";
        subscriptionService.updateTenantSubscription(savedTenant.getId(), new UpdateSubscriptionRequest(planCode, volumeCode));

        return savedTenant;
    }
}


