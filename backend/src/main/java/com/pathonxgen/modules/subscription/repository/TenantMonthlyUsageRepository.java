package com.pathonxgen.modules.subscription.repository;

import com.pathonxgen.modules.subscription.entity.TenantMonthlyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantMonthlyUsageRepository extends JpaRepository<TenantMonthlyUsage, UUID> {
    Optional<TenantMonthlyUsage> findByTenantIdAndYearMonth(UUID tenantId, String yearMonth);
}
