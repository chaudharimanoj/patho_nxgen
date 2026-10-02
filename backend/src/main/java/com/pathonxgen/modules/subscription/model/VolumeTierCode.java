package com.pathonxgen.modules.subscription.model;

public enum VolumeTierCode {
    VILLAGE(100),
    TOWN(500),
    CITY(5000),
    METRO(-1); // -1 indicates Unlimited

    private final Integer patientLimit;

    VolumeTierCode(Integer patientLimit) {
        this.patientLimit = patientLimit;
    }

    public Integer getPatientLimit() {
        return patientLimit;
    }

    public boolean isUnlimited() {
        return patientLimit == null || patientLimit < 0;
    }
}
