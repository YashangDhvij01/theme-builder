package com.themebuilder.backend.config;

import com.themebuilder.backend.entity.EmailTemplate;
import com.themebuilder.backend.repository.EmailTemplateRepository;
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
@Order(20)
public class EmailTemplateDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateDataInitializer.class);
    private static final String TEMPLATES_FILE = "email-templates.json";

    private final EmailTemplateRepository templateRepository;
    private final JsonMapper jsonMapper;

    public EmailTemplateDataInitializer(EmailTemplateRepository templateRepository, JsonMapper jsonMapper) {
        this.templateRepository = templateRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource(TEMPLATES_FILE);
        if (!resource.exists()) {
            log.warn("{} not found in classpath. Skipping email template initialization.", TEMPLATES_FILE);
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode root = jsonMapper.readTree(inputStream);
            if (!root.isArray()) {
                log.warn("{} does not contain a JSON array. Skipping.", TEMPLATES_FILE);
                return;
            }

            int inserted = 0;
            int skipped = 0;

            LocalDateTime now = LocalDateTime.now();

            for (JsonNode item : root) {
                if (!item.has("templateKey")) {
                    continue;
                }
                String templateKey = item.get("templateKey").asText();

                // Idempotent safety check: skip if template key already exists
                if (templateRepository.existsByTemplateKey(templateKey)) {
                    skipped++;
                    continue;
                }

                String name = item.has("name") ? item.get("name").asText() : "Untitled Template";
                String category = item.has("category") ? item.get("category").asText() : "general";
                String description = item.has("description") ? item.get("description").asText() : "";
                String thumbnailUrl = item.has("thumbnailUrl") ? item.get("thumbnailUrl").asText() : null;
                JsonNode sectionsData = item.has("sectionsData") ? item.get("sectionsData") : null;

                EmailTemplate template = new EmailTemplate(templateKey, name, category, description, sectionsData);
                template.setThumbnailUrl(thumbnailUrl);
                template.setIsActive(true);
                template.setCreatedAt(now);
                template.setUpdatedAt(now);

                templateRepository.save(template);
                inserted++;
            }

            log.info("Email Template Initialization: {} inserted, {} skipped (already exist).", inserted, skipped);
        } catch (Exception e) {
            log.error("Failed to initialize email templates from {}.", TEMPLATES_FILE, e);
        }
    }
}
