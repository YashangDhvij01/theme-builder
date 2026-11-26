package com.themebuilder.backend.controller;

import com.themebuilder.backend.dto.ThemeElementResponse;
import com.themebuilder.backend.entity.AssetLibrary;
import com.themebuilder.backend.exception.ResourceNotFoundException;
import com.themebuilder.backend.repository.AssetLibraryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/theme-elements")
public class ThemeElementController {

    private final AssetLibraryRepository assetLibraryRepository;

    public ThemeElementController(AssetLibraryRepository assetLibraryRepository) {
        this.assetLibraryRepository = assetLibraryRepository;
    }

    private String resolveAssetType(String kind) {
        if (kind == null) {
            throw new IllegalArgumentException("Theme element kind is required.");
        }
        return switch (kind.trim().toLowerCase(Locale.ROOT)) {
            case "icons", "icon" -> "ICON";
            case "bricks", "brick" -> "BRICK";
            case "columns", "column" -> "COLUMN";
            default -> throw new IllegalArgumentException(
                    "Unsupported theme element kind: " + kind
            );
        };
    }

    @GetMapping("/{kind}/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories(
            @PathVariable String kind
    ) {
        String assetType = resolveAssetType(kind);
        List<AssetLibrary> assets = assetLibraryRepository
                .findByTypeAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(assetType);

        Map<String, List<AssetLibrary>> grouped = new LinkedHashMap<>();
        for (AssetLibrary asset : assets) {
            String cat = (asset.getCategory() != null && !asset.getCategory().isBlank())
                    ? asset.getCategory().trim()
                    : "general";
            grouped.computeIfAbsent(cat, k -> new ArrayList<>()).add(asset);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<AssetLibrary>> entry : grouped.entrySet()) {
            AssetLibrary first = entry.getValue().get(0);
            Map<String, Object> data = first.getJsonData();
            Object svgObj = data != null ? data.get("svgContent") : null;
            String previewSvg = svgObj != null ? svgObj.toString() : null;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", entry.getKey());
            item.put("name", entry.getKey());
            item.put("count", entry.getValue().size());
            item.put("previewSvg", previewSvg);
            item.put("previewData", data);
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{kind}/page")
    public ResponseEntity<Page<ThemeElementResponse>> getPage(
            @PathVariable String kind,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        String assetType = resolveAssetType(kind);
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));
        String normalizedCategory = (category == null || category.isBlank()) ? null : category.trim();
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();

        Page<ThemeElementResponse> result = assetLibraryRepository
                .searchAssets(
                        assetType,
                        normalizedCategory,
                        normalizedSearch,
                        PageRequest.of(safePage, safeSize)
                )
                .map(ThemeElementResponse::fromEntity);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{kind}/{identifier}")
    public ResponseEntity<ThemeElementResponse> getByIdOrKey(
            @PathVariable String kind,
            @PathVariable String identifier
    ) {
        String assetType = resolveAssetType(kind);

        if (identifier.matches("\\d+")) {
            Long id = Long.parseLong(identifier);
            AssetLibrary byId = assetLibraryRepository.findById(id)
                    .filter(a -> assetType.equalsIgnoreCase(a.getType())
                            && Boolean.TRUE.equals(a.getIsActive())
                            && !Boolean.TRUE.equals(a.getIsDeleted()))
                    .orElse(null);
            if (byId != null) {
                return ResponseEntity.ok(ThemeElementResponse.fromEntity(byId));
            }
        }

        AssetLibrary byKey = assetLibraryRepository
                .findByTypeAndAssetKeyAndIsDeletedFalse(assetType, identifier)
                .filter(a -> Boolean.TRUE.equals(a.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Theme element not found: " + identifier
                ));
        return ResponseEntity.ok(ThemeElementResponse.fromEntity(byKey));
    }
}
