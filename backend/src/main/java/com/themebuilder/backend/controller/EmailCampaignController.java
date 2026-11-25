package com.themebuilder.backend.controller;

import com.themebuilder.backend.dto.EmailCampaignRequest;
import com.themebuilder.backend.dto.EmailCampaignResponse;
import com.themebuilder.backend.service.EmailCampaignService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email-campaigns")
@CrossOrigin(origins = "http://localhost:5173")
public class EmailCampaignController {

    private final EmailCampaignService campaignService;

    public EmailCampaignController(EmailCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    public ResponseEntity<EmailCampaignResponse> createCampaign(
            @Valid @RequestBody EmailCampaignRequest request
    ) {
        EmailCampaignResponse created = campaignService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<EmailCampaignResponse>> getAllCampaigns() {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<EmailCampaignResponse>> getCampaignsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(campaignService.getCampaignsPaginated(page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailCampaignResponse> getCampaignById(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailCampaignResponse> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody EmailCampaignRequest request
    ) {
        return ResponseEntity.ok(campaignService.updateCampaign(id, request));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<EmailCampaignResponse> duplicateCampaign(@PathVariable Long id) {
        EmailCampaignResponse duplicated = campaignService.duplicateCampaign(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(duplicated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }
}
