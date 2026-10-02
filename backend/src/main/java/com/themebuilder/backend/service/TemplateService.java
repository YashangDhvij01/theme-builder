package com.themebuilder.backend.service;

import com.themebuilder.backend.entity.DesignElement;
import com.themebuilder.backend.entity.Template;
import com.themebuilder.backend.repository.TemplateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

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

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Template getTemplateById(Long id) {

        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    public Template updateTemplate(Long id, Template template) {

        Template existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        existingTemplate.setName(template.getName());
        existingTemplate.setDescription(template.getDescription());
        existingTemplate.setBackgroundColor(template.getBackgroundColor());

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

    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }
}