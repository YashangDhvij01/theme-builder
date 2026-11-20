package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.EditorAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Paginated methods
    Page<EditorAsset> findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc(
            String assetType,
            Pageable pageable
    );

    Page<EditorAsset> findByAssetTypeAndIsActiveTrueAndCategoryIgnoreCaseOrderBySortOrderAscIdAsc(
            String assetType,
            String category,
            Pageable pageable
    );

    List<EditorAsset> findByAssetType(
            String assetType
    );

    List<EditorAsset> findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc(
            String assetType
    );

    List<EditorAsset> findByAssetTypeAndIsActiveTrueAndCategoryIgnoreCaseOrderBySortOrderAscIdAsc(
            String assetType,
            String category
    );

    // Database-level search across asset types, categories, names, keys, and asset_data JSON
    @Query(
            value = "SELECT * FROM editor_assets a WHERE a.is_active = true " +
                    "AND (:assetType IS NULL OR :assetType = '' OR UPPER(a.asset_type) = UPPER(:assetType)) " +
                    "AND (:category IS NULL OR :category = '' OR LOWER(:category) = 'all' OR LOWER(a.category) = LOWER(:category)) " +
                    "AND (:search IS NULL OR :search = '' " +
                    "     OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(a.asset_key) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(COALESCE(a.category, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(CAST(a.asset_data AS text)) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                    "ORDER BY a.sort_order ASC, a.id ASC",
            countQuery = "SELECT count(*) FROM editor_assets a WHERE a.is_active = true " +
                    "AND (:assetType IS NULL OR :assetType = '' OR UPPER(a.asset_type) = UPPER(:assetType)) " +
                    "AND (:category IS NULL OR :category = '' OR LOWER(:category) = 'all' OR LOWER(a.category) = LOWER(:category)) " +
                    "AND (:search IS NULL OR :search = '' " +
                    "     OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(a.asset_key) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(COALESCE(a.category, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(CAST(a.asset_data AS text)) LIKE LOWER(CONCAT('%', :search, '%')))",
            nativeQuery = true
    )
    Page<EditorAsset> searchAssetsNative(
            @Param("assetType") String assetType,
            @Param("category") String category,
            @Param("search") String search,
            Pageable pageable
    );
}