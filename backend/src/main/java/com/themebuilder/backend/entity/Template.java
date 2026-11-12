package com.themebuilder.backend.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(
        name = "template",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_template_template_key",
                        columnNames = "template_key"
                )
        }
)
public class Template extends BaseEntity {

    // Original frontend template ID / slug
    @Column(name = "template_key", length = 100)
    private String templateKey;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String backgroundColor;

    // Existing relationship preserved
    @OneToMany(
            mappedBy = "template",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DesignElement> elements = new ArrayList<>();

    // NEW: Complete template elements stored as PostgreSQL JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "elements_data", columnDefinition = "jsonb")
    private List<Map<String, Object>> elementsData = new ArrayList<>();

    // Template Key
    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    // Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Background Color
    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    // Existing DesignElement relationship
    public List<DesignElement> getElements() {
        return elements;
    }

    public void setElements(List<DesignElement> elements) {
        this.elements = elements;
    }

    // Complete JSONB elements
    public List<Map<String, Object>> getElementsData() {
        return elementsData;
    }

    public void setElementsData(
            List<Map<String, Object>> elementsData
    ) {
        this.elementsData = elementsData;
    }
}