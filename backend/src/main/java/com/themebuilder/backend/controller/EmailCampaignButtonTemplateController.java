package com.themebuilder.backend.controller;

import com.themebuilder.backend.dto.EmailCampaignSectionTemplateResponse;
import com.themebuilder.backend.service.EmailCampaignSectionTemplateService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/email-campaign-button-templates")
public class EmailCampaignButtonTemplateController {

    private final EmailCampaignSectionTemplateService templateService;

    public EmailCampaignButtonTemplateController(
            EmailCampaignSectionTemplateService templateService
    ) {
        this.templateService = templateService;
    }

    @GetMapping("/page")
    public ResponseEntity<Page<EmailCampaignSectionTemplateResponse>> getPaginatedButtons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                templateService.getButtonsPaginated(page, size, category, search)
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getButtonCategories() {
        return ResponseEntity.ok(templateService.getButtonCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailCampaignSectionTemplateResponse> getButtonById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(templateService.getButtonById(id));
    }

    @GetMapping("/key/{templateKey}")
    public ResponseEntity<EmailCampaignSectionTemplateResponse> getButtonByKey(
            @PathVariable String templateKey
    ) {
        return ResponseEntity.ok(templateService.getButtonByKey(templateKey));
    }
}
