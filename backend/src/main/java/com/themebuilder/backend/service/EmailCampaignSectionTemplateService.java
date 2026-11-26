package com.themebuilder.backend.service;

import com.themebuilder.backend.dto.EmailCampaignSectionTemplateResponse;
import com.themebuilder.backend.entity.EmailCampaignSectionTemplateBase;
import com.themebuilder.backend.exception.ResourceNotFoundException;
import com.themebuilder.backend.repository.EmailCampaignButtonTemplateRepository;
import com.themebuilder.backend.repository.EmailCampaignFooterTemplateRepository;
import com.themebuilder.backend.repository.EmailCampaignHighlightTemplateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmailCampaignSectionTemplateService {

    private final EmailCampaignFooterTemplateRepository footerRepo;
    private final EmailCampaignButtonTemplateRepository buttonRepo;
    private final EmailCampaignHighlightTemplateRepository highlightRepo;

    public EmailCampaignSectionTemplateService(
            EmailCampaignFooterTemplateRepository footerRepo,
            EmailCampaignButtonTemplateRepository buttonRepo,
            EmailCampaignHighlightTemplateRepository highlightRepo
    ) {
        this.footerRepo = footerRepo;
        this.buttonRepo = buttonRepo;
        this.highlightRepo = highlightRepo;
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));
        return PageRequest.of(safePage, safeSize);
    }

    private String normalizeFilter(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private EmailCampaignSectionTemplateResponse requireActive(
            Optional<? extends EmailCampaignSectionTemplateBase> entity,
            String label,
            Object identifier
    ) {
        return entity
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()) && !Boolean.TRUE.equals(e.getIsDeleted()))
                .map(EmailCampaignSectionTemplateResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException(
                        label + " not found: " + identifier
                ));
    }

    // --- Footer Templates ---

    @Transactional(readOnly = true)
    public Page<EmailCampaignSectionTemplateResponse> getFootersPaginated(
            int page, int size, String category, String search
    ) {
        return footerRepo.searchActiveTemplates(
                normalizeFilter(category), normalizeFilter(search), buildPageable(page, size)
        ).map(EmailCampaignSectionTemplateResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<String> getFooterCategories() {
        return footerRepo.findDistinctActiveCategories();
    }

    @Transactional(readOnly = true)
    public EmailCampaignSectionTemplateResponse getFooterById(Long id) {
        return requireActive(footerRepo.findById(id), "Footer template", id);
    }

    @Transactional(readOnly = true)
    public EmailCampaignSectionTemplateResponse getFooterByKey(String key) {
        return requireActive(
                footerRepo.findByTemplateKeyAndIsActiveTrueAndIsDeletedFalse(key),
                "Footer template",
                key
        );
    }

    // --- Button Templates ---

    @Transactional(readOnly = true)
    public Page<EmailCampaignSectionTemplateResponse> getButtonsPaginated(
            int page, int size, String category, String search
    ) {
        return buttonRepo.searchActiveTemplates(
                normalizeFilter(category), normalizeFilter(search), buildPageable(page, size)
        ).map(EmailCampaignSectionTemplateResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<String> getButtonCategories() {
        return buttonRepo.findDistinctActiveCategories();
    }

    @Transactional(readOnly = true)
    public EmailCampaignSectionTemplateResponse getButtonById(Long id) {
        return requireActive(buttonRepo.findById(id), "Button template", id);
    }

    @Transactional(readOnly = true)
    public EmailCampaignSectionTemplateResponse getButtonByKey(String key) {
        return requireActive(
                buttonRepo.findByTemplateKeyAndIsActiveTrueAndIsDeletedFalse(key),
                "Button template",
                key
        );
    }

    // --- Highlight Templates ---

    @Transactional(readOnly = true)
    public Page<EmailCampaignSectionTemplateResponse> getHighlightsPaginated(
            int page, int size, String category, String search
    ) {
        return highlightRepo.searchActiveTemplates(
                normalizeFilter(category), normalizeFilter(search), buildPageable(page, size)
        ).map(EmailCampaignSectionTemplateResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<String> getHighlightCategories() {
        return highlightRepo.findDistinctActiveCategories();
    }

    @Transactional(readOnly = true)
    public EmailCampaignSectionTemplateResponse getHighlightById(Long id) {
        return requireActive(highlightRepo.findById(id), "Highlight template", id);
    }

    @Transactional(readOnly = true)
    public EmailCampaignSectionTemplateResponse getHighlightByKey(String key) {
        return requireActive(
                highlightRepo.findByTemplateKeyAndIsActiveTrueAndIsDeletedFalse(key),
                "Highlight template",
                key
        );
    }
}
