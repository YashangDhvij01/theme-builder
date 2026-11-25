package com.themebuilder.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(
        name = "email_template",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_template_key",
                        columnNames = "template_key"
                )
        }
)
public class EmailTemplate extends BaseEntity {

    @Column(name = "template_key", nullable = false, length = 100)
    private String templateKey;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sections_data", nullable = false, columnDefinition = "jsonb")
    private JsonNode sectionsData;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "owner_scope", length = 100)
    private String ownerScope = "prototype_workspace";

    public EmailTemplate() {
    }

    public EmailTemplate(String templateKey, String name, String category, String description, JsonNode sectionsData) {
        this.templateKey = templateKey;
        this.name = name;
        this.category = category;
        this.description = description;
        this.sectionsData = sectionsData;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getOwnerScope() {
        return ownerScope;
    }

    public void setOwnerScope(String ownerScope) {
        this.ownerScope = ownerScope;
    }
}
