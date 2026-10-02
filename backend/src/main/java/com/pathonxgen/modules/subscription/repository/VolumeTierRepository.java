package com.pathonxgen.modules.subscription.repository;

import com.pathonxgen.modules.subscription.entity.VolumeTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VolumeTierRepository extends JpaRepository<VolumeTier, UUID> {
    Optional<VolumeTier> findByCode(String code);
    List<VolumeTier> findAllByOrderByDisplayOrderAsc();
}
