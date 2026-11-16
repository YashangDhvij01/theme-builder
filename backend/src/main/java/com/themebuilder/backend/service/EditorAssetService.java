package com.themebuilder.backend.service;

import com.themebuilder.backend.entity.EditorAsset;
import com.themebuilder.backend.repository.EditorAssetRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EditorAssetService {

    private final EditorAssetRepository editorAssetRepository;

    public EditorAssetService(EditorAssetRepository editorAssetRepository) {
        this.editorAssetRepository = editorAssetRepository;
    }

    // 1. Fetch active text presets or shapes by type
    // Existing method preserved for backward compatibility
    @Transactional(readOnly = true)
    public List<EditorAsset> getAssetsByType(String assetType) {

        validateAssetType(assetType);

        return editorAssetRepository
                .findByAssetTypeAndIsActiveTrueOrderBySortOrderAsc(
                        assetType.toUpperCase()
                );
    }

    // 2. Fetch all active assets
    // Existing method preserved
    @Transactional(readOnly = true)
    public List<EditorAsset> getAllActiveAssets() {

        return editorAssetRepository
                .findByIsActiveTrueOrderByAssetTypeAscSortOrderAsc();
    }

    // 3. NEW: Fetch assets with pagination
    // Default frontend batch size will be 20
    @Transactional(readOnly = true)
    public Page<EditorAsset> getAssetsByTypePaginated(
            String assetType,
            int page,
            int size,
            String category
    ) {

        validateAssetType(assetType);

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100."
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        String normalizedType = assetType.toUpperCase();

        // Category filter
        if (category != null && !category.isBlank()
                && !category.equalsIgnoreCase("All")) {

            return editorAssetRepository
                    .findByAssetTypeAndIsActiveTrueAndCategoryIgnoreCaseOrderBySortOrderAscIdAsc(
                            normalizedType,
                            category.trim(),
                            pageable
                    );
        }

        // Without category filter
        return editorAssetRepository
                .findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc(
                        normalizedType,
                        pageable
                );
    }

    // 4. Add a new text preset or shape
    @Transactional
    public EditorAsset addAsset(EditorAsset asset) {

        validateAsset(asset);

        String assetType = asset.getAssetType().toUpperCase();
        String assetKey = asset.getAssetKey().trim();

        if (editorAssetRepository.existsByAssetTypeAndAssetKey(
                assetType, assetKey)) {

            throw new IllegalArgumentException(
                    "Asset with this type and key already exists."
            );
        }

        asset.setAssetType(assetType);
        asset.setAssetKey(assetKey);
        asset.setIsActive(true);

        return editorAssetRepository.save(asset);
    }

    // 5. Update an existing asset
    @Transactional
    public EditorAsset updateAsset(Long id, EditorAsset updatedAsset) {

        validateAsset(updatedAsset);

        EditorAsset existingAsset = editorAssetRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Asset not found with id: " + id
                        )
                );

        String assetType = updatedAsset.getAssetType().toUpperCase();
        String assetKey = updatedAsset.getAssetKey().trim();

        editorAssetRepository
                .findByAssetTypeAndAssetKey(assetType, assetKey)
                .ifPresent(duplicate -> {

                    if (!duplicate.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Another asset already uses this type and key."
                        );
                    }
                });

        existingAsset.setName(updatedAsset.getName().trim());
        existingAsset.setAssetType(assetType);
        existingAsset.setAssetKey(assetKey);
        existingAsset.setCategory(updatedAsset.getCategory());
        existingAsset.setAssetData(updatedAsset.getAssetData());
        existingAsset.setSortOrder(updatedAsset.getSortOrder());

        return editorAssetRepository.save(existingAsset);
    }

    // 6. Deactivate an asset instead of permanently deleting it
    @Transactional
    public EditorAsset deactivateAsset(Long id) {

        EditorAsset asset = editorAssetRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Asset not found with id: " + id
                        )
                );

        asset.setIsActive(false);

        return editorAssetRepository.save(asset);
    }

    // 7. Activate an existing asset again
    @Transactional
    public EditorAsset activateAsset(Long id) {

        EditorAsset asset = editorAssetRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Asset not found with id: " + id
                        )
                );

        asset.setIsActive(true);

        return editorAssetRepository.save(asset);
    }

    // Validation for asset type
    private void validateAssetType(String assetType) {

        if (assetType == null ||
                (!assetType.equalsIgnoreCase("TEXT")
                        && !assetType.equalsIgnoreCase("SHAPE"))) {

            throw new IllegalArgumentException(
                    "Asset type must be TEXT or SHAPE."
            );
        }
    }

    // Validation for new and updated assets
    private void validateAsset(EditorAsset asset) {

        if (asset == null) {
            throw new IllegalArgumentException(
                    "Asset data is required."
            );
        }

        validateAssetType(asset.getAssetType());

        if (asset.getAssetKey() == null ||
                asset.getAssetKey().isBlank()) {

            throw new IllegalArgumentException(
                    "Asset key is required."
            );
        }

        if (asset.getName() == null ||
                asset.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Asset name is required."
            );
        }

        if (asset.getAssetData() == null) {
            throw new IllegalArgumentException(
                    "Asset JSON data is required."
            );
        }

        if (asset.getSortOrder() == null ||
                asset.getSortOrder() < 0) {

            throw new IllegalArgumentException(
                    "Sort order must be zero or greater."
            );
        }
    }
}