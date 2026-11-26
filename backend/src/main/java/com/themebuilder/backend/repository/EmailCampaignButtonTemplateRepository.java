package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.EmailCampaignButtonTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailCampaignButtonTemplateRepository
        extends JpaRepository<EmailCampaignButtonTemplate, Long> {

    Optional<EmailCampaignButtonTemplate> findByTemplateKeyAndIsActiveTrueAndIsDeletedFalse(
            String templateKey
    );

    @Query("""
            SELECT DISTINCT e.category
            FROM EmailCampaignButtonTemplate e
            WHERE e.isActive = true AND e.isDeleted = false
            ORDER BY e.category ASC
            """)
    List<String> findDistinctActiveCategories();

    @Query("""
            SELECT e FROM EmailCampaignButtonTemplate e
            WHERE e.isActive = true AND e.isDeleted = false
              AND (:category IS NULL OR :category = '' OR LOWER(:category) = 'all' OR LOWER(e.category) = LOWER(:category))
              AND (:search IS NULL OR :search = ''
                   OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(COALESCE(e.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(e.category) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY e.displayOrder ASC, e.id ASC
            """)
    Page<EmailCampaignButtonTemplate> searchActiveTemplates(
            @Param("category") String category,
            @Param("search") String search,
            Pageable pageable
    );
}
