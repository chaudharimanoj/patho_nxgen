package com.pathonxgen.modules.crm.entity;

import com.pathonxgen.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "referrals")
public class Referral extends BaseEntity {

    @Column(name = "referring_doctor_id", nullable = false)
    private UUID referringDoctorId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(length = 120)
    private String source;

    protected Referral() {}

    public Referral(UUID referringDoctorId, UUID patientId, String source) {
        this.referringDoctorId = referringDoctorId;
        this.patientId = patientId;
        this.source = source;
    }

    public UUID getReferringDoctorId() { return referringDoctorId; }
    public UUID getPatientId() { return patientId; }
    public String getSource() { return source; }
}
