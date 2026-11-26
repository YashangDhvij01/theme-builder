package com.themebuilder.backend.service;

import com.themebuilder.backend.dto.AssetLibraryRequest;
// import com.themebuilder.backend.dto.BulkAssetImportResult;
import com.themebuilder.backend.entity.AssetLibrary;
import com.themebuilder.backend.repository.AssetLibraryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssetLibraryAdminService {

    private final AssetLibraryRepository assetLibraryRepository;

    public AssetLibraryAdminService(AssetLibraryRepository assetLibraryRepository) {
        this.assetLibraryRepository = assetLibraryRepository;
    }

    public Page<AssetLibrary> getAdminAssets(
            String type,
            String category,
            Boolean isActive,
            Boolean isDeleted,
            String search,
            int page,
            int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String normType = trimToNull(type);
        if ("ALL".equalsIgnoreCase(normType)) {
            normType = null;
        }
        String normCat = trimToNull(category);
        String normSearch = trimToNull(search);

        return assetLibraryRepository.findAdminAssets(
                normType,
                normCat,
                isActive,
                isDeleted != null ? isDeleted : Boolean.FALSE,
                normSearch,
                pageable
        );
    }

    public Map<String, Object> getStats() {
        List<Object[]> rows = assetLibraryRepository.countActiveTypes();
        Map<String, Long> byType = new LinkedHashMap<>();
        long total = 0;
        for (Object[] row : rows) {
            String t = String.valueOf(row[0]);
            long cnt = ((Number) row[1]).longValue();
            byType.put(t, cnt);
            total += cnt;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("byType", byType);
        return result;
    }

    public List<String> getCategories(String type) {
        String normType = trimToNull(type);
        if ("ALL".equalsIgnoreCase(normType)) {
            normType = null;
        }
        return assetLibraryRepository.findDistinctCategories(normType);
    }

    public AssetLibrary getById(Long id) {
        return assetLibraryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with id: " + id));
    }

    @Transactional
    public AssetLibrary create(AssetLibraryRequest req) {
        validateRequest(req);

        String type = req.getType().trim().toUpperCase();
        String assetKey = req.getAssetKey().trim();

        if (assetLibraryRepository.existsByTypeAndAssetKey(type, assetKey)) {
            throw new IllegalArgumentException(
                    "Asset already exists with type '" + type + "' and asset_key '" + assetKey + "'"
            );
        }

        AssetLibrary entity = new AssetLibrary();
        applyFields(entity, req);
        if (req.getSortOrder() == null) {
            Integer maxOrder = assetLibraryRepository.findMaxSortOrderByTypeAndCategory(
                    type,
                    req.getCategory().trim()
            );
            entity.setSortOrder((maxOrder != null ? maxOrder : 0) + 1);
        }
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return assetLibraryRepository.save(entity);
    }

    @Transactional
    public AssetLibrary update(Long id, AssetLibraryRequest req) {
        AssetLibrary entity = getById(id);
        validateRequest(req);

        String newType = req.getType().trim().toUpperCase();
        String newKey = req.getAssetKey().trim();

        boolean keyOrTypeChanged = !newType.equalsIgnoreCase(entity.getType())
                || !newKey.equals(entity.getAssetKey());

        if (keyOrTypeChanged && assetLibraryRepository.existsByTypeAndAssetKey(newType, newKey)) {
            throw new IllegalArgumentException(
                    "Another asset already uses type '" + newType + "' and asset_key '" + newKey + "'"
            );
        }

        Integer existingSortOrder = entity.getSortOrder();
        applyFields(entity, req);
        if (req.getSortOrder() == null) {
            entity.setSortOrder(existingSortOrder != null ? existingSortOrder : 0);
        }
        entity.setUpdatedAt(LocalDateTime.now());

        return assetLibraryRepository.save(entity);
    }

    @Transactional
    public AssetLibrary toggleActive(Long id, boolean isActive) {
        AssetLibrary entity = getById(id);
        entity.setIsActive(isActive);
        entity.setUpdatedAt(LocalDateTime.now());
        return assetLibraryRepository.save(entity);
    }

    @Transactional
    public AssetLibrary setDeleted(Long id, boolean isDeleted) {
        AssetLibrary entity = getById(id);
        entity.setIsDeleted(isDeleted);
        entity.setUpdatedAt(LocalDateTime.now());
        return assetLibraryRepository.save(entity);
    }

    /*
    @Transactional
    public BulkAssetImportResult bulkImport(List<AssetLibraryRequest> requests) {
        BulkAssetImportResult result = new BulkAssetImportResult();
        if (requests == null) {
            return result;
        }
        result.setTotal(requests.size());

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < requests.size(); i++) {
            AssetLibraryRequest req = requests.get(i);
            try {
                validateRequest(req);
                String type = req.getType().trim().toUpperCase();
                String assetKey = req.getAssetKey().trim();

                Optional<AssetLibrary> existingOpt = assetLibraryRepository.findByTypeAndAssetKey(type, assetKey);
                AssetLibrary entity;
                boolean isNew = existingOpt.isEmpty();
                if (isNew) {
                    entity = new AssetLibrary();
                    entity.setCreatedAt(now);
                } else {
                    entity = existingOpt.get();
                }

                applyFields(entity, req);
                entity.setUpdatedAt(now);
                assetLibraryRepository.save(entity);

                if (isNew) {
                    result.setInserted(result.getInserted() + 1);
                } else {
                    result.setUpdated(result.getUpdated() + 1);
                }
            } catch (Exception ex) {
                result.setFailed(result.getFailed() + 1);
                result.getErrors().add("Row " + (i + 1) + ": " + ex.getMessage());
            }
        }
        return result;
    }
    */

    private void validateRequest(AssetLibraryRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Request body cannot be empty.");
        }
        if (isBlank(req.getAssetKey())) {
            throw new IllegalArgumentException("asset_key is required.");
        }
        if (isBlank(req.getName())) {
            throw new IllegalArgumentException("name is required.");
        }
        if (isBlank(req.getType())) {
            throw new IllegalArgumentException("type is required.");
        }
        if (isBlank(req.getCategory())) {
            throw new IllegalArgumentException("category is required.");
        }
        if (req.getJsonData() == null) {
            throw new IllegalArgumentException("json_data is required.");
        }
    }

    private void applyFields(AssetLibrary entity, AssetLibraryRequest req) {
        entity.setAssetKey(req.getAssetKey().trim());
        entity.setName(req.getName().trim());
        entity.setType(req.getType().trim().toUpperCase());
        entity.setCategory(req.getCategory().trim());
        entity.setSubcategory(trimToNull(req.getSubcategory()));
        entity.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        entity.setJsonData(req.getJsonData() != null ? req.getJsonData() : new HashMap<>());
        entity.setIsActive(req.getIsActive() != null ? req.getIsActive() : Boolean.TRUE);
        entity.setIsDeleted(req.getIsDeleted() != null ? req.getIsDeleted() : Boolean.FALSE);
    }

    private boolean isBlank(String val) {
        return val == null || val.trim().isEmpty();
    }

    private String trimToNull(String val) {
        if (val == null) return null;
        String trimmed = val.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
