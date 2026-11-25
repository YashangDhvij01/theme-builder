package com.themebuilder.backend.controller;

import com.themebuilder.backend.dto.EmailCampaignHeaderTemplateResponse;
import com.themebuilder.backend.service.EmailCampaignHeaderTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public read-only endpoints for Email Campaign Header Templates.
 * CORS is handled centrally by CorsConfig.
 * Administrative write endpoints (POST/PUT/DELETE) are intentionally omitted
 * because the application does not yet configure Spring Security / admin roles.
 */
@RestController
@RequestMapping("/api/email-campaign-header-templates")
public class EmailCampaignHeaderTemplateController {

    private final EmailCampaignHeaderTemplateService headerTemplateService;

    public EmailCampaignHeaderTemplateController(
            EmailCampaignHeaderTemplateService headerTemplateService
    ) {
        this.headerTemplateService = headerTemplateService;
    }

    @GetMapping
    public ResponseEntity<List<EmailCampaignHeaderTemplateResponse>> getAllActiveHeaders(
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(headerTemplateService.getAllActiveHeaders(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailCampaignHeaderTemplateResponse> getHeaderById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(headerTemplateService.getHeaderById(id));
    }

    @GetMapping("/key/{templateKey}")
    public ResponseEntity<EmailCampaignHeaderTemplateResponse> getHeaderByKey(
            @PathVariable String templateKey
    ) {
        return ResponseEntity.ok(headerTemplateService.getHeaderByKey(templateKey));
    }
}
