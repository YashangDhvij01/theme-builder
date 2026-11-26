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
@RequestMapping("/api/email-campaign-highlight-templates")
public class EmailCampaignHighlightTemplateController {

    private final EmailCampaignSectionTemplateService templateService;

    public EmailCampaignHighlightTemplateController(
            EmailCampaignSectionTemplateService templateService
    ) {
        this.templateService = templateService;
    }

    @GetMapping("/page")
    public ResponseEntity<Page<EmailCampaignSectionTemplateResponse>> getPaginatedHighlights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                templateService.getHighlightsPaginated(page, size, category, search)
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getHighlightCategories() {
        return ResponseEntity.ok(templateService.getHighlightCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailCampaignSectionTemplateResponse> getHighlightById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(templateService.getHighlightById(id));
    }

    @GetMapping("/key/{templateKey}")
    public ResponseEntity<EmailCampaignSectionTemplateResponse> getHighlightByKey(
            @PathVariable String templateKey
    ) {
        return ResponseEntity.ok(templateService.getHighlightByKey(templateKey));
    }
}
