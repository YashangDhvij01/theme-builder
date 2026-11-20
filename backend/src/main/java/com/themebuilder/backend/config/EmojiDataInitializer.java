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
@Order(15)
public class EmojiDataInitializer implements CommandLineRunner {

    private static final String EMOJI_ASSETS_FILE = "assets/emoji-assets.json";

    private final EditorAssetRepository editorAssetRepository;
    private final JsonMapper jsonMapper;

    public EmojiDataInitializer(
            EditorAssetRepository editorAssetRepository,
            JsonMapper jsonMapper
    ) {
        this.editorAssetRepository = editorAssetRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        loadEmojiAssets();
    }

    public int[] loadEmojiAssets() throws Exception {
        ClassPathResource resource = new ClassPathResource(EMOJI_ASSETS_FILE);

        if (!resource.exists()) {
            System.err.println(EMOJI_ASSETS_FILE + " not found in src/main/resources. Skipping emoji initialization.");
            return new int[]{0, 0, 0};
        }

        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Object> root = jsonMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, Object>>() {}
            );

            Object assetsObject = root.get("assets");

            if (!(assetsObject instanceof List<?> assets)) {
                throw new IllegalStateException("Invalid " + EMOJI_ASSETS_FILE + ": assets must be an array.");
            }

            // Fetch existing emojis to prevent duplicate assetKey entries and allow updates
            List<EditorAsset> existingList = editorAssetRepository
                    .findByAssetType("EMOJI");

            Map<String, EditorAsset> existingMap = existingList.stream()
                    .collect(Collectors.toMap(
                            EditorAsset::getAssetKey,
                            asset -> asset,
                            (first, second) -> first
                    ));

            List<EditorAsset> toSave = new ArrayList<>();
            Set<String> processedKeys = new HashSet<>();
            int inserted = 0;
            int updated = 0;
            int skipped = 0;

            for (Object item : assets) {
                if (!(item instanceof Map<?, ?> assetData)) {
                    skipped++;
                    continue;
                }

                String assetKey = String.valueOf(assetData.get("assetKey"));
                String assetType = "EMOJI";
                String name = String.valueOf(assetData.get("name"));
                String category = assetData.get("category") != null ? String.valueOf(assetData.get("category")) : "Smileys & Emotions";

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

                Object sortOrder = assetData.get("sortOrder");
                asset.setSortOrder(
                        sortOrder instanceof Number ? ((Number) sortOrder).intValue() : 0
                );
                asset.setIsActive(true);

                toSave.add(asset);
                processedKeys.add(assetKey);

                if (existing != null) {
                    updated++;
                } else {
                    inserted++;
                }
            }

            // Deactivate any legacy emojis not present in current emoji-assets.json
            int deactivated = 0;
            for (EditorAsset existing : existingList) {
                if (!processedKeys.contains(existing.getAssetKey()) && Boolean.TRUE.equals(existing.getIsActive())) {
                    existing.setIsActive(false);
                    toSave.add(existing);
                    deactivated++;
                }
            }

            if (!toSave.isEmpty()) {
                editorAssetRepository.saveAll(toSave);
            }

            System.out.println("-----------------------------------");
            System.out.println("EMOJI ASSET INITIALIZATION");
            System.out.println("Emoji assets processed: " + toSave.size());
            System.out.println("Emoji assets inserted:  " + inserted);
            System.out.println("Emoji assets updated:   " + updated);
            System.out.println("Emoji assets skipped:   " + skipped);
            System.out.println("-----------------------------------");

            return new int[]{inserted, updated, skipped};
        }
    }
}
