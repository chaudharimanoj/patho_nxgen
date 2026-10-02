package com.pathonxgen.modules.patient.entity;

import com.pathonxgen.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "patients", indexes = {
        @jakarta.persistence.Index(name = "idx_patient_phone", columnList = "phone"),
        @jakarta.persistence.Index(name = "idx_patient_mrn", columnList = "mrn", unique = true)
})
public class Patient extends BaseEntity {

    @Column(nullable = false, unique = true, length = 32)
    private String mrn;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Gender gender;

    @Column(length = 20)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 64)
    private String insuranceProvider;

    @Column(length = 64)
    private String corporateAccountCode;

    public enum Gender { MALE, FEMALE, OTHER, UNDISCLOSED }

    protected Patient() {
    }

    public Patient(String mrn, String fullName, LocalDate dateOfBirth, Gender gender) {
        this.mrn = mrn;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public String getMrn() { return mrn; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }
    public String getCorporateAccountCode() { return corporateAccountCode; }
    public void setCorporateAccountCode(String corporateAccountCode) { this.corporateAccountCode = corporateAccountCode; }
}
