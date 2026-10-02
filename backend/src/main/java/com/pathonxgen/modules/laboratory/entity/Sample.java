package com.pathonxgen.modules.laboratory.entity;

import com.pathonxgen.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "samples", indexes = {
        @jakarta.persistence.Index(name = "idx_sample_barcode", columnList = "barcode", unique = true),
        @jakarta.persistence.Index(name = "idx_sample_patient", columnList = "patient_id")
})
public class Sample extends BaseEntity {

    @Column(nullable = false, unique = true, length = 32)
    private String barcode;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(nullable = false, length = 64)
    private String testType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SampleStatus status;

    @Column(length = 64)
    private String collectedBy;

    @Column(length = 64)
    private String verifiedBy;

    @Column(length = 64)
    private String approvedBy;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    public enum SampleStatus {
        REGISTERED, COLLECTED, DISPATCHED, RECEIVED, PROCESSING,
        QUALITY_CHECK, VERIFIED, APPROVED, REPORT_DELIVERED, REJECTED
    }

    protected Sample() {
    }

    public Sample(String barcode, UUID patientId, String testType) {
        this.barcode = barcode;
        this.patientId = patientId;
        this.testType = testType;
        this.status = SampleStatus.REGISTERED;
    }

    public String getBarcode() { return barcode; }
    public UUID getPatientId() { return patientId; }
    public String getTestType() { return testType; }
    public SampleStatus getStatus() { return status; }
    public void setStatus(SampleStatus status) { this.status = status; }
    public String getCollectedBy() { return collectedBy; }
    public void setCollectedBy(String collectedBy) { this.collectedBy = collectedBy; }
    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
