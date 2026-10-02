package com.pathonxgen.modules.laboratory.service;

import com.pathonxgen.core.exception.ResourceNotFoundException;
import com.pathonxgen.core.tenant.TenantContext;
import com.pathonxgen.modules.laboratory.entity.Sample;
import com.pathonxgen.modules.laboratory.entity.Sample.SampleStatus;
import com.pathonxgen.modules.laboratory.repository.SampleRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.pathonxgen.config.RabbitMqConfig.DOMAIN_EVENTS_EXCHANGE;

/**
 * Enforces the sample lifecycle state machine described in the business
 * model doc (Collection -> Barcode -> Dispatch -> Processing -> Quality
 * Control -> Verification -> Approval -> Report Delivery). Any transition
 * not present in ALLOWED_TRANSITIONS is rejected, and every accepted
 * transition publishes a domain event so notification/reporting modules
 * can react without coupling to this service directly.
 */
@Service
@Transactional
public class SampleLifecycleService {

    private static final Map<SampleStatus, Set<SampleStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(SampleStatus.class);
    static {
        ALLOWED_TRANSITIONS.put(SampleStatus.REGISTERED, EnumSet.of(SampleStatus.COLLECTED, SampleStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(SampleStatus.COLLECTED, EnumSet.of(SampleStatus.DISPATCHED, SampleStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(SampleStatus.DISPATCHED, EnumSet.of(SampleStatus.RECEIVED, SampleStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(SampleStatus.RECEIVED, EnumSet.of(SampleStatus.PROCESSING, SampleStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(SampleStatus.PROCESSING, EnumSet.of(SampleStatus.QUALITY_CHECK, SampleStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(SampleStatus.QUALITY_CHECK, EnumSet.of(SampleStatus.VERIFIED, SampleStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(SampleStatus.VERIFIED, EnumSet.of(SampleStatus.APPROVED, SampleStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(SampleStatus.APPROVED, EnumSet.of(SampleStatus.REPORT_DELIVERED));
    }

    private final SampleRepository sampleRepository;
    private final RabbitTemplate rabbitTemplate;

    public SampleLifecycleService(SampleRepository sampleRepository, RabbitTemplate rabbitTemplate) {
        this.sampleRepository = sampleRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Sample registerSample(String barcode, UUID patientId, String testType) {
        Sample sample = new Sample(barcode, patientId, testType);
        return sampleRepository.save(sample);
    }

    public Sample transition(UUID sampleId, SampleStatus target, String actorRole) {
        UUID tenantId = TenantContext.getTenantId();
        Sample sample = sampleRepository.findByTenantIdAndId(tenantId, sampleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sample not found: " + sampleId));

        Set<SampleStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(sample.getStatus(), Set.of());
        if (!allowed.contains(target)) {
            throw new IllegalStateException(
                    "Cannot transition sample %s from %s to %s".formatted(sample.getBarcode(), sample.getStatus(), target));
        }

        sample.setStatus(target);
        Sample saved = sampleRepository.save(sample);

        rabbitTemplate.convertAndSend(DOMAIN_EVENTS_EXCHANGE, "laboratory.sample.notify.status-changed",
                Map.of("tenantId", tenantId, "sampleId", saved.getId(), "barcode", saved.getBarcode(), "status", saved.getStatus().name(), "actorRole", actorRole));

        return saved;
    }
}

