package com.themebuilder.backend.service;

import com.themebuilder.backend.dto.EmailCampaignHeaderTemplateRequest;
import com.themebuilder.backend.dto.EmailCampaignHeaderTemplateResponse;
import com.themebuilder.backend.entity.EmailCampaignHeaderTemplate;
import com.themebuilder.backend.exception.ResourceNotFoundException;
import com.themebuilder.backend.repository.EmailCampaignHeaderTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailCampaignHeaderTemplateService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailCampaignHeaderTemplateService.class);

    private final EmailCampaignHeaderTemplateRepository headerTemplateRepository;

    public EmailCampaignHeaderTemplateService(
            EmailCampaignHeaderTemplateRepository headerTemplateRepository
    ) {
        this.headerTemplateRepository = headerTemplateRepository;
    }

    @Transactional(readOnly = true)
    public List<EmailCampaignHeaderTemplateResponse> getAllActiveHeaders(String category) {
        List<EmailCampaignHeaderTemplate> headers;
        if (category != null && !category.isBlank() && !category.trim().equalsIgnoreCase("all")) {
            headers = headerTemplateRepository
                    .findByCategoryIgnoreCaseAndIsActiveTrueOrderByDisplayOrderAscIdAsc(category.trim());
        } else {
            headers = headerTemplateRepository.findByIsActiveTrueOrderByDisplayOrderAscIdAsc();
        }
        return headers.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmailCampaignHeaderTemplateResponse getHeaderById(Long id) {
        EmailCampaignHeaderTemplate entity = headerTemplateRepository.findById(id)
                .filter(h -> Boolean.TRUE.equals(h.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Email campaign header template not found with ID: " + id
                ));
        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public EmailCampaignHeaderTemplateResponse getHeaderByKey(String templateKey) {
        EmailCampaignHeaderTemplate entity = headerTemplateRepository.findByTemplateKey(templateKey)
                .filter(h -> Boolean.TRUE.equals(h.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Email campaign header template not found with key: " + templateKey
                ));
        return mapToResponse(entity);
    }

    @Transactional
    public EmailCampaignHeaderTemplateResponse createHeader(EmailCampaignHeaderTemplateRequest request) {
        String key = request.getTemplateKey().trim();
        if (headerTemplateRepository.existsByTemplateKey(key)) {
            throw new IllegalArgumentException(
                    "Header template already exists with key: " + key
            );
        }

        LocalDateTime now = LocalDateTime.now();
        EmailCampaignHeaderTemplate entity = new EmailCampaignHeaderTemplate();
        entity.setTemplateKey(key);
        entity.setName(request.getName().trim());
        entity.setCategory(request.getCategory().trim());
        entity.setDescription(request.getDescription());
        entity.setThumbnailUrl(request.getThumbnailUrl());
        entity.setTemplateData(request.getTemplateData());
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        entity.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        EmailCampaignHeaderTemplate saved = headerTemplateRepository.save(entity);
        log.info("Created header template '{}' (ID: {})", saved.getTemplateKey(), saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public EmailCampaignHeaderTemplateResponse updateHeader(
            Long id,
            EmailCampaignHeaderTemplateRequest request
    ) {
        EmailCampaignHeaderTemplate entity = headerTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Email campaign header template not found with ID: " + id
                ));

        entity.setName(request.getName().trim());
        entity.setCategory(request.getCategory().trim());
        entity.setDescription(request.getDescription());
        entity.setThumbnailUrl(request.getThumbnailUrl());
        entity.setTemplateData(request.getTemplateData());
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            entity.setDisplayOrder(request.getDisplayOrder());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        EmailCampaignHeaderTemplate saved = headerTemplateRepository.save(entity);
        log.info("Updated header template '{}' (ID: {})", saved.getTemplateKey(), saved.getId());
        return mapToResponse(saved);
    }

    /**
     * Soft-deactivate a header template (sets is_active = false without deleting the row).
     */
    @Transactional
    public void deactivateHeader(Long id) {
        EmailCampaignHeaderTemplate entity = headerTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Email campaign header template not found with ID: " + id
                ));
        entity.setIsActive(false);
        entity.setUpdatedAt(LocalDateTime.now());
        headerTemplateRepository.save(entity);
        log.info("Soft-deactivated header template '{}' (ID: {})", entity.getTemplateKey(), id);
    }

    private EmailCampaignHeaderTemplateResponse mapToResponse(EmailCampaignHeaderTemplate entity) {
        EmailCampaignHeaderTemplateResponse response = new EmailCampaignHeaderTemplateResponse();
        response.setId(entity.getId());
        response.setTemplateKey(entity.getTemplateKey());
        response.setName(entity.getName());
        response.setCategory(entity.getCategory());
        response.setDescription(entity.getDescription());
        response.setThumbnailUrl(entity.getThumbnailUrl());
        response.setTemplateData(entity.getTemplateData());
        response.setIsActive(entity.getIsActive());
        response.setDisplayOrder(entity.getDisplayOrder());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
