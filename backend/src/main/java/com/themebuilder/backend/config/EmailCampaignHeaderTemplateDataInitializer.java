package com.themebuilder.backend.config;

import com.themebuilder.backend.entity.EmailCampaignHeaderTemplate;
import com.themebuilder.backend.repository.EmailCampaignHeaderTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.time.LocalDateTime;

@Component
@Order(25)
public class EmailCampaignHeaderTemplateDataInitializer implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(EmailCampaignHeaderTemplateDataInitializer.class);
    private static final String HEADER_TEMPLATES_FILE = "email-campaign-header-templates.json";

    private final EmailCampaignHeaderTemplateRepository headerTemplateRepository;
    private final JsonMapper jsonMapper;

    public EmailCampaignHeaderTemplateDataInitializer(
            EmailCampaignHeaderTemplateRepository headerTemplateRepository,
            JsonMapper jsonMapper
    ) {
        this.headerTemplateRepository = headerTemplateRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ClassPathResource resource = new ClassPathResource(HEADER_TEMPLATES_FILE);
        if (!resource.exists()) {
            log.warn("{} not found in classpath. Skipping header template initialization.", HEADER_TEMPLATES_FILE);
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode root = jsonMapper.readTree(inputStream);
            if (!root.isArray()) {
                log.warn("{} does not contain a JSON array. Skipping.", HEADER_TEMPLATES_FILE);
                return;
            }

            int inserted = 0;
            int skipped = 0;
            LocalDateTime now = LocalDateTime.now();

            for (int idx = 0; idx < root.size(); idx++) {
                JsonNode item = root.get(idx);
                if (!item.has("templateKey") || item.get("templateKey").asText().isBlank()) {
                    continue;
                }

                String templateKey = item.get("templateKey").asText().trim();
                if (headerTemplateRepository.existsByTemplateKey(templateKey)) {
                    skipped++;
                    continue;
                }

                String name = item.has("name") ? item.get("name").asText() : "Untitled Header";
                String category = item.has("category") ? item.get("category").asText() : "header";
                String description = item.has("description") && !item.get("description").isNull()
                        ? item.get("description").asText()
                        : null;
                String thumbnailUrl = item.has("thumbnailUrl") && !item.get("thumbnailUrl").isNull()
                        ? item.get("thumbnailUrl").asText()
                        : null;
                JsonNode templateData = item.get("templateData");
                int displayOrder = item.has("displayOrder") ? item.get("displayOrder").asInt(idx + 1) : (idx + 1);

                EmailCampaignHeaderTemplate entity = new EmailCampaignHeaderTemplate(
                        templateKey,
                        name,
                        category,
                        description,
                        templateData,
                        displayOrder
                );
                entity.setThumbnailUrl(thumbnailUrl);
                entity.setIsActive(true);
                entity.setCreatedAt(now);
                entity.setUpdatedAt(now);

                headerTemplateRepository.save(entity);
                inserted++;
            }

            log.info("Email Campaign Header Template Initialization: {} inserted, {} skipped (already exist).",
                    inserted, skipped);
        } catch (Exception e) {
            log.error("Failed to initialize email campaign header templates from {}.", HEADER_TEMPLATES_FILE, e);
        }
    }
}
