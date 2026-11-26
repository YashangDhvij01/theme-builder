package com.themebuilder.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

public class EmailCampaignHeaderTemplateRequest {

    @NotBlank(message = "Template key is required")
    @Size(max = 100, message = "Template key cannot exceed 100 characters")
    private String templateKey;

    @NotBlank(message = "Header template name is required")
    @Size(max = 255, message = "Header template name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category = "header";

    private String description;

    private String thumbnailUrl;

    @NotNull(message = "Template data is required")
    private JsonNode templateData;

    private Boolean isActive = true;

    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder = 0;

    public EmailCampaignHeaderTemplateRequest() {
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

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
