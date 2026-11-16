package com.themebuilder.backend.controller;

import com.themebuilder.backend.entity.EditorAsset;
import com.themebuilder.backend.service.EditorAssetService;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "http://localhost:5173")
public class EditorAssetController {

    private final EditorAssetService editorAssetService;

    public EditorAssetController(EditorAssetService editorAssetService) {
        this.editorAssetService = editorAssetService;
    }

    // GET: Fetch all active assets
    // Existing endpoint preserved
    @GetMapping
    public ResponseEntity<List<EditorAsset>> getAllAssets() {

        return ResponseEntity.ok(
                editorAssetService.getAllActiveAssets()
        );
    }

    // GET: Fetch all text presets
    // Existing endpoint preserved
    // URL: /api/assets/text
    @GetMapping("/text")
    public ResponseEntity<List<EditorAsset>> getTextAssets() {

        return ResponseEntity.ok(
                editorAssetService.getAssetsByType("TEXT")
        );
    }

    // GET: Fetch all shapes
    // Existing endpoint preserved
    // URL: /api/assets/shapes
    @GetMapping("/shapes")
    public ResponseEntity<List<EditorAsset>> getShapeAssets() {

        return ResponseEntity.ok(
                editorAssetService.getAssetsByType("SHAPE")
        );
    }

    // NEW: Paginated shapes
    // URL: /api/assets/shapes/page?page=0&size=20
    @GetMapping("/shapes/page")
    public ResponseEntity<Page<EditorAsset>> getPaginatedShapes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category
    ) {

        return ResponseEntity.ok(
                editorAssetService.getAssetsByTypePaginated(
                        "SHAPE",
                        page,
                        size,
                        category
                )
        );
    }

    // NEW: Paginated text presets
    // URL: /api/assets/text/page?page=0&size=20
    @GetMapping("/text/page")
    public ResponseEntity<Page<EditorAsset>> getPaginatedTextAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category
    ) {

        return ResponseEntity.ok(
                editorAssetService.getAssetsByTypePaginated(
                        "TEXT",
                        page,
                        size,
                        category
                )
        );
    }

    // POST: Add a new text preset or shape
    @PostMapping
    public ResponseEntity<EditorAsset> addAsset(
            @RequestBody EditorAsset asset
    ) {

        EditorAsset savedAsset = editorAssetService.addAsset(asset);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedAsset);
    }

    // PUT: Update an existing asset
    @PutMapping("/{id}")
    public ResponseEntity<EditorAsset> updateAsset(
            @PathVariable Long id,
            @RequestBody EditorAsset asset
    ) {

        return ResponseEntity.ok(
                editorAssetService.updateAsset(id, asset)
        );
    }

    // PATCH: Deactivate an asset
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<EditorAsset> deactivateAsset(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                editorAssetService.deactivateAsset(id)
        );
    }

    // PATCH: Activate an asset
    @PatchMapping("/{id}/activate")
    public ResponseEntity<EditorAsset> activateAsset(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                editorAssetService.activateAsset(id)
        );
    }
}