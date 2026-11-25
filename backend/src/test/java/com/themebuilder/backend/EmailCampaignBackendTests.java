package com.themebuilder.backend;

import com.themebuilder.backend.dto.EmailCampaignRequest;
import com.themebuilder.backend.dto.EmailCampaignResponse;
import com.themebuilder.backend.dto.EmailTemplateResponse;
import com.themebuilder.backend.service.EmailCampaignService;
import com.themebuilder.backend.service.EmailTemplateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmailCampaignBackendTests {

    @Autowired
    private EmailCampaignService campaignService;

    @Autowired
    private EmailTemplateService templateService;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("Should create and retrieve an email campaign successfully in test isolation")
    void testCreateAndReadCampaign() throws Exception {
        JsonNode sections = jsonMapper.readTree("[{\"id\":\"sec-1\",\"type\":\"header\",\"content\":{\"title\":\"Welcome!\"}}]");
        EmailCampaignRequest req = new EmailCampaignRequest("Spring Launch", "Welcome to Spring 2026", "Big news inside", sections);

        EmailCampaignResponse created = campaignService.createCampaign(req);
        Assertions.assertNotNull(created.getId(), "Created campaign must have an ID");
        Assertions.assertEquals("Spring Launch", created.getName());
        Assertions.assertEquals("Welcome to Spring 2026", created.getSubject());
        Assertions.assertEquals("DRAFT", created.getStatus());

        EmailCampaignResponse fetched = campaignService.getCampaignById(created.getId());
        Assertions.assertEquals(created.getId(), fetched.getId());
        Assertions.assertEquals("Spring Launch", fetched.getName());
        Assertions.assertNotNull(fetched.getSectionsData());
        Assertions.assertTrue(fetched.getSectionsData().isArray());
    }

    @Test
    @DisplayName("Should update an existing email campaign")
    void testUpdateCampaign() throws Exception {
        JsonNode sections = jsonMapper.readTree("[{\"id\":\"sec-1\",\"type\":\"text\",\"content\":{\"body\":\"Initial body\"}}]");
        EmailCampaignRequest req = new EmailCampaignRequest("Initial Title", "Initial Subject", "Preview", sections);
        EmailCampaignResponse created = campaignService.createCampaign(req);

        JsonNode updatedSections = jsonMapper.readTree("[{\"id\":\"sec-1\",\"type\":\"text\",\"content\":{\"body\":\"Updated body\"}}]");
        EmailCampaignRequest updateReq = new EmailCampaignRequest("Updated Title", "Updated Subject", "New Preview", updatedSections);
        updateReq.setStatus("READY");

        EmailCampaignResponse updated = campaignService.updateCampaign(created.getId(), updateReq);
        Assertions.assertEquals("Updated Title", updated.getName());
        Assertions.assertEquals("Updated Subject", updated.getSubject());
        Assertions.assertEquals("New Preview", updated.getPreviewText());
        Assertions.assertEquals("READY", updated.getStatus());
    }

    @Test
    @DisplayName("Should duplicate an email campaign with Copy prefix and DRAFT status")
    void testDuplicateCampaign() throws Exception {
        JsonNode sections = jsonMapper.readTree("[{\"id\":\"sec-1\",\"type\":\"banner\",\"content\":{\"url\":\"https://example.com/banner.png\"}}]");
        EmailCampaignRequest req = new EmailCampaignRequest("Black Friday", "50% off everything", "Deals await", sections);
        EmailCampaignResponse original = campaignService.createCampaign(req);

        EmailCampaignResponse duplicate = campaignService.duplicateCampaign(original.getId());
        Assertions.assertNotEquals(original.getId(), duplicate.getId(), "Duplicate must have a unique ID");
        Assertions.assertEquals("Copy of Black Friday", duplicate.getName());
        Assertions.assertEquals(original.getSubject(), duplicate.getSubject());
        Assertions.assertEquals("DRAFT", duplicate.getStatus());
        Assertions.assertEquals(original.getSectionsData().toString(), duplicate.getSectionsData().toString());
    }

    @Test
    @DisplayName("Should list all campaigns and support pagination")
    void testListAndPagination() throws Exception {
        JsonNode dummySections = jsonMapper.readTree("[]");
        campaignService.createCampaign(new EmailCampaignRequest("Product Update A", "Subject A", "Prev A", dummySections));
        campaignService.createCampaign(new EmailCampaignRequest("Newsletter Weekly", "Subject B", "Prev B", dummySections));
        campaignService.createCampaign(new EmailCampaignRequest("Product Update B", "Subject C", "Prev C", dummySections));

        List<EmailCampaignResponse> all = campaignService.getAllCampaigns();
        Assertions.assertTrue(all.size() >= 3);

        Page<EmailCampaignResponse> searchResult = campaignService.getCampaignsPaginated(0, 10, "Product");
        Assertions.assertEquals(2, searchResult.getTotalElements(), "Search for 'Product' should match 2 campaigns");
    }

    @Test
    @DisplayName("Should fetch template categories and query active templates")
    void testTemplateQueries() {
        List<String> categories = templateService.getDistinctCategories();
        Assertions.assertNotNull(categories);

        List<EmailTemplateResponse> active = templateService.getAllActiveTemplates();
        Assertions.assertNotNull(active);
    }
}
