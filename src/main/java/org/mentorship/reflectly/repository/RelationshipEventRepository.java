package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.RelationshipEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipEventRepository extends JpaRepository<RelationshipEventEntity, String> {

    List<RelationshipEventEntity> findByPersonIdOrderByCreatedDateDesc(String personId);

    List<RelationshipEventEntity> findTop5ByPersonIdOrderByCreatedDateDesc(String personId);
}
