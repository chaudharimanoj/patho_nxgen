package com.pathonxgen.modules.billing.repository;

import com.pathonxgen.modules.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<Invoice> findByTenantIdAndInvoiceNumber(UUID tenantId, String invoiceNumber);
    List<Invoice> findByTenantIdAndPatientId(UUID tenantId, UUID patientId);
}

