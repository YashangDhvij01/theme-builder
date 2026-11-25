package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.EmailTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByTemplateKey(String templateKey);

    boolean existsByTemplateKey(String templateKey);

    List<EmailTemplate> findByIsActiveTrueOrderByCreatedAtDesc();

    List<EmailTemplate> findByCategoryIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String category);

    Page<EmailTemplate> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<EmailTemplate> findByCategoryIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String category, Pageable pageable);

    @Query("SELECT t FROM EmailTemplate t WHERE t.isActive = true AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.category) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.createdAt DESC")
    List<EmailTemplate> searchActiveTemplates(@Param("search") String search);

    @Query("SELECT t FROM EmailTemplate t WHERE t.isActive = true AND " +
           "LOWER(t.category) = LOWER(:category) AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.createdAt DESC")
    List<EmailTemplate> searchActiveTemplatesByCategory(@Param("category") String category, @Param("search") String search);

    @Query("SELECT t FROM EmailTemplate t WHERE t.isActive = true AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.category) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<EmailTemplate> searchActiveTemplates(@Param("search") String search, Pageable pageable);

    @Query("SELECT t FROM EmailTemplate t WHERE t.isActive = true AND " +
           "LOWER(t.category) = LOWER(:category) AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<EmailTemplate> searchActiveTemplatesByCategory(@Param("category") String category, @Param("search") String search, Pageable pageable);

    @Query("SELECT DISTINCT t.category FROM EmailTemplate t WHERE t.isActive = true ORDER BY t.category ASC")
    List<String> findDistinctActiveCategories();
}
