package com.pathonxgen.modules.subscription.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathonxgen.modules.subscription.dto.PlanCatalogDto;
import com.pathonxgen.modules.subscription.dto.SubscriptionUsageDto;
import com.pathonxgen.modules.subscription.dto.UpdateSubscriptionRequest;
import com.pathonxgen.modules.subscription.entity.*;
import com.pathonxgen.modules.subscription.exception.SubscriptionLimitExceededException;
import com.pathonxgen.modules.subscription.repository.*;
import com.pathonxgen.modules.tenantadmin.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final VolumeTierRepository volumeTierRepository;
    private final TenantMonthlyUsageRepository usageRepository;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;

    public SubscriptionService(TenantSubscriptionRepository subscriptionRepository,
                               SubscriptionPlanRepository planRepository,
                               VolumeTierRepository volumeTierRepository,
                               TenantMonthlyUsageRepository usageRepository,
                               TenantRepository tenantRepository,
                               ObjectMapper objectMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.volumeTierRepository = volumeTierRepository;
        this.usageRepository = usageRepository;
        this.tenantRepository = tenantRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public SubscriptionUsageDto getTenantSubscriptionUsage(UUID tenantId) {
        TenantSubscription sub = getOrCreateDefaultSubscription(tenantId);
        String currentMonth = YearMonth.now().format(YEAR_MONTH_FORMATTER);

        TenantMonthlyUsage usage = usageRepository.findByTenantIdAndYearMonth(tenantId, currentMonth)
                .orElseGet(() -> new TenantMonthlyUsage(tenantId, currentMonth, 0));

        Integer limit = sub.getVolumeTier().getMonthlyPatientLimit();
        boolean unlimited = limit == null || limit < 0;
        int currentCount = usage.getPatientCount();

        double usagePercentage = 0.0;
        if (!unlimited && limit > 0) {
            usagePercentage = Math.min(100.0, Math.round(((double) currentCount / limit) * 100.0 * 10.0) / 10.0);
        }

        Map<String, Boolean> features = parseFeatures(sub.getPlan().getFeatures());

        return new SubscriptionUsageDto(
                sub.getPlan().getCode(),
                sub.getPlan().getName(),
                sub.getPlan().getBranchScope(),
                sub.getVolumeTier().getCode(),
                sub.getVolumeTier().getName(),
                unlimited ? null : limit,
                currentCount,
                usagePercentage,
                unlimited,
                features,
                sub.getStatus(),
                sub.getRenewsAt()
        );
    }

    @Transactional(readOnly = true)
    public void checkPatientVolumeLimit(UUID tenantId) {
        TenantSubscription sub = getOrCreateDefaultSubscription(tenantId);
        Integer limit = sub.getVolumeTier().getMonthlyPatientLimit();

        // Unlimited tier
        if (limit == null || limit < 0) {
            return;
        }

        String currentMonth = YearMonth.now().format(YEAR_MONTH_FORMATTER);
        int currentCount = usageRepository.findByTenantIdAndYearMonth(tenantId, currentMonth)
                .map(TenantMonthlyUsage::getPatientCount)
                .orElse(0);

        if (currentCount >= limit) {
            log.warn("[SubscriptionService] Tenant {} has reached patient limit of {} for month {}",
                    tenantId, limit, currentMonth);
            throw new SubscriptionLimitExceededException(
                    String.format("Monthly patient registration limit of %d reached for Tier %s (%s). Please upgrade your volume tier.",
                            limit, sub.getVolumeTier().getName(), sub.getVolumeTier().getCode()),
                    "PATIENT_VOLUME",
                    currentCount,
                    limit
            );
        }
    }

    @Transactional
    public void incrementPatientUsage(UUID tenantId) {
        String currentMonth = YearMonth.now().format(YEAR_MONTH_FORMATTER);
        TenantMonthlyUsage usage = usageRepository.findByTenantIdAndYearMonth(tenantId, currentMonth)
                .orElseGet(() -> new TenantMonthlyUsage(tenantId, currentMonth, 0));

        usage.setPatientCount(usage.getPatientCount() + 1);
        usage.setUpdatedAt(java.time.Instant.now());
        usageRepository.save(usage);
        log.info("[SubscriptionService] Tenant {} monthly patient usage incremented to {} for {}", tenantId, usage.getPatientCount(), currentMonth);
    }

    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(UUID tenantId, String featureKey) {
        TenantSubscription sub = getOrCreateDefaultSubscription(tenantId);
        Map<String, Boolean> features = parseFeatures(sub.getPlan().getFeatures());
        return Boolean.TRUE.equals(features.get(featureKey));
    }

    @Transactional
    public SubscriptionUsageDto updateTenantSubscription(UUID tenantId, UpdateSubscriptionRequest request) {
        SubscriptionPlan plan = planRepository.findByCode(request.planCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid subscription plan code: " + request.planCode()));

        VolumeTier volumeTier = volumeTierRepository.findByCode(request.volumeTierCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid volume tier code: " + request.volumeTierCode()));

        TenantSubscription sub = subscriptionRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    TenantSubscription newSub = new TenantSubscription();
                    newSub.setTenantId(tenantId);
                    return newSub;
                });

        sub.setPlan(plan);
        sub.setVolumeTier(volumeTier);
        sub.setStatus("ACTIVE");
        sub.setUpdatedAt(java.time.Instant.now());

        subscriptionRepository.save(sub);

        // Sync tenant entity subscription_plan column
        tenantRepository.findById(tenantId).ifPresent(tenant -> {
            tenant.setSubscriptionPlan(plan.getCode());
            tenant.setUpdatedAt(java.time.Instant.now());
            tenantRepository.save(tenant);
        });

        log.info("[SubscriptionService] Updated subscription for tenant {}: Plan={}, VolumeTier={}",
                tenantId, plan.getCode(), volumeTier.getCode());

        return getTenantSubscriptionUsage(tenantId);
    }

    @Transactional(readOnly = true)
    public PlanCatalogDto getCatalog() {
        return new PlanCatalogDto(
                planRepository.findAllByOrderByDisplayOrderAsc(),
                volumeTierRepository.findAllByOrderByDisplayOrderAsc()
        );
    }

    private TenantSubscription getOrCreateDefaultSubscription(UUID tenantId) {
        return subscriptionRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    SubscriptionPlan plan = planRepository.findByCode("PREMIUM")
                            .or(() -> planRepository.findByCode("STARTER"))
                            .orElseThrow(() -> new IllegalStateException("Default subscription plan not found"));

                    VolumeTier volumeTier = volumeTierRepository.findByCode("CITY")
                            .or(() -> volumeTierRepository.findByCode("TOWN"))
                            .orElseThrow(() -> new IllegalStateException("Default volume tier not found"));

                    TenantSubscription defaultSub = new TenantSubscription();
                    defaultSub.setTenantId(tenantId);
                    defaultSub.setPlan(plan);
                    defaultSub.setVolumeTier(volumeTier);
                    defaultSub.setStatus("ACTIVE");
                    return subscriptionRepository.save(defaultSub);
                });
    }

    private Map<String, Boolean> parseFeatures(String jsonFeatures) {
        if (jsonFeatures == null || jsonFeatures.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonFeatures, new TypeReference<Map<String, Boolean>>() {});
        } catch (Exception e) {
            log.error("[SubscriptionService] Failed to parse JSON features string: {}", jsonFeatures, e);
            return new HashMap<>();
        }
    }
}
