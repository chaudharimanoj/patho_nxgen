package com.pathonxgen.modules.subscription.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "branches", schema = "public", uniqueConstraints = {
    @UniqueConstraint(name = "uq_tenant_branch_code", columnNames = {"tenant_id", "branch_code"})
})
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "branch_code", nullable = false, length = 32)
    private String branchCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120)
    private String city;

    @Column(name = "is_main_branch", nullable = false)
    private Boolean isMainBranch = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Branch() {}

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

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Boolean getIsMainBranch() {
        return isMainBranch;
    }

    public void setIsMainBranch(Boolean isMainBranch) {
        this.isMainBranch = isMainBranch;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
