package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.EmailCampaignHeaderTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailCampaignHeaderTemplateRepository
        extends JpaRepository<EmailCampaignHeaderTemplate, Long> {

    Optional<EmailCampaignHeaderTemplate> findByTemplateKey(String templateKey);

    boolean existsByTemplateKey(String templateKey);

    List<EmailCampaignHeaderTemplate> findByIsActiveTrueOrderByDisplayOrderAscIdAsc();

    List<EmailCampaignHeaderTemplate> findByCategoryIgnoreCaseAndIsActiveTrueOrderByDisplayOrderAscIdAsc(
            String category
    );
}
