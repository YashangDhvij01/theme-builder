package com.themebuilder.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

public class EmailCampaignRequest {

    @NotBlank(message = "Campaign name is required")
    @Size(max = 255, message = "Campaign name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Email subject is required")
    @Size(max = 255, message = "Subject cannot exceed 255 characters")
    private String subject;

    @Size(max = 500, message = "Preview text cannot exceed 500 characters")
    private String previewText;

    @NotNull(message = "Sections data is required")
    private JsonNode sectionsData;

    private String thumbnailUrl;

    private String status;

    public EmailCampaignRequest() {
    }

    public EmailCampaignRequest(String name, String subject, String previewText, JsonNode sectionsData) {
        this.name = name;
        this.subject = subject;
        this.previewText = previewText;
        this.sectionsData = sectionsData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPreviewText() {
        return previewText;
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }

    public JsonNode getSectionsData() {
        return sectionsData;
    }

    public void setSectionsData(JsonNode sectionsData) {
        this.sectionsData = sectionsData;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
