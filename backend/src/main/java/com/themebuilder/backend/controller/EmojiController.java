package com.themebuilder.backend.controller;

import com.themebuilder.backend.entity.EditorAsset;
import com.themebuilder.backend.service.EmojiService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emojis")
@CrossOrigin(origins = "http://localhost:5173")
public class EmojiController {

    private final EmojiService emojiService;

    public EmojiController(EmojiService emojiService) {
        this.emojiService = emojiService;
    }

    /**
     * 1. GET /api/emojis/categories
     * Returns aggregated category summaries with counts, categoryId, and preview SVG.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        return ResponseEntity.ok(emojiService.getCategories());
    }

    /**
     * 2. GET /api/emojis/recommended
     * Returns top recommended emojis for Canva-style rail.
     */
    @GetMapping("/recommended")
    public ResponseEntity<List<EditorAsset>> getRecommended(
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(emojiService.getRecommended(limit));
    }

    /**
     * 3. GET /api/emojis
     * Query and filter emojis with backend database search and pagination.
     * Supports category, search, recommended, page, size.
     */
    @GetMapping
    public ResponseEntity<Page<EditorAsset>> getEmojis(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                emojiService.getEmojis(category, search, recommended, page, size)
        );
    }

    /**
     * 4. GET /api/emojis/{identifier}
     * Fetch single emoji by numeric ID or string assetKey.
     */
    @GetMapping("/{identifier}")
    public ResponseEntity<EditorAsset> getEmojiById(
            @PathVariable String identifier
    ) {
        return emojiService.getEmojiByIdOrKey(identifier)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
