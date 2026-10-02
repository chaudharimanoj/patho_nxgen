package com.pathonxgen.modules.billing.service;

import com.pathonxgen.core.exception.ResourceNotFoundException;
import com.pathonxgen.core.tenant.TenantContext;
import com.pathonxgen.modules.billing.entity.Invoice;
import com.pathonxgen.modules.billing.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private static final AtomicLong SEQ = new AtomicLong(1);

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice createInvoice(UUID patientId, BigDecimal amount, Invoice.PaymentMode mode) {
        String number = "INV-" + String.format("%08d", SEQ.getAndIncrement());
        return invoiceRepository.save(new Invoice(number, patientId, amount, mode));
    }

    @Transactional(readOnly = true)
    public Invoice getById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return invoiceRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }
}

