
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

    // 1. GET: Fetch all active assets.
    // Existing endpoint preserved.
    // URL: /api/assets
    @GetMapping
    public ResponseEntity<List<EditorAsset>> getAllAssets() {

        return ResponseEntity.ok(
                editorAssetService.getAllActiveAssets()
        );
    }

    // 2. GET: Fetch all text presets.
    // Existing endpoint preserved.
    // URL: /api/assets/text
    @GetMapping("/text")
    public ResponseEntity<List<EditorAsset>> getTextAssets() {

        return ResponseEntity.ok(
                editorAssetService.getAssetsByType("TEXT")
        );
    }

    // 3. GET: Fetch all shapes.
    // Existing endpoint preserved.
    // URL: /api/assets/shapes
    @GetMapping("/shapes")
    public ResponseEntity<List<EditorAsset>> getShapeAssets() {

        return ResponseEntity.ok(
                editorAssetService.getAssetsByType("SHAPE")
        );
    }

    // 4. Paginated shapes.
    // Default page size: 10
    // URL: /api/assets/shapes/page?page=0&size=10
    @GetMapping("/shapes/page")
    public ResponseEntity<Page<EditorAsset>> getPaginatedShapes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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

    // 5. Paginated text presets.
    // Default page size: 10
    // URL: /api/assets/text/page?page=0&size=10
    @GetMapping("/text/page")
    public ResponseEntity<Page<EditorAsset>> getPaginatedTextAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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

    // 6. Generic paginated assets endpoint.
    // Supports SHAPE, TEXT, EMOJI, PHOTO, ANIMATION and BORDER.
    // Default page size: 10
    //
    // Examples:
    // /api/assets/EMOJI/page?page=0&size=10
    // /api/assets/PHOTO/page?page=0&size=10&category=Nature
    // /api/assets/ANIMATION/page?page=1&size=10
    // /api/assets/BORDER/page?page=0&size=10&category=Floral
    //
    // Existing SHAPE and TEXT routes above are preserved.
    @GetMapping("/{assetType}/page")
    public ResponseEntity<Page<EditorAsset>> getPaginatedAssetsByType(
            @PathVariable String assetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category
    ) {

        return ResponseEntity.ok(
                editorAssetService.getAssetsByTypePaginated(
                        assetType,
                        page,
                        size,
                        category
                )
        );
    }

    // 7. POST: Add a new asset.
    // Supports all six asset types.
    @PostMapping
    public ResponseEntity<EditorAsset> addAsset(
            @RequestBody EditorAsset asset
    ) {

        EditorAsset savedAsset = editorAssetService.addAsset(asset);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedAsset);
    }

    // 8. PUT: Update an existing asset.
    @PutMapping("/{id}")
    public ResponseEntity<EditorAsset> updateAsset(
            @PathVariable Long id,
            @RequestBody EditorAsset asset
    ) {

        return ResponseEntity.ok(
                editorAssetService.updateAsset(id, asset)
        );
    }

    // 9. PATCH: Deactivate an asset.
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<EditorAsset> deactivateAsset(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                editorAssetService.deactivateAsset(id)
        );
    }

    // 10. PATCH: Activate an asset.
    @PatchMapping("/{id}/activate")
    public ResponseEntity<EditorAsset> activateAsset(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                editorAssetService.activateAsset(id)
        );
    }
}
