package com.themebuilder.backend.controller;

import com.themebuilder.backend.entity.EditorAsset;
import com.themebuilder.backend.service.GraphicService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graphics")
@CrossOrigin(origins = "http://localhost:5173")
public class GraphicController {

    private final GraphicService graphicService;

    public GraphicController(GraphicService graphicService) {
        this.graphicService = graphicService;
    }

    /**
     * 1. GET /api/graphics/categories
     * Returns aggregated category summaries with counts, iconKey and preview SVG.
     * Supports optional type query parameter: "static" or "animated".
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories(
            @RequestParam(required = false) String type
    ) {
        return ResponseEntity.ok(graphicService.getCategories(type));
    }

    /**
     * 2. GET /api/graphics/recommended
     * Returns top recommended graphics for the Canva-style top rail.
     * Supports optional type query parameter: "static" or "animated".
     */
    @GetMapping("/recommended")
    public ResponseEntity<List<EditorAsset>> getRecommended(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(graphicService.getRecommended(type, limit));
    }

    /**
     * 3. GET /api/graphics
     * Query and filter graphics with pagination.
     * Supports categoryId, search, type (static | animated), recommended, page, size.
     */
    @GetMapping
    public ResponseEntity<Page<EditorAsset>> getGraphics(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                graphicService.getGraphics(categoryId, search, type, recommended, page, size)
        );
    }

    /**
     * 4. GET /api/graphics/{identifier}
     * Fetch single graphic by numeric ID or string assetKey.
     */
    @GetMapping("/{identifier}")
    public ResponseEntity<EditorAsset> getGraphicById(
            @PathVariable String identifier
    ) {
        return graphicService.getGraphicByIdOrKey(identifier)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
