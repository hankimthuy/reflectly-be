package org.mentorship.reflectly.repository;

import org.mentorship.reflectly.model.EntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntryRepository extends JpaRepository<EntryEntity, String> {

    /**
     * Find all entries by user ID.
     * @param userId The user ID to search for.
     * @return List of entries for the user.
     */
    List<EntryEntity> findByUserIdOrderByCreatedDateDesc(String userId);

    /**
     * Find all entries by user ID with pagination.
     * @param userId The user ID to search for.
     * @param pageable Pagination information.
     * @return Page of entries for the user.
     */
    Page<EntryEntity> findByUserIdOrderByCreatedDateDesc(String userId, Pageable pageable);

    /**
     * Find an entry by ID and user ID (for security - users can only access their own entries).
     * @param id The entry ID.
     * @param userId The user ID.
     * @return Optional containing the entry if found and belongs to the user.
     */
    Optional<EntryEntity> findByIdAndUserId(String id, String userId);

    /**
     * Find entries by user ID within a date range.
     * @param userId The user ID.
     * @param startDate The start date (inclusive).
     * @param endDate The end date (inclusive).
     * @return List of entries within the date range.
     */
    @Query("SELECT e FROM EntryEntity e WHERE e.userId = :userId AND e.createdDate >= :startDate AND e.createdDate <= :endDate ORDER BY e.createdDate DESC")
    List<EntryEntity> findByUserIdAndCreatedAtBetween(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find entries by user ID within a date range with pagination.
     * @param userId The user ID.
     * @param startDate The start date (inclusive).
     * @param endDate The end date (inclusive).
     * @param pageable Pagination information.
     * @return Page of entries within the date range.
     */
    @Query("SELECT e FROM EntryEntity e WHERE e.userId = :userId AND e.createdDate >= :startDate AND e.createdDate <= :endDate ORDER BY e.createdDate DESC")
    Page<EntryEntity> findByUserIdAndCreatedAtBetween(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Find entries by user ID and specific emotion.
     * @param userId The user ID.
     * @param emotion The emotion to search for.
     * @return List of entries containing the specified emotion.
     */
    @Query("SELECT e FROM EntryEntity e JOIN e.emotions em WHERE e.userId = :userId AND em = :emotion ORDER BY e.createdDate DESC")
    List<EntryEntity> findByUserIdAndEmotionsContaining(
            @Param("userId") String userId,
            @Param("emotion") String emotion
    );

    /**
     * Find entries by user ID and specific emotion with pagination.
     * @param userId The user ID.
     * @param emotion The emotion to search for.
     * @param pageable Pagination information.
     * @return Page of entries containing the specified emotion.
     */
    @Query("SELECT e FROM EntryEntity e JOIN e.emotions em WHERE e.userId = :userId AND em = :emotion ORDER BY e.createdDate DESC")
    Page<EntryEntity> findByUserIdAndEmotionsContaining(
            @Param("userId") String userId,
            @Param("emotion") String emotion,
            Pageable pageable
    );

    /**
     * Check if an entry exists and belongs to a specific user.
     * @param id The entry ID.
     * @param userId The user ID.
     * @return True if the entry exists and belongs to the user.
     */
    boolean existsByIdAndUserId(String id, String userId);

    /**
     * Count entries by user ID.
     * @param userId The user ID.
     * @return Number of entries for the user.
     */
    long countByUserId(String userId);

    /**
     * Just the creation timestamps of a user's entries — a slim projection for the day-streak
     * calculation, which only needs to know which calendar days have an entry and would
     * otherwise pull every reflection body into memory.
     * @param userId The user ID.
     * @return Creation timestamps, unordered.
     */
    @Query("SELECT e.createdDate FROM EntryEntity e WHERE e.userId = :userId")
    List<Instant> findCreatedDatesByUserId(@Param("userId") String userId);

    /**
     * Tag counts per emotion over a window, aggregated in the database rather than by loading
     * entries. Only emotions actually tagged in the window come back — callers zero-fill the
     * rest of the catalog.
     * @param userId The user ID.
     * @param from Window start (inclusive).
     * @param to Window end (inclusive).
     * @return Rows of {@code [emotion (String), count (Long)]}.
     */
    @Query("SELECT em, COUNT(em) FROM EntryEntity e JOIN e.emotions em "
            + "WHERE e.userId = :userId AND e.createdDate BETWEEN :from AND :to GROUP BY em")
    List<Object[]> countEmotionsByUserIdAndCreatedDateBetween(
            @Param("userId") String userId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    /**
     * Entries created inside a window, oldest first — the entry half of the day-bucketed mood
     * summary.
     * @param userId The user ID.
     * @param from Window start (inclusive).
     * @param to Window end (inclusive).
     * @return Entries within the window.
     */
    List<EntryEntity> findByUserIdAndCreatedDateBetweenOrderByCreatedDateAsc(
            String userId, Instant from, Instant to);
}
