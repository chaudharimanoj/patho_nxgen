package com.pathonxgen.modules.billing.entity;

import com.pathonxgen.common.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @Column(nullable = false, unique = true, length = 32)
    private String invoiceNumber;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    public enum PaymentMode { CASH, CARD, UPI, INSURANCE, CORPORATE }
    public enum InvoiceStatus { DRAFT, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED }

    protected Invoice() {}

    public Invoice(String invoiceNumber, UUID patientId, BigDecimal amount, PaymentMode paymentMode) {
        this.invoiceNumber = invoiceNumber;
        this.patientId = patientId;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.status = InvoiceStatus.DRAFT;
    }

    public String getInvoiceNumber() { return invoiceNumber; }
    public UUID getPatientId() { return patientId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
}
