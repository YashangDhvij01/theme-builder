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
@RequestMapping("/api/email-campaign-footer-templates")
public class EmailCampaignFooterTemplateController {

    private final EmailCampaignSectionTemplateService templateService;

    public EmailCampaignFooterTemplateController(
            EmailCampaignSectionTemplateService templateService
    ) {
        this.templateService = templateService;
    }

    @GetMapping("/page")
    public ResponseEntity<Page<EmailCampaignSectionTemplateResponse>> getPaginatedFooters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                templateService.getFootersPaginated(page, size, category, search)
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getFooterCategories() {
        return ResponseEntity.ok(templateService.getFooterCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailCampaignSectionTemplateResponse> getFooterById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(templateService.getFooterById(id));
    }

    @GetMapping("/key/{templateKey}")
    public ResponseEntity<EmailCampaignSectionTemplateResponse> getFooterByKey(
            @PathVariable String templateKey
    ) {
        return ResponseEntity.ok(templateService.getFooterByKey(templateKey));
    }
}
