package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.ProtocolUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocolUsageRepository extends JpaRepository<ProtocolUsageEntity, String> {

    /**
     * Find all usage log entries for a protocol, most recent first.
     * Reserved for a future "effectiveness over time" view.
     */
    List<ProtocolUsageEntity> findByProtocolIdAndUserIdOrderByUsedAtDesc(String protocolId, String userId);
}
