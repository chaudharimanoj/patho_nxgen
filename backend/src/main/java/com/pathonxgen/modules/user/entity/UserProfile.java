package com.pathonxgen.modules.user.entity;

import com.pathonxgen.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Stores tenant user business profile information mapped to Keycloak user identity (keycloak_user_id).
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @Column(name = "keycloak_user_id", nullable = false)
    private String keycloakUserId;

    @Column(name = "employee_code", length = 32)
    private String employeeCode;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(length = 120)
    private String email;

    @Column(length = 64)
    private String department;

    @Column(length = 64)
    private String designation;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    protected UserProfile() {}

    public UserProfile(String keycloakUserId, String displayName, String email, String department) {
        this.keycloakUserId = keycloakUserId;
        this.displayName = displayName;
        this.email = email;
        this.department = department;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public void setKeycloakUserId(String keycloakUserId) {
        this.keycloakUserId = keycloakUserId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
