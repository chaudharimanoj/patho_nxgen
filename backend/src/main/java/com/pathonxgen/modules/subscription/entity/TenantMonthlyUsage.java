package com.pathonxgen.modules.subscription.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_monthly_usage", schema = "public", uniqueConstraints = {
    @UniqueConstraint(name = "uq_tenant_monthly_usage", columnNames = {"tenant_id", "year_month"})
})
public class TenantMonthlyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth; // e.g. "2026-09"

    @Column(name = "patient_count", nullable = false)
    private Integer patientCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public TenantMonthlyUsage() {}

    public TenantMonthlyUsage(UUID tenantId, String yearMonth, Integer patientCount) {
        this.tenantId = tenantId;
        this.yearMonth = yearMonth;
        this.patientCount = patientCount;
    }

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

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public Integer getPatientCount() {
        return patientCount;
    }

    public void setPatientCount(Integer patientCount) {
        this.patientCount = patientCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
