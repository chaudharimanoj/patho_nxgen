package com.pathonxgen.modules.subscription.model;

public enum FeatureFlag {
    BILLING("billing", "Standard Billing & Invoicing"),
    REPORTS("reports", "Standard Reports & Result Exports"),
    INVENTORY("inventory", "Inventory & Reagent Tracking"),
    PATIENT_PORTAL("patient_portal", "Patient Self-Service Portal"),
    ADVANCED_ANALYTICS("advanced_analytics", "Advanced Analytics & ECharts Dashboards"),
    WHATSAPP_SMS_INTEGRATION("whatsapp_sms_integration", "Automated WhatsApp & SMS Notifications"),
    DOCTOR_COMMISSION_TRACKING("doctor_commission_tracking", "Doctor Commission & Referral Management"),
    CUSTOM_BRANDING("custom_branding", "Custom Lab Branding & Logo Styling"),
    MULTI_BRANCH_INVENTORY("multi_branch_inventory", "Multi-Branch Consolidated Inventory"),
    INTER_BRANCH_TRANSFER("inter_branch_transfer", "Inter-Branch Sample & Inventory Transfers"),
    CROSS_BRANCH_RBAC("cross_branch_rbac", "Multi-Branch Cross-Regional RBAC");

    private final String key;
    private final String description;

    FeatureFlag(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }
}
