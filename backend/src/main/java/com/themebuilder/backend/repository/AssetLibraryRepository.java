package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.AssetLibrary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetLibraryRepository extends JpaRepository<AssetLibrary, Long> {

    List<AssetLibrary> findByTypeAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(
            String type
    );

    Page<AssetLibrary> findByTypeAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(
            String type,
            Pageable pageable
    );

    Page<AssetLibrary> findByTypeAndCategoryIgnoreCaseAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(
            String type,
            String category,
            Pageable pageable
    );

    Optional<AssetLibrary> findByTypeAndAssetKeyAndIsDeletedFalse(
            String type,
            String assetKey
    );

    Optional<AssetLibrary> findByTypeAndAssetKey(
            String type,
            String assetKey
    );

    boolean existsByTypeAndAssetKey(
            String type,
            String assetKey
    );

    @Query(
            value = "SELECT * FROM asset_library a WHERE a.is_active = true AND a.is_deleted = false " +
                    "AND (:type IS NULL OR :type = '' OR UPPER(a.type) = UPPER(:type)) " +
                    "AND (:category IS NULL OR :category = '' OR LOWER(:category) = 'all' OR LOWER(a.category) = LOWER(:category)) " +
                    "AND (:search IS NULL OR :search = '' " +
                    "     OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(a.asset_key) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(COALESCE(a.category, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(COALESCE(a.subcategory, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                    "ORDER BY a.sort_order ASC",
            countQuery = "SELECT count(*) FROM asset_library a WHERE a.is_active = true AND a.is_deleted = false " +
                    "AND (:type IS NULL OR :type = '' OR UPPER(a.type) = UPPER(:type)) " +
                    "AND (:category IS NULL OR :category = '' OR LOWER(:category) = 'all' OR LOWER(a.category) = LOWER(:category)) " +
                    "AND (:search IS NULL OR :search = '' " +
                    "     OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(a.asset_key) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(COALESCE(a.category, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(COALESCE(a.subcategory, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
            nativeQuery = true
    )
    Page<AssetLibrary> searchAssets(
            @Param("type") String type,
            @Param("category") String category,
            @Param("search") String search,
            Pageable pageable
    );

    @Query(
            value = "SELECT * FROM asset_library a WHERE " +
                    "(:type IS NULL OR :type = '' OR UPPER(a.type) = UPPER(:type)) " +
                    "AND (:category IS NULL OR :category = '' OR LOWER(:category) = 'all' OR LOWER(a.category) = LOWER(:category)) " +
                    "AND (:isActive IS NULL OR a.is_active = :isActive) " +
                    "AND (:isDeleted IS NULL OR a.is_deleted = :isDeleted) " +
                    "AND (:search IS NULL OR :search = '' " +
                    "     OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(a.asset_key) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(COALESCE(a.category, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(COALESCE(a.subcategory, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                    "ORDER BY a.sort_order ASC, a.id DESC",
            countQuery = "SELECT count(*) FROM asset_library a WHERE " +
                    "(:type IS NULL OR :type = '' OR UPPER(a.type) = UPPER(:type)) " +
                    "AND (:category IS NULL OR :category = '' OR LOWER(:category) = 'all' OR LOWER(a.category) = LOWER(:category)) " +
                    "AND (:isActive IS NULL OR a.is_active = :isActive) " +
                    "AND (:isDeleted IS NULL OR a.is_deleted = :isDeleted) " +
                    "AND (:search IS NULL OR :search = '' " +
                    "     OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(a.asset_key) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(COALESCE(a.category, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(COALESCE(a.subcategory, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
            nativeQuery = true
    )
    Page<AssetLibrary> findAdminAssets(
            @Param("type") String type,
            @Param("category") String category,
            @Param("isActive") Boolean isActive,
            @Param("isDeleted") Boolean isDeleted,
            @Param("search") String search,
            Pageable pageable
    );

    @Query(
            value = "SELECT UPPER(type) AS type, COUNT(*) AS cnt " +
                    "FROM asset_library " +
                    "WHERE is_deleted = false " +
                    "GROUP BY UPPER(type) " +
                    "ORDER BY UPPER(type)",
            nativeQuery = true
    )
    List<Object[]> countActiveTypes();

    @Query(
            value = "SELECT DISTINCT category FROM asset_library " +
                    "WHERE (:type IS NULL OR :type = '' OR UPPER(type) = UPPER(:type)) " +
                    "AND is_deleted = false AND category IS NOT NULL AND category <> '' " +
                    "ORDER BY category ASC",
            nativeQuery = true
    )
    List<String> findDistinctCategories(@Param("type") String type);

    @Query(
            value = "SELECT COALESCE(MAX(sort_order), 0) FROM asset_library " +
                    "WHERE UPPER(type) = UPPER(:type) " +
                    "AND LOWER(category) = LOWER(:category) " +
                    "AND is_deleted = false",
            nativeQuery = true
    )
    Integer findMaxSortOrderByTypeAndCategory(
            @Param("type") String type,
            @Param("category") String category
    );
}
