package com.themebuilder.backend.service;

import com.themebuilder.backend.entity.DesignElement;
import com.themebuilder.backend.entity.Template;
import com.themebuilder.backend.repository.TemplateRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    // 1. Create a new template
    @Transactional
    public Template createTemplate(Template template) {

        LocalDateTime now = LocalDateTime.now();

        template.setCreatedAt(now);
        template.setUpdatedAt(now);

        if (template.getElements() != null) {

            for (DesignElement element : template.getElements()) {
                element.setTemplate(template);
                element.setCreatedAt(now);
                element.setUpdatedAt(now);
            }
        }

        return templateRepository.save(template);
    }

    // 2. Fetch all templates
    // Existing method preserved
    @Transactional(readOnly = true)
    public List<Template> getAllTemplates() {

        return templateRepository.findAll();
    }

    // 3. NEW: Fetch templates with pagination
    @Transactional(readOnly = true)
    public Page<Template> getTemplatesPaginated(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100."
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        return templateRepository.findAllByOrderByIdAsc(pageable);
    }

    // 4. Fetch template using database ID
    @Transactional(readOnly = true)
    public Template getTemplateById(Long id) {

        return templateRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Template not found")
                );
    }

    // 5. NEW: Fetch template using original slug
    @Transactional(readOnly = true)
    public Template getTemplateByKey(String templateKey) {

        return templateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Template not found with key: " + templateKey
                        )
                );
    }

    // 6. Update an existing template
    @Transactional
    public Template updateTemplate(Long id, Template template) {

        Template existingTemplate = templateRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Template not found")
                );

        existingTemplate.setTemplateKey(template.getTemplateKey());
        existingTemplate.setName(template.getName());
        existingTemplate.setDescription(template.getDescription());
        existingTemplate.setBackgroundColor(template.getBackgroundColor());

        // Preserve complete JSONB template elements
        existingTemplate.setElementsData(template.getElementsData());

        existingTemplate.getElements().clear();

        LocalDateTime now = LocalDateTime.now();

        if (template.getElements() != null) {

            for (DesignElement element : template.getElements()) {

                element.setTemplate(existingTemplate);
                element.setCreatedAt(now);
                element.setUpdatedAt(now);

                existingTemplate.getElements().add(element);
            }
        }

        existingTemplate.setUpdatedAt(now);

        return templateRepository.save(existingTemplate);
    }

    // 7. Delete a template
    @Transactional
    public void deleteTemplate(Long id) {

        templateRepository.deleteById(id);
    }
}