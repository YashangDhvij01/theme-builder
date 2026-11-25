package com.themebuilder.backend.service;

import com.themebuilder.backend.entity.EditorAsset;
import com.themebuilder.backend.repository.EditorAssetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphicService {

    private static final long CACHE_TTL_MS = 10 * 60 * 1000L;

    private final EditorAssetRepository editorAssetRepository;

    // In-memory snapshot of all active graphics. Every endpoint filters this list
    // instead of re-reading the whole table (with big SVG JSON) from the remote DB.
    private volatile List<EditorAsset> snapshot;
    private volatile long snapshotLoadedAt;

    public GraphicService(EditorAssetRepository editorAssetRepository) {
        this.editorAssetRepository = editorAssetRepository;
    }

    private List<EditorAsset> loadAll() {
        List<EditorAsset> current = snapshot;
        if (current == null || System.currentTimeMillis() - snapshotLoadedAt > CACHE_TTL_MS) {
            synchronized (this) {
                current = snapshot;
                if (current == null || System.currentTimeMillis() - snapshotLoadedAt > CACHE_TTL_MS) {
                    current = refreshCache();
                }
            }
        }
        return current;
    }

    /** Reload the snapshot from the database (called on startup after seeding and on TTL expiry). */
    public synchronized List<EditorAsset> refreshCache() {
        List<EditorAsset> fresh = List.copyOf(editorAssetRepository
                .findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc("GRAPHIC"));
        snapshot = fresh;
        snapshotLoadedAt = System.currentTimeMillis();
        return fresh;
    }

    /**
     * Fetch aggregated categories with metadata, counts, and preview icons.
     * Optionally filtered by type: "static" or "animated".
     */
    public List<Map<String, Object>> getCategories(String type) {
        List<EditorAsset> allGraphics = loadAll();

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

    public List<Map<String, Object>> getCategories() {
        return getCategories(null);
    }

    /**
     * Search and filter graphics with pagination.
     */
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

        List<EditorAsset> allGraphics = loadAll();

        // Apply in-memory filters
        List<EditorAsset> filtered = allGraphics.stream()
                .filter(asset -> {
                    Map<String, Object> data = asset.getAssetData();

                    // Category Filter (accepts a single id/name or a comma-separated list)
                    if (categoryId != null && !categoryId.isBlank() && !"all".equalsIgnoreCase(categoryId)) {
                        String assetCatId = data != null && data.get("categoryId") != null
                                ? String.valueOf(data.get("categoryId"))
                                : toKebabCase(asset.getCategory());
                        boolean matched = false;
                        for (String wanted : categoryId.split(",")) {
                            String w = wanted.trim();
                            if (w.isEmpty()) continue;
                            if (assetCatId.equalsIgnoreCase(w)
                                    || (asset.getCategory() != null && asset.getCategory().equalsIgnoreCase(w))) {
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
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

        // For merged sections (several categories), interleave so every collection is represented.
        if (categoryId != null && categoryId.contains(",")) {
            Map<String, List<EditorAsset>> byCat = new LinkedHashMap<>();
            for (EditorAsset a : filtered) {
                byCat.computeIfAbsent(a.getCategory() == null ? "" : a.getCategory(), k -> new ArrayList<>()).add(a);
            }
            List<EditorAsset> mixed = new ArrayList<>(filtered.size());
            for (int i = 0; mixed.size() < filtered.size(); i++) {
                for (List<EditorAsset> list : byCat.values()) {
                    if (i < list.size()) mixed.add(list.get(i));
                }
            }
            filtered = mixed;
        }

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
    public List<EditorAsset> getRecommended(String type, int limit) {
        int safeLimit = limit > 0 ? Math.min(limit, 50) : 20;

        List<EditorAsset> allGraphics = loadAll();

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

    public List<EditorAsset> getRecommended(int limit) {
        return getRecommended(null, limit);
    }

    /**
     * Get single graphic by ID or assetKey.
     */
    public Optional<EditorAsset> getGraphicByIdOrKey(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        String ident = identifier.trim();
        for (EditorAsset a : loadAll()) {
            if (ident.equals(String.valueOf(a.getId())) || ident.equals(a.getAssetKey())) {
                return Optional.of(a);
            }
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
            case "featured" -> "star";
            case "social-media" -> "share";
            case "realistic-animals" -> "paw-print";
            case "abstract-shapes" -> "shapes";
            case "gradients-blurs" -> "palette";
            default -> "sparkles";
        };
    }
}
