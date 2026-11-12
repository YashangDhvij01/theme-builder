package com.themebuilder.backend.config;

import com.themebuilder.backend.entity.EditorAsset;
import com.themebuilder.backend.repository.EditorAssetRepository;

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
public class EditorAssetDataInitializer implements CommandLineRunner {

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

        int totalInserted = 0;
        int totalSkipped = 0;

        List<String> assetFiles = List.of(
                "assets/text-assets.json",
                "assets/shape-assets.json"
        );

        for (String fileName : assetFiles) {

            ClassPathResource resource =
                    new ClassPathResource(fileName);

            if (!resource.exists()) {
                throw new IllegalStateException(
                        fileName + " file not found in resources folder."
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
                            "Invalid " + fileName + ": assets must be an array."
                    );
                }

                int inserted = 0;
                int skipped = 0;

                for (Object item : assets) {

                    if (!(item instanceof Map<?, ?> assetData)) {
                        throw new IllegalStateException(
                                "Invalid asset record in " + fileName
                        );
                    }

                    String assetKey = String.valueOf(
                            assetData.get("assetKey")
                    );

                    String assetType = String.valueOf(
                            assetData.get("assetType")
                    ).toUpperCase();

                    if (editorAssetRepository.existsByAssetTypeAndAssetKey(
                            assetType,
                            assetKey
                    )) {
                        skipped++;
                        continue;
                    }

                    EditorAsset asset = new EditorAsset();

                    asset.setAssetKey(assetKey);

                    asset.setName(
                            String.valueOf(assetData.get("name"))
                    );

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

                    inserted++;
                }

                totalInserted += inserted;
                totalSkipped += skipped;

                System.out.println("-----------------------------------");
                System.out.println("FILE: " + fileName);
                System.out.println("Inserted assets: " + inserted);
                System.out.println("Skipped existing assets: " + skipped);
                System.out.println("-----------------------------------");
            }
        }

        System.out.println("===================================");
        System.out.println("EDITOR ASSET INITIALIZATION COMPLETE");
        System.out.println("Total inserted assets: " + totalInserted);
        System.out.println("Total skipped assets: " + totalSkipped);
        System.out.println("===================================");
    }
}