package com.themebuilder.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_campaign_button_templates")
public class EmailCampaignButtonTemplate extends EmailCampaignSectionTemplateBase {
}
