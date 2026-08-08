package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.InsightEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsightRepository extends JpaRepository<InsightEntity, String> {

    Page<InsightEntity> findByUserIdOrderByCreatedDateDesc(Long userId, Pageable pageable);

    Page<InsightEntity> findByUserIdAndPersonIdOrderByCreatedDateDesc(Long userId, String personId, Pageable pageable);
}
