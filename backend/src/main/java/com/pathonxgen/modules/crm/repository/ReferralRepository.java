package com.pathonxgen.modules.crm.repository;

import com.pathonxgen.modules.crm.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReferralRepository extends JpaRepository<Referral, UUID> {
}
