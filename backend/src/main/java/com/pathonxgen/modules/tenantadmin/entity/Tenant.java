package com.pathonxgen.modules.tenantadmin.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Lives in the public schema - this is the central registry of tenants,
 * mapping host domains, tenant codes, and Keycloak realms.
 */
@Entity
@Table(name = "tenants", schema = "public")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String slug;

    @Column(name = "tenant_code", unique = true, length = 32)
    private String tenantCode;

    @Column(name = "domain", unique = true, length = 255)
    private String domain;

    @Column(name = "legal_name", nullable = false, length = 255)
    private String legalName;

    @Column(name = "keycloak_realm", nullable = false, length = 64)
    private String keycloakRealm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantStatus status;

    @Column(name = "subscription_plan", length = 64)
    private String subscriptionPlan;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum TenantStatus { PROVISIONING, PENDING, ACTIVE, SUSPENDED, DEACTIVATED, DELETED }

    protected Tenant() {}

    public Tenant(String slug, String tenantCode, String domain, String legalName, String keycloakRealm) {
        this.slug = slug;
        this.tenantCode = tenantCode;
        this.domain = domain;
        this.legalName = legalName;
        this.keycloakRealm = keycloakRealm;
        this.status = TenantStatus.ACTIVE;
        this.subscriptionPlan = "STARTER";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTenantCode() { return tenantCode != null ? tenantCode : slug; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getKeycloakRealm() { return keycloakRealm; }
    public void setKeycloakRealm(String keycloakRealm) { this.keycloakRealm = keycloakRealm; }
    public TenantStatus getStatus() { return status; }
    public void setStatus(TenantStatus status) { this.status = status; }
    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

