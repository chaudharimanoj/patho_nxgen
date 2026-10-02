package com.pathonxgen.modules.subscription.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_subscriptions", schema = "public")
public class TenantSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "volume_tier_id", nullable = false)
    private VolumeTier volumeTier;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "billing_cycle", nullable = false, length = 10)
    private String billingCycle = "MONTHLY";

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "renews_at")
    private Instant renewsAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public TenantSubscription() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public VolumeTier getVolumeTier() {
        return volumeTier;
    }

    public void setVolumeTier(VolumeTier volumeTier) {
        this.volumeTier = volumeTier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getRenewsAt() {
        return renewsAt;
    }

    public void setRenewsAt(Instant renewsAt) {
        this.renewsAt = renewsAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
