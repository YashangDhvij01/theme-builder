package com.themebuilder.backend.controller;

import com.themebuilder.backend.entity.Template;
import com.themebuilder.backend.service.TemplateService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    // 1. Create a new template
    @PostMapping
    public Template createTemplate(@RequestBody Template template) {

        return templateService.createTemplate(template);
    }

    // 2. Get all templates
    // Existing endpoint preserved
    @GetMapping
    public List<Template> getAllTemplates() {

        return templateService.getAllTemplates();
    }

    // 3. NEW: Get templates with pagination
    // URL: /api/templates/page?page=0&size=20
    @GetMapping("/page")
    public Page<Template> getPaginatedTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return templateService.getTemplatesPaginated(page, size);
    }

    // 4. Get template by original slug
    // Example: /api/templates/key/classic-gold
    @GetMapping("/key/{templateKey}")
    public Template getTemplateByKey(
            @PathVariable String templateKey
    ) {

        return templateService.getTemplateByKey(templateKey);
    }

    // 5. Get template by database ID
    @GetMapping("/{id}")
    public Template getTemplateById(@PathVariable Long id) {

        return templateService.getTemplateById(id);
    }

    // 6. Update an existing template
    @PutMapping("/{id}")
    public Template updateTemplate(
            @PathVariable Long id,
            @RequestBody Template template
    ) {

        return templateService.updateTemplate(id, template);
    }

    // 7. Delete a template
    @DeleteMapping("/{id}")
    public void deleteTemplate(@PathVariable Long id) {

        templateService.deleteTemplate(id);
    }
}