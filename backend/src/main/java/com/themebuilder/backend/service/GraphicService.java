package com.themebuilder.backend.service;

import com.themebuilder.backend.entity.EditorAsset;
import com.themebuilder.backend.repository.EditorAssetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphicService {

    private final EditorAssetRepository editorAssetRepository;

    public GraphicService(EditorAssetRepository editorAssetRepository) {
        this.editorAssetRepository = editorAssetRepository;
    }

    /**
     * Fetch aggregated categories with metadata, counts, and preview icons.
     * Optionally filtered by type: "static" or "animated".
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCategories(String type) {
        List<EditorAsset> allGraphics = editorAssetRepository
                .findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc("GRAPHIC");

        // Maintain category display order
        Map<String, List<EditorAsset>> grouped = new LinkedHashMap<>();
        for (EditorAsset asset : allGraphics) {
            String category = asset.getCategory() != null ? asset.getCategory() : "Other";
            grouped.computeIfAbsent(category, k -> new ArrayList<>()).add(asset);
        }

        List<Map<String, Object>> categorySummaries = new ArrayList<>();

        for (Map.Entry<String, List<EditorAsset>> entry : grouped.entrySet()) {
            String categoryName = entry.getKey();
            List<EditorAsset> items = entry.getValue();
            EditorAsset firstAsset = items.get(0);

            Map<String, Object> assetData = firstAsset.getAssetData();
            String categoryId = assetData != null && assetData.get("categoryId") != null
                    ? String.valueOf(assetData.get("categoryId"))
                    : toKebabCase(categoryName);

            boolean hasAnimated = items.stream().anyMatch(a -> {
                Map<String, Object> d = a.getAssetData();
                return d != null && "animated".equalsIgnoreCase(String.valueOf(d.get("type")));
            });

            // If a specific type is requested, filter categories appropriately
            if (type != null && !type.isBlank() && !"all".equalsIgnoreCase(type)) {
                if ("animated".equalsIgnoreCase(type.trim()) && !hasAnimated) {
                    continue;
                }
                if ("static".equalsIgnoreCase(type.trim()) && hasAnimated) {
                    continue;
                }
            }

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("id", categoryId);
            summary.put("name", categoryName);
            summary.put("count", items.size());
            summary.put("iconKey", mapCategoryIcon(categoryId));
            summary.put("previewSvg", assetData != null ? assetData.get("svgContent") : null);
            summary.put("isAnimated", hasAnimated);

            categorySummaries.add(summary);
        }

        return categorySummaries;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCategories() {
        return getCategories(null);
    }

    /**
     * Search and filter graphics with pagination.
     */
    @Transactional(readOnly = true)
    public Page<EditorAsset> getGraphics(
            String categoryId,
            String search,
            String type,
            Boolean isRecommended,
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index cannot be negative.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100.");
        }

        List<EditorAsset> allGraphics = editorAssetRepository
                .findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc("GRAPHIC");

        // Apply in-memory filters
        List<EditorAsset> filtered = allGraphics.stream()
                .filter(asset -> {
                    Map<String, Object> data = asset.getAssetData();

                    // Category Filter
                    if (categoryId != null && !categoryId.isBlank() && !"all".equalsIgnoreCase(categoryId)) {
                        String assetCatId = data != null && data.get("categoryId") != null
                                ? String.valueOf(data.get("categoryId"))
                                : toKebabCase(asset.getCategory());
                        boolean matchId = assetCatId.equalsIgnoreCase(categoryId.trim());
                        boolean matchName = asset.getCategory() != null && asset.getCategory().equalsIgnoreCase(categoryId.trim());
                        if (!matchId && !matchName) {
                            return false;
                        }
                    }

                    // Recommended Filter
                    if (Boolean.TRUE.equals(isRecommended)) {
                        if (data == null || !Boolean.TRUE.equals(data.get("isRecommended"))) {
                            return false;
                        }
                    }

                    // Type Filter: static vs animated
                    if (type != null && !type.isBlank() && !"all".equalsIgnoreCase(type)) {
                        String assetType = data != null && data.get("type") != null
                                ? String.valueOf(data.get("type"))
                                : "static";
                        if (!assetType.equalsIgnoreCase(type.trim())) {
                            return false;
                        }
                    }

                    // Search Filter
                    if (search != null && !search.isBlank()) {
                        String q = search.trim().toLowerCase();
                        boolean nameMatch = asset.getName() != null && asset.getName().toLowerCase().contains(q);
                        boolean catMatch = asset.getCategory() != null && asset.getCategory().toLowerCase().contains(q);
                        boolean tagMatch = false;

                        if (data != null) {
                            Object tagsObj = data.get("tags");
                            if (tagsObj instanceof List<?> tags) {
                                tagMatch = tags.stream().anyMatch(t -> String.valueOf(t).toLowerCase().contains(q));
                            }
                            if (!tagMatch && data.get("keywords") instanceof List<?> kws) {
                                tagMatch = kws.stream().anyMatch(k -> String.valueOf(k).toLowerCase().contains(q));
                            }
                        }

                        if (!nameMatch && !catMatch && !tagMatch) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<EditorAsset> subList = filtered.subList(start, end);

        return new PageImpl<>(subList, pageable, filtered.size());
    }

    /**
     * Get recommended graphics for the top horizontal rail.
     * Optionally filtered by type: "static" or "animated".
     */
    @Transactional(readOnly = true)
    public List<EditorAsset> getRecommended(String type, int limit) {
        int safeLimit = limit > 0 ? Math.min(limit, 50) : 20;

        List<EditorAsset> allGraphics = editorAssetRepository
                .findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc("GRAPHIC");

        return allGraphics.stream()
                .filter(asset -> {
                    Map<String, Object> data = asset.getAssetData();
                    if (data == null || !Boolean.TRUE.equals(data.get("isRecommended"))) {
                        return false;
                    }
                    if (type != null && !type.isBlank() && !"all".equalsIgnoreCase(type)) {
                        String assetType = data.get("type") != null ? String.valueOf(data.get("type")) : "static";
                        return assetType.equalsIgnoreCase(type.trim());
                    }
                    return true;
                })
                .limit(safeLimit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EditorAsset> getRecommended(int limit) {
        return getRecommended(null, limit);
    }

    /**
     * Get single graphic by ID or assetKey.
     */
    @Transactional(readOnly = true)
    public Optional<EditorAsset> getGraphicByIdOrKey(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        try {
            Long id = Long.parseLong(identifier.trim());
            Optional<EditorAsset> byId = editorAssetRepository.findById(id);
            if (byId.isPresent() && "GRAPHIC".equalsIgnoreCase(byId.get().getAssetType())) {
                return byId;
            }
        } catch (NumberFormatException ignored) {
            // Not a numeric ID, search by assetKey
        }

        return editorAssetRepository.findByAssetTypeAndAssetKey("GRAPHIC", identifier.trim());
    }

    private String toKebabCase(String input) {
        if (input == null) return "other";
        return input.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    private String mapCategoryIcon(String categoryId) {
        return switch (categoryId) {
            case "bold-foliage" -> "leaf";
            case "organic-summer" -> "sun";
            case "camping-rustic-drawing" -> "tent";
            case "vivid-leaves" -> "leaf";
            case "handdrawn-animals" -> "heart";
            case "pastel-travel-accessories" -> "compass";
            case "simple-drawn-objects" -> "coffee";
            case "organic-sketched-fruit" -> "apple";
            case "arrow-animations" -> "arrow-right";
            case "word-animations" -> "type";
            case "shape-animations" -> "shapes";
            case "emoji-animations" -> "smile";
            case "food-animations" -> "utensils";
            case "decorative-animations" -> "sparkles";
            case "certificate-animations" -> "award";
            case "educational-graphics" -> "book-open";
            case "business-graphics" -> "briefcase";
            case "lines-dividers" -> "minus";
            case "abstract-shapes" -> "shapes";
            case "gradients-blurs" -> "palette";
            default -> "sparkles";
        };
    }
}
