package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.FrameworkType;
import org.mentorship.reflectly.model.SavedFrameworkEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedFrameworkEntryRepository extends JpaRepository<SavedFrameworkEntryEntity, String> {

    Page<SavedFrameworkEntryEntity> findByUserIdOrderByCreatedDateDesc(Long userId, Pageable pageable);

    Page<SavedFrameworkEntryEntity> findByUserIdAndFrameworkTypeOrderByCreatedDateDesc(
            Long userId, FrameworkType frameworkType, Pageable pageable);

    Optional<SavedFrameworkEntryEntity> findByIdAndUserId(String id, Long userId);
}
