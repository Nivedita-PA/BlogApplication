package com.mountblue.BlogApplication.repository;

import com.mountblue.BlogApplication.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findById(Long id);
    void deleteById(Long id);
    List<Post> findAll();
    Page<Post> findAll(Pageable pageable);
    Page<Post> findByAuthorIgnoreCase(String author, Pageable pageable);
    Page<Post> findByTags_NameAndIsPublishedTrue(String tagName, Pageable pageable);
    Page<Post> findByPublishedAtGreaterThanEqualAndPublishedAtLessThan(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN p.tags t " +
            "WHERE (:author IS NULL OR :author = '' OR LOWER(p.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
            "AND (:tag IS NULL OR :tag = '' OR t.name = :tag) " +
            "AND (:publishedDate IS NULL OR p.publishedAt >= :startOfDay AND p.publishedAt < :endOfDay) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Post> findByFilters(
            @Param("author") String author,
            @Param("tag") String tag,
            @Param("publishedDate") LocalDate publishedDate,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("search") String search,
            Pageable pageable
    );
    Page<Post> findAllByOrderByPublishedAtAsc(Pageable pageable);
    Page<Post> findAllByOrderByPublishedAtDesc(Pageable pageable);
    @Query("SELECT DISTINCT p.author FROM Post p WHERE p.author IS NOT NULL")
    Set<String> findAllAuthors();

    @Query("SELECT DISTINCT t.name FROM Tag t WHERE t.name IS NOT NULL")
    Set<String> findAllTags();

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN p.tags t " +
            "WHERE (:authors IS NULL OR p.author IN :authors) " +
            "AND (:tags IS NULL OR t.name IN :tags) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(COALESCE(p.title, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(COALESCE(p.content, '')) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Post> findByFilters(@Param("authors") Set<String> authors,
                             @Param("tags") Set<String> tags,
                             @Param("search") String search,
                             Pageable pageable);


}
