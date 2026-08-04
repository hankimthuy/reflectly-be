package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.ActionProtocolEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActionProtocolRepository extends JpaRepository<ActionProtocolEntity, String> {

    /**
     * Find all action protocols by user ID with pagination, most recently created first.
     */
    Page<ActionProtocolEntity> findByUserIdOrderByCreatedDateDesc(String userId, Pageable pageable);

    /**
     * Find an action protocol by ID and user ID (for security - users can only access their own protocols).
     */
    Optional<ActionProtocolEntity> findByIdAndUserId(String id, String userId);

    /**
     * Check if an action protocol exists and belongs to a specific user.
     */
    boolean existsByIdAndUserId(String id, String userId);
}
