package com.themebuilder.backend.config;

import com.themebuilder.backend.entity.EditorAsset;
import com.themebuilder.backend.repository.EditorAssetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Order(10)
public class GraphicDataInitializer implements CommandLineRunner {

    private static final String GRAPHICS_ASSETS_FILE = "assets/graphics-assets.json";

    private final EditorAssetRepository editorAssetRepository;
    private final JsonMapper jsonMapper;

    public GraphicDataInitializer(
            EditorAssetRepository editorAssetRepository,
            JsonMapper jsonMapper
    ) {
        this.editorAssetRepository = editorAssetRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        loadGraphicAssets();
    }

    public int[] loadGraphicAssets() throws Exception {
        ClassPathResource resource = new ClassPathResource(GRAPHICS_ASSETS_FILE);

        if (!resource.exists()) {
            System.err.println(GRAPHICS_ASSETS_FILE + " not found in src/main/resources. Skipping graphics initialization.");
            return new int[]{0, 0, 0};
        }

        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Object> root = jsonMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, Object>>() {}
            );

            Object assetsObject = root.get("assets");

            if (!(assetsObject instanceof List<?> assets)) {
                throw new IllegalStateException("Invalid " + GRAPHICS_ASSETS_FILE + ": assets must be an array.");
            }

            // Fetch existing graphics (including inactive ones) to prevent duplicate assetKey entries
            List<EditorAsset> existingList = editorAssetRepository
                    .findByAssetType("GRAPHIC");

            Map<String, EditorAsset> existingMap = existingList.stream()
                    .collect(Collectors.toMap(
                            EditorAsset::getAssetKey,
                            asset -> asset,
                            (first, second) -> first
                    ));

            List<EditorAsset> toSave = new ArrayList<>();
            int inserted = 0;
            int updated = 0;
            int skipped = 0;

            for (Object item : assets) {
                if (!(item instanceof Map<?, ?> assetData)) {
                    skipped++;
                    continue;
                }

                String assetKey = String.valueOf(assetData.get("assetKey"));
                String assetType = "GRAPHIC";
                String name = String.valueOf(assetData.get("name"));
                String category = assetData.get("category") != null ? String.valueOf(assetData.get("category")) : null;

                Object jsonData = assetData.get("assetData");
                if (!(jsonData instanceof Map<?, ?>)) {
                    skipped++;
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> typedAssetData = (Map<String, Object>) jsonData;

                EditorAsset existing = existingMap.get(assetKey);
                EditorAsset asset = existing != null ? existing : new EditorAsset();

                asset.setAssetKey(assetKey);
                asset.setAssetType(assetType);
                asset.setName(name);
                asset.setCategory(category);
                asset.setAssetData(typedAssetData);

                Object sortOrderObj = assetData.get("sortOrder");
                int sortOrder = (sortOrderObj instanceof Number number) ? number.intValue() : 0;
                asset.setSortOrder(sortOrder);
                asset.setIsActive(true);

                toSave.add(asset);

                if (existing != null) {
                    updated++;
                } else {
                    inserted++;
                }
            }

            if (!toSave.isEmpty()) {
                editorAssetRepository.saveAll(toSave);
            }

            System.out.println("-----------------------------------");
            System.out.println("GRAPHIC ASSETS INITIALIZATION");
            System.out.println("Graphics inserted: " + inserted);
            System.out.println("Graphics updated: " + updated);
            System.out.println("Total graphics processed: " + toSave.size());
            System.out.println("-----------------------------------");

            return new int[]{inserted, updated, skipped};
        }
    }
}
