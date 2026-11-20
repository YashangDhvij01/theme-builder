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
public class EmojiService {

    private final EditorAssetRepository editorAssetRepository;

    // 14 canonical categories required by specification
    public static final List<String> CANONICAL_CATEGORIES = List.of(
            "Smileys & Emotions",
            "People & Gestures",
            "Animals & Nature",
            "Food & Drinks",
            "Travel & Places",
            "Activities & Sports",
            "Objects",
            "Symbols",
            "Flags",
            "Education & Learning",
            "Business & Work",
            "Celebration & Events",
            "Love & Relationships",
            "Weather & Seasons"
    );

    public EmojiService(EditorAssetRepository editorAssetRepository) {
        this.editorAssetRepository = editorAssetRepository;
    }

    /**
     * Aggregated category summaries with counts, categoryId, and preview SVG.
     * Strictly preserves the 14 canonical categories in required order.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCategories() {
        List<EditorAsset> allEmojis = editorAssetRepository
                .findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc("EMOJI");

        Map<String, List<EditorAsset>> grouped = new LinkedHashMap<>();
        for (String cat : CANONICAL_CATEGORIES) {
            grouped.put(cat, new ArrayList<>());
        }

        for (EditorAsset asset : allEmojis) {
            String cat = toCanonicalCategory(asset.getCategory());
            grouped.computeIfAbsent(cat, k -> new ArrayList<>()).add(asset);
        }

        List<Map<String, Object>> summaries = new ArrayList<>();

        for (String categoryName : CANONICAL_CATEGORIES) {
            List<EditorAsset> items = grouped.getOrDefault(categoryName, Collections.emptyList());
            String categoryId = toKebabCase(categoryName);

            String previewSvg = null;
            String iconUnicode = "😀";

            if (!items.isEmpty()) {
                // Find first asset with valid SVG content
                for (EditorAsset item : items) {
                    Map<String, Object> data = item.getAssetData();
                    if (data != null && data.get("svgContent") != null) {
                        previewSvg = String.valueOf(data.get("svgContent"));
                        if (data.get("unicode") != null) {
                            iconUnicode = String.valueOf(data.get("unicode"));
                        }
                        break;
                    }
                }
            }

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("id", categoryId);
            summary.put("name", categoryName);
            summary.put("count", items.size());
            summary.put("icon", iconUnicode);
            summary.put("previewSvg", previewSvg);

            summaries.add(summary);
        }

        return summaries;
    }

    private String toCanonicalCategory(String cat) {
        if (cat == null || cat.isBlank()) return "Smileys & Emotions";
        for (String canon : CANONICAL_CATEGORIES) {
            if (canon.equalsIgnoreCase(cat.trim()) || toKebabCase(canon).equalsIgnoreCase(cat.trim())) {
                return canon;
            }
        }
        if (cat.equalsIgnoreCase("Faces")) return "Smileys & Emotions";
        if (cat.equalsIgnoreCase("Animals") || cat.equalsIgnoreCase("Nature")) return "Animals & Nature";
        if (cat.equalsIgnoreCase("Awards")) return "Activities & Sports";
        if (cat.equalsIgnoreCase("Education")) return "Education & Learning";
        if (cat.equalsIgnoreCase("Celebration")) return "Celebration & Events";
        if (cat.equalsIgnoreCase("Business")) return "Business & Work";
        return "Smileys & Emotions";
    }

    /**
     * Top recommended emojis for Canva-style rail.
     */
    @Transactional(readOnly = true)
    public List<EditorAsset> getRecommended(int limit) {
        List<EditorAsset> allEmojis = editorAssetRepository
                .findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc("EMOJI");

        List<EditorAsset> recs = allEmojis.stream()
                .filter(asset -> {
                    Map<String, Object> data = asset.getAssetData();
                    return data != null && Boolean.TRUE.equals(data.get("isRecommended"));
                })
                .limit(limit)
                .collect(Collectors.toList());

        if (recs.isEmpty() && !allEmojis.isEmpty()) {
            return allEmojis.stream().limit(limit).collect(Collectors.toList());
        }

        return recs;
    }

    /**
     * Search and filter emojis with database querying and pagination.
     */
    @Transactional(readOnly = true)
    public Page<EditorAsset> getEmojis(
            String category,
            String search,
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

        Pageable pageable = PageRequest.of(page, size);

        String normalizedCategory = null;
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            // Find match in canonical categories (either exact or kebab-case)
            for (String canon : CANONICAL_CATEGORIES) {
                if (canon.equalsIgnoreCase(category.trim()) || toKebabCase(canon).equalsIgnoreCase(category.trim())) {
                    normalizedCategory = canon;
                    break;
                }
            }
            if (normalizedCategory == null) {
                normalizedCategory = category.trim();
            }
        }

        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        // Perform backend database search
        if (normalizedSearch != null) {
            Page<EditorAsset> dbResults = editorAssetRepository.searchAssetsNative(
                    "EMOJI",
                    normalizedCategory,
                    normalizedSearch,
                    pageable
            );

            if (Boolean.TRUE.equals(isRecommended)) {
                List<EditorAsset> recFiltered = dbResults.getContent().stream()
                        .filter(a -> a.getAssetData() != null && Boolean.TRUE.equals(a.getAssetData().get("isRecommended")))
                        .collect(Collectors.toList());
                return new PageImpl<>(recFiltered, pageable, recFiltered.size());
            }

            return dbResults;
        }

        // Filter by category
        if (normalizedCategory != null) {
            return editorAssetRepository.findByAssetTypeAndIsActiveTrueAndCategoryIgnoreCaseOrderBySortOrderAscIdAsc(
                    "EMOJI",
                    normalizedCategory,
                    pageable
            );
        }

        // All emojis paginated
        return editorAssetRepository.findByAssetTypeAndIsActiveTrueOrderBySortOrderAscIdAsc(
                "EMOJI",
                pageable
        );
    }

    /**
     * Fetch single emoji by ID or assetKey.
     */
    @Transactional(readOnly = true)
    public Optional<EditorAsset> getEmojiByIdOrKey(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        try {
            Long numericId = Long.parseLong(identifier.trim());
            Optional<EditorAsset> byId = editorAssetRepository.findById(numericId);
            if (byId.isPresent() && "EMOJI".equalsIgnoreCase(byId.get().getAssetType())) {
                return byId;
            }
        } catch (NumberFormatException ignored) {
            // Not a numeric ID, fall through to assetKey search
        }

        return editorAssetRepository.findByAssetTypeAndAssetKey("EMOJI", identifier.trim());
    }

    private String toKebabCase(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
