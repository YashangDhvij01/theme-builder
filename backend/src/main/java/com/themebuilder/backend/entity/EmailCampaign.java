package com.themebuilder.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "email_campaign")
public class EmailCampaign extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(name = "preview_text", length = 500)
    private String previewText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sections_data", nullable = false, columnDefinition = "jsonb")
    private JsonNode sectionsData;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(length = 50)
    private String status = "DRAFT";

    @Column(name = "owner_scope", length = 100)
    private String ownerScope = "prototype_workspace";

    public EmailCampaign() {
    }

    public EmailCampaign(String name, String subject, String previewText, JsonNode sectionsData) {
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

    public String getOwnerScope() {
        return ownerScope;
    }

    public void setOwnerScope(String ownerScope) {
        this.ownerScope = ownerScope;
    }
}
