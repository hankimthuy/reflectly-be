package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, String> {

    Page<ConversationEntity> findByUserIdOrderByStartedAtDesc(Long userId, Pageable pageable);

    Optional<ConversationEntity> findByIdAndUserId(String id, Long userId);

    long countByUserId(Long userId);

    /**
     * Sessions the user ended inside a window, oldest first — the conversation half of the
     * day-bucketed mood summary. Still-active sessions have a null endedAt and are excluded.
     */
    List<ConversationEntity> findByUserIdAndEndedAtBetweenOrderByEndedAtAsc(Long userId, Instant from, Instant to);
}
