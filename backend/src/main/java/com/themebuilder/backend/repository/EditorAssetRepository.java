package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.EditorAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EditorAssetRepository extends JpaRepository<EditorAsset, Long> {

    // Existing methods - backward compatibility
    List<EditorAsset> findByAssetTypeAndIsActiveTrueOrderBySortOrderAsc(
            String assetType
    );

    List<EditorAsset> findByIsActiveTrueOrderByAssetTypeAscSortOrderAsc();

    boolean existsByAssetTypeAndAssetKey(
            String assetType,
            String assetKey
    );

    Optional<EditorAsset> findByAssetTypeAndAssetKey(
            String assetType,
            String assetKey
    );

    // New paginated methods
    Page<EditorAsset> findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc(
            String assetType,
            Pageable pageable
    );

    Page<EditorAsset> findByAssetTypeAndIsActiveTrueAndCategoryIgnoreCaseOrderBySortOrderAscIdAsc(
            String assetType,
            String category,
            Pageable pageable
    );
}