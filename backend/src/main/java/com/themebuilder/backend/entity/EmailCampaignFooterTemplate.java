package com.themebuilder.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_campaign_footer_templates")
public class EmailCampaignFooterTemplate extends EmailCampaignSectionTemplateBase {
}
