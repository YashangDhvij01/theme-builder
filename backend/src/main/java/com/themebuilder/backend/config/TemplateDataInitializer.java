package com.themebuilder.backend.config;

import com.themebuilder.backend.entity.Template;
import com.themebuilder.backend.repository.TemplateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class TemplateDataInitializer implements CommandLineRunner {

    private final TemplateRepository templateRepository;
    private final JsonMapper jsonMapper;

    public TemplateDataInitializer(
            TemplateRepository templateRepository,
            JsonMapper jsonMapper
    ) {
        this.templateRepository = templateRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        ClassPathResource resource =
                new ClassPathResource("templates.json");

        if (!resource.exists()) {
            System.out.println("templates.json file not found.");
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {

            List<Map<String, Object>> templates =
                    jsonMapper.readValue(
                            inputStream,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );

            int inserted = 0;
            int skipped = 0;

            for (Map<String, Object> data : templates) {

                String templateKey =
                        String.valueOf(data.get("templateKey"));

                if (templateRepository.existsByTemplateKey(templateKey)) {
                    skipped++;
                    continue;
                }

                Template template = new Template();

                template.setTemplateKey(templateKey);
                template.setName((String) data.get("name"));
                template.setDescription((String) data.get("description"));
                template.setBackgroundColor(
                        (String) data.get("backgroundColor")
                );

                Object elements = data.get("elementsData");

                if (elements instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> elementsData =
                            (List<Map<String, Object>>) elements;

                    template.setElementsData(elementsData);
                }

                templateRepository.save(template);
                inserted++;
            }

            System.out.println("==================================");
            System.out.println("Template initialization completed");
            System.out.println("Inserted templates: " + inserted);
            System.out.println("Skipped existing templates: " + skipped);
            System.out.println("==================================");
        }
    }
}