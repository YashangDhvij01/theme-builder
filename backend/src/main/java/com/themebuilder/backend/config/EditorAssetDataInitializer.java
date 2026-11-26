
package com.themebuilder.backend.config;

import com.themebuilder.backend.entity.EditorAsset;
import com.themebuilder.backend.repository.EditorAssetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.legacy-asset-init.enabled", havingValue = "true", matchIfMissing = false)
public class EditorAssetDataInitializer implements CommandLineRunner {

    private static final String TEXT_ASSETS_FILE = "assets/text-assets.json";
    private static final String SHAPE_ASSETS_FILE = "assets/shape-assets.json";
    private static final String BORDER_ASSETS_FILE = "assets/border-assets.json";
    private static final String ANIMATION_ASSETS_FILE = "assets/animation-assets.json";

    private final EditorAssetRepository editorAssetRepository;
    private final JsonMapper jsonMapper;

    public EditorAssetDataInitializer(
            EditorAssetRepository editorAssetRepository,
            JsonMapper jsonMapper
    ) {
        this.editorAssetRepository = editorAssetRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        int[] textCounts = loadAssets(TEXT_ASSETS_FILE, "TEXT", false);
        int[] shapeCounts = loadAssets(SHAPE_ASSETS_FILE, "SHAPE", false);
        int[] borderCounts = loadAssets(BORDER_ASSETS_FILE, "BORDER", true);
        int[] animationCounts = loadAssets(ANIMATION_ASSETS_FILE, "ANIMATION", true);

        System.out.println("-----------------------------------");
        System.out.println("EDITOR ASSET INITIALIZATION");

        printCounts("Text", textCounts);
        printCounts("Shape", shapeCounts);
        printCounts("Border", borderCounts);
        printCounts("Animation", animationCounts);

        System.out.println("Total inserted: " +
                (textCounts[0] + shapeCounts[0] + borderCounts[0] + animationCounts[0]));

        System.out.println("Total updated: " +
                (borderCounts[1] + animationCounts[1]));

        System.out.println("Total skipped: " +
                (textCounts[1] + shapeCounts[1] + borderCounts[2] + animationCounts[2]));

        System.out.println("-----------------------------------");
    }

    private void printCounts(String label, int[] counts) {

        System.out.println(label + " assets inserted: " + counts[0]);

        if (counts.length == 3) {
            System.out.println(label + " assets updated: " + counts[1]);
            System.out.println(label + " assets skipped: " + counts[2]);
        } else {
            System.out.println(label + " assets skipped: " + counts[1]);
        }
    }

    /**
     * TEXT and SHAPE keep their existing insert-only behavior.
     * BORDER and ANIMATION are updated when matching records already exist.
     */
    private int[] loadAssets(
            String filePath,
            String expectedAssetType,
            boolean updateExisting
    ) throws Exception {

        ClassPathResource resource = new ClassPathResource(filePath);

        if (!resource.exists()) {
            throw new IllegalStateException(
                    filePath + " file not found in src/main/resources."
            );
        }

        try (InputStream inputStream = resource.getInputStream()) {

            Map<String, Object> root = jsonMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, Object>>() {}
            );

            Object assetsObject = root.get("assets");

            if (!(assetsObject instanceof List<?> assets)) {
                throw new IllegalStateException(
                        "Invalid " + filePath + ": assets must be an array."
                );
            }

            int inserted = 0;
            int updated = 0;
            int skipped = 0;

            for (Object item : assets) {

                if (!(item instanceof Map<?, ?> assetData)) {
                    throw new IllegalStateException(
                            "Invalid asset record in " + filePath + "."
                    );
                }

                String assetKey = requiredString(assetData, "assetKey", filePath);

                String assetType = requiredString(assetData, "assetType", filePath)
                        .toUpperCase();

                if (!expectedAssetType.equals(assetType)) {
                    throw new IllegalStateException(
                            "Asset type mismatch in " + filePath
                                    + " for asset: " + assetKey
                                    + ". Expected " + expectedAssetType
                                    + " but found " + assetType + "."
                    );
                }

                Optional<EditorAsset> existing =
                        editorAssetRepository.findByAssetTypeAndAssetKey(
                                assetType,
                                assetKey
                        );

                if (existing.isPresent() && !updateExisting) {
                    skipped++;
                    continue;
                }

                EditorAsset asset = existing.orElseGet(EditorAsset::new);

                asset.setAssetKey(assetKey);
                asset.setName(requiredString(assetData, "name", filePath));
                asset.setAssetType(assetType);

                asset.setCategory(
                        assetData.get("category") == null
                                ? null
                                : String.valueOf(assetData.get("category"))
                );

                Object jsonData = assetData.get("assetData");

                if (!(jsonData instanceof Map<?, ?>)) {
                    throw new IllegalStateException(
                            "Invalid assetData for asset: " + assetKey
                    );
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> typedAssetData =
                        (Map<String, Object>) jsonData;

                asset.setAssetData(typedAssetData);

                Object sortOrder = assetData.get("sortOrder");

                asset.setSortOrder(
                        sortOrder instanceof Number
                                ? ((Number) sortOrder).intValue()
                                : 0
                );

                asset.setIsActive(true);

                editorAssetRepository.save(asset);

                if (existing.isPresent()) {
                    updated++;
                } else {
                    inserted++;
                }
            }

            return updateExisting
                    ? new int[]{inserted, updated, skipped}
                    : new int[]{inserted, skipped};
        }
    }

    private String requiredString(
            Map<?, ?> data,
            String key,
            String filePath
    ) {

        Object value = data.get(key);

        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalStateException(
                    "Missing '" + key + "' in " + filePath + "."
            );
        }

        return String.valueOf(value);
    }
}
