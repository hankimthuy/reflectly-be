package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.EnergyLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnergyLogRepository extends JpaRepository<EnergyLogEntity, String> {

    /**
     * Find all energy logs by user ID with pagination, most recent first.
     */
    Page<EnergyLogEntity> findByUserIdOrderByLoggedAtDesc(String userId, Pageable pageable);

    /**
     * Find energy logs by user ID and context tag with pagination, most recent first.
     */
    Page<EnergyLogEntity> findByUserIdAndContextTagOrderByLoggedAtDesc(String userId, String contextTag, Pageable pageable);

    /**
     * Find an energy log by ID and user ID (for security - users can only access their own logs).
     */
    Optional<EnergyLogEntity> findByIdAndUserId(String id, String userId);

    /**
     * Check if an energy log exists and belongs to a specific user.
     */
    boolean existsByIdAndUserId(String id, String userId);
}
