package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, String> {

    List<PersonEntity> findByUserIdOrderByLastMentionedAtDesc(Long userId);

    Optional<PersonEntity> findByIdAndUserId(String id, Long userId);

    Optional<PersonEntity> findByUserIdAndNameIgnoreCase(Long userId, String name);

    boolean existsByIdAndUserId(String id, Long userId);
}
