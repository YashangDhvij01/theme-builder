package com.themebuilder.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_campaign_highlight_templates")
public class EmailCampaignHighlightTemplate extends EmailCampaignSectionTemplateBase {

    @Column(length = 100)
    private String subcategory;

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }
}
