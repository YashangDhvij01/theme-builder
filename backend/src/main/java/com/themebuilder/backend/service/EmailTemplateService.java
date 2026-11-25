package com.themebuilder.backend.service;

import com.themebuilder.backend.dto.EmailTemplateResponse;
import com.themebuilder.backend.entity.EmailTemplate;
import com.themebuilder.backend.exception.ResourceNotFoundException;
import com.themebuilder.backend.repository.EmailTemplateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmailTemplateService {

    private final EmailTemplateRepository templateRepository;

    public EmailTemplateService(EmailTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getAllActiveTemplates() {
        return getAllActiveTemplates(null, null);
    }

    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getTemplatesByCategory(String category) {
        return getAllActiveTemplates(category, null);
    }

    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getAllActiveTemplates(String category, String search) {
        String cleanCategory = normalizeParam(category);
        String cleanSearch = normalizeParam(search);

        List<EmailTemplate> templates;

        if (cleanCategory != null && cleanSearch != null) {
            templates = templateRepository.searchActiveTemplatesByCategory(cleanCategory, cleanSearch);
        } else if (cleanSearch != null) {
            templates = templateRepository.searchActiveTemplates(cleanSearch);
        } else if (cleanCategory != null) {
            templates = templateRepository.findByCategoryIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(cleanCategory);
        } else {
            templates = templateRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        }

        return templates.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<EmailTemplateResponse> getTemplatesPaginated(int page, int size, String category) {
        return getTemplatesPaginated(page, size, category, null);
    }

    @Transactional(readOnly = true)
    public Page<EmailTemplateResponse> getTemplatesPaginated(int page, int size, String category, String search) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100.");
        }

        Pageable pageable = PageRequest.of(page, size);
        String cleanCategory = normalizeParam(category);
        String cleanSearch = normalizeParam(search);

        Page<EmailTemplate> result;

        if (cleanCategory != null && cleanSearch != null) {
            result = templateRepository.searchActiveTemplatesByCategory(cleanCategory, cleanSearch, pageable);
        } else if (cleanSearch != null) {
            result = templateRepository.searchActiveTemplates(cleanSearch, pageable);
        } else if (cleanCategory != null) {
            result = templateRepository.findByCategoryIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(cleanCategory, pageable);
        } else {
            result = templateRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        }

        return result.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctCategories() {
        return templateRepository.findDistinctActiveCategories();
    }

    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplateById(Long id) {
        EmailTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found with ID: " + id));
        return mapToResponse(template);
    }

    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplateByKey(String templateKey) {
        EmailTemplate template = templateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found with key: " + templateKey));
        return mapToResponse(template);
    }

    private String normalizeParam(String param) {
        if (param == null || param.trim().isEmpty() || param.trim().equalsIgnoreCase("all")) {
            return null;
        }
        return param.trim();
    }

    private EmailTemplateResponse mapToResponse(EmailTemplate template) {
        EmailTemplateResponse response = new EmailTemplateResponse();
        response.setId(template.getId());
        response.setTemplateKey(template.getTemplateKey());
        response.setName(template.getName());
        response.setCategory(template.getCategory());
        response.setDescription(template.getDescription());
        response.setSectionsData(template.getSectionsData());
        response.setThumbnailUrl(template.getThumbnailUrl());
        response.setIsActive(template.getIsActive());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        return response;
    }
}
