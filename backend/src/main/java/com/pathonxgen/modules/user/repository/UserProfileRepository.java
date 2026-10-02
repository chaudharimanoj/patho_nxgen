package com.pathonxgen.modules.user.repository;

import com.pathonxgen.modules.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByKeycloakUserId(String keycloakUserId);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    @Query("SELECT u FROM UserProfile u WHERE " +
           "(:query IS NULL OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.employeeCode) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:department IS NULL OR u.department = :department)")
    List<UserProfile> searchUsers(@Param("query") String query,
                                  @Param("status") String status,
                                  @Param("department") String department);
}
