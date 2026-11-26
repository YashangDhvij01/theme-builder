package com.themebuilder.backend.dto;

import com.themebuilder.backend.entity.EmailCampaignHighlightTemplate;
import com.themebuilder.backend.entity.EmailCampaignSectionTemplateBase;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class EmailCampaignSectionTemplateResponse {

    private Long id;
    private String templateKey;
    private String name;
    private String category;
    private String subcategory;
    private String description;
    private String thumbnailUrl;
    private JsonNode templateData;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmailCampaignSectionTemplateResponse fromEntity(
            EmailCampaignSectionTemplateBase entity
    ) {
        EmailCampaignSectionTemplateResponse dto = new EmailCampaignSectionTemplateResponse();
        dto.setId(entity.getId());
        dto.setTemplateKey(entity.getTemplateKey());
        dto.setName(entity.getName());
        dto.setCategory(entity.getCategory());
        if (entity instanceof EmailCampaignHighlightTemplate hl) {
            dto.setSubcategory(hl.getSubcategory());
        }
        dto.setDescription(entity.getDescription());
        dto.setThumbnailUrl(entity.getThumbnailUrl());
        dto.setTemplateData(entity.getTemplateData());
        dto.setIsActive(entity.getIsActive());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public JsonNode getTemplateData() {
        return templateData;
    }

    public void setTemplateData(JsonNode templateData) {
        this.templateData = templateData;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
