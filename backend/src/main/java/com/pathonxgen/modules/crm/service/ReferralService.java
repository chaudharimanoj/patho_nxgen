package com.pathonxgen.modules.crm.service;

import com.pathonxgen.modules.crm.entity.Referral;
import com.pathonxgen.modules.crm.repository.ReferralRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReferralService {

    private final ReferralRepository referralRepository;

    public ReferralService(ReferralRepository referralRepository) {
        this.referralRepository = referralRepository;
    }

    public Referral recordReferral(UUID doctorId, UUID patientId, String source) {
        return referralRepository.save(new Referral(doctorId, patientId, source));
    }
}
