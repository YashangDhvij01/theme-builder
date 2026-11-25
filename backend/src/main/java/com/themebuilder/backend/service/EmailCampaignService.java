package com.themebuilder.backend.service;

import com.themebuilder.backend.dto.EmailCampaignRequest;
import com.themebuilder.backend.dto.EmailCampaignResponse;
import com.themebuilder.backend.entity.EmailCampaign;
import com.themebuilder.backend.exception.ResourceNotFoundException;
import com.themebuilder.backend.repository.EmailCampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailCampaignService {

    private static final Logger log = LoggerFactory.getLogger(EmailCampaignService.class);

    private final EmailCampaignRepository campaignRepository;

    public EmailCampaignService(EmailCampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Transactional
    public EmailCampaignResponse createCampaign(EmailCampaignRequest request) {
        LocalDateTime now = LocalDateTime.now();

        EmailCampaign campaign = new EmailCampaign();
        campaign.setName(request.getName().trim());
        campaign.setSubject(request.getSubject().trim());
        campaign.setPreviewText(request.getPreviewText() != null ? request.getPreviewText().trim() : "");
        campaign.setSectionsData(request.getSectionsData());
        campaign.setThumbnailUrl(request.getThumbnailUrl());
        campaign.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");
        campaign.setCreatedAt(now);
        campaign.setUpdatedAt(now);

        EmailCampaign saved = campaignRepository.save(campaign);
        log.info("Created new email campaign ID: '{}' ({})", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    @Transactional
    public EmailCampaignResponse updateCampaign(Long id, EmailCampaignRequest request) {
        EmailCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email campaign not found with ID: " + id));

        LocalDateTime now = LocalDateTime.now();
        campaign.setName(request.getName().trim());
        campaign.setSubject(request.getSubject().trim());
        if (request.getPreviewText() != null) {
            campaign.setPreviewText(request.getPreviewText().trim());
        }
        if (request.getSectionsData() != null) {
            campaign.setSectionsData(request.getSectionsData());
        }
        if (request.getThumbnailUrl() != null) {
            campaign.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getStatus() != null) {
            campaign.setStatus(request.getStatus());
        }
        campaign.setUpdatedAt(now);

        EmailCampaign saved = campaignRepository.save(campaign);
        log.info("Updated email campaign ID: '{}' ({})", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmailCampaignResponse getCampaignById(Long id) {
        EmailCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email campaign not found with ID: " + id));
        return mapToResponse(campaign);
    }

    @Transactional(readOnly = true)
    public List<EmailCampaignResponse> getAllCampaigns() {
        return campaignRepository.findAllByOrderByUpdatedAtDescIdDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<EmailCampaignResponse> getCampaignsPaginated(int page, int size, String search) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100.");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<EmailCampaign> pageResult;

        if (search != null && !search.isBlank()) {
            pageResult = campaignRepository.findByNameContainingIgnoreCaseOrderByUpdatedAtDescIdDesc(search.trim(), pageable);
        } else {
            pageResult = campaignRepository.findAllByOrderByUpdatedAtDescIdDesc(pageable);
        }

        return pageResult.map(this::mapToResponse);
    }

    @Transactional
    public EmailCampaignResponse duplicateCampaign(Long id) {
        EmailCampaign original = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email campaign not found with ID: " + id));

        LocalDateTime now = LocalDateTime.now();
        EmailCampaign copy = new EmailCampaign();
        copy.setName("Copy of " + original.getName());
        copy.setSubject(original.getSubject());
        copy.setPreviewText(original.getPreviewText());
        copy.setSectionsData(original.getSectionsData());
        copy.setThumbnailUrl(original.getThumbnailUrl());
        copy.setStatus("DRAFT");
        copy.setCreatedAt(now);
        copy.setUpdatedAt(now);

        EmailCampaign saved = campaignRepository.save(copy);
        log.info("Duplicated campaign ID {} into new campaign ID {}", id, saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteCampaign(Long id) {
        EmailCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email campaign not found with ID: " + id));

        campaignRepository.delete(campaign);
        log.info("Deleted email campaign ID: {}", id);
    }

    private EmailCampaignResponse mapToResponse(EmailCampaign campaign) {
        EmailCampaignResponse response = new EmailCampaignResponse();
        response.setId(campaign.getId());
        response.setName(campaign.getName());
        response.setSubject(campaign.getSubject());
        response.setPreviewText(campaign.getPreviewText());
        response.setSectionsData(campaign.getSectionsData());
        response.setThumbnailUrl(campaign.getThumbnailUrl());
        response.setStatus(campaign.getStatus());
        response.setOwnerScope(campaign.getOwnerScope());
        response.setCreatedAt(campaign.getCreatedAt());
        response.setUpdatedAt(campaign.getUpdatedAt());
        return response;
    }
}
