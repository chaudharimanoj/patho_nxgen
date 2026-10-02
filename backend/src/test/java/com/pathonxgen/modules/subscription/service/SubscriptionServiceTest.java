package com.pathonxgen.modules.subscription.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathonxgen.modules.subscription.dto.SubscriptionUsageDto;
import com.pathonxgen.modules.subscription.dto.UpdateSubscriptionRequest;
import com.pathonxgen.modules.subscription.entity.SubscriptionPlan;
import com.pathonxgen.modules.subscription.entity.TenantMonthlyUsage;
import com.pathonxgen.modules.subscription.entity.TenantSubscription;
import com.pathonxgen.modules.subscription.entity.VolumeTier;
import com.pathonxgen.modules.subscription.exception.SubscriptionLimitExceededException;
import com.pathonxgen.modules.subscription.repository.SubscriptionPlanRepository;
import com.pathonxgen.modules.subscription.repository.TenantMonthlyUsageRepository;
import com.pathonxgen.modules.subscription.repository.TenantSubscriptionRepository;
import com.pathonxgen.modules.subscription.repository.VolumeTierRepository;
import com.pathonxgen.modules.tenantadmin.entity.Tenant;
import com.pathonxgen.modules.tenantadmin.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private TenantSubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository planRepository;

    @Mock
    private VolumeTierRepository volumeTierRepository;

    @Mock
    private TenantMonthlyUsageRepository usageRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SubscriptionService subscriptionService;

    private UUID tenantId;
    private SubscriptionPlan starterPlan;
    private VolumeTier villageTier;
    private TenantSubscription activeSubscription;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();

        starterPlan = new SubscriptionPlan();
        starterPlan.setCode("STARTER");
        starterPlan.setName("Starter Plan");
        starterPlan.setBranchScope("SINGLE_LAB");
        starterPlan.setFeatures("{\"billing\": true, \"advanced_analytics\": false}");

        villageTier = new VolumeTier();
        villageTier.setCode("VILLAGE");
        villageTier.setName("Village Tier");
        villageTier.setMonthlyPatientLimit(100);

        activeSubscription = new TenantSubscription();
        activeSubscription.setTenantId(tenantId);
        activeSubscription.setPlan(starterPlan);
        activeSubscription.setVolumeTier(villageTier);
        activeSubscription.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should return accurate usage stats and percentage when under limit")
    void testGetTenantSubscriptionUsage_Normal() {
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(activeSubscription));
        when(usageRepository.findByTenantIdAndYearMonth(eq(tenantId), anyString()))
                .thenReturn(Optional.of(new TenantMonthlyUsage(tenantId, "2026-09", 50)));

        SubscriptionUsageDto usage = subscriptionService.getTenantSubscriptionUsage(tenantId);

        assertNotNull(usage);
        assertEquals("STARTER", usage.planCode());
        assertEquals("VILLAGE", usage.volumeTierCode());
        assertEquals(100, usage.monthlyPatientLimit());
        assertEquals(50, usage.currentMonthPatientCount());
        assertEquals(50.0, usage.usagePercentage());
        assertFalse(usage.isUnlimited());
        assertTrue(usage.features().get("billing"));
        assertFalse(usage.features().get("advanced_analytics"));
    }

    @Test
    @DisplayName("Should throw SubscriptionLimitExceededException when patient limit is exceeded")
    void testCheckPatientVolumeLimit_Exceeded() {
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(activeSubscription));
        when(usageRepository.findByTenantIdAndYearMonth(eq(tenantId), anyString()))
                .thenReturn(Optional.of(new TenantMonthlyUsage(tenantId, "2026-09", 100)));

        assertThrows(SubscriptionLimitExceededException.class, () ->
                subscriptionService.checkPatientVolumeLimit(tenantId)
        );
    }

    @Test
    @DisplayName("Should not throw when patient count is within limit")
    void testCheckPatientVolumeLimit_WithinLimit() {
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(activeSubscription));
        when(usageRepository.findByTenantIdAndYearMonth(eq(tenantId), anyString()))
                .thenReturn(Optional.of(new TenantMonthlyUsage(tenantId, "2026-09", 99)));

        assertDoesNotThrow(() -> subscriptionService.checkPatientVolumeLimit(tenantId));
    }

    @Test
    @DisplayName("Should allow unlimited registrations for METRO tier (-1 limit)")
    void testCheckPatientVolumeLimit_Unlimited() {
        VolumeTier metroTier = new VolumeTier();
        metroTier.setCode("METRO");
        metroTier.setMonthlyPatientLimit(-1);
        activeSubscription.setVolumeTier(metroTier);

        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(activeSubscription));

        assertDoesNotThrow(() -> subscriptionService.checkPatientVolumeLimit(tenantId));
    }

    @Test
    @DisplayName("Should successfully update tenant plan and volume tier")
    void testUpdateTenantSubscription() {
        SubscriptionPlan premiumPlan = new SubscriptionPlan();
        premiumPlan.setCode("PREMIUM");
        premiumPlan.setName("Premium Plan");
        premiumPlan.setBranchScope("SINGLE_LAB");
        premiumPlan.setFeatures("{\"advanced_analytics\": true}");

        VolumeTier cityTier = new VolumeTier();
        cityTier.setCode("CITY");
        cityTier.setName("City Tier");
        cityTier.setMonthlyPatientLimit(5000);

        when(planRepository.findByCode("PREMIUM")).thenReturn(Optional.of(premiumPlan));
        when(volumeTierRepository.findByCode("CITY")).thenReturn(Optional.of(cityTier));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(activeSubscription));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(new Tenant("acme", "ACME", "acme.local", "Acme Labs", "acme-realm")));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usageRepository.findByTenantIdAndYearMonth(eq(tenantId), anyString()))
                .thenReturn(Optional.of(new TenantMonthlyUsage(tenantId, "2026-09", 120)));

        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest("PREMIUM", "CITY");
        SubscriptionUsageDto updated = subscriptionService.updateTenantSubscription(tenantId, request);

        assertNotNull(updated);
        assertEquals("PREMIUM", updated.planCode());
        assertEquals("CITY", updated.volumeTierCode());
        assertEquals(5000, updated.monthlyPatientLimit());
        verify(subscriptionRepository).save(activeSubscription);
    }
}
