package com.themebuilder.backend.controller;

import com.themebuilder.backend.dto.AssetLibraryRequest;
// import com.themebuilder.backend.dto.BulkAssetImportResult;
import com.themebuilder.backend.entity.AssetLibrary;
import com.themebuilder.backend.service.AssetLibraryAdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/assets")
@CrossOrigin(origins = "http://localhost:5173")
public class AssetLibraryAdminController {

    private final AssetLibraryAdminService adminService;

    @Value("${app.admin.key:}")
    private String configuredAdminKey;

    public AssetLibraryAdminController(AssetLibraryAdminService adminService) {
        this.adminService = adminService;
    }

    private boolean isAuthorized(String headerKey) {
        if (configuredAdminKey == null || configuredAdminKey.isBlank()) {
            return true;
        }
        return configuredAdminKey.equals(headerKey);
    }

    @GetMapping
    public ResponseEntity<?> listAssets(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false, defaultValue = "false") Boolean isDeleted,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (!isAuthorized(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing X-Admin-Key"));
        }
        Page<AssetLibrary> result = adminService.getAdminAssets(
                type, category, isActive, isDeleted, search, page, size
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey
    ) {
        if (!isAuthorized(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing X-Admin-Key"));
        }
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestParam(required = false) String type
    ) {
        if (!isAuthorized(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing X-Admin-Key"));
        }
        return ResponseEntity.ok(adminService.getCategories(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @PathVariable Long id
    ) {
        if (!isAuthorized(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing X-Admin-Key"));
        }
        try {
            return ResponseEntity.ok(adminService.getById(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createAsset(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestBody AssetLibraryRequest request
    ) {
        if (!isAuthorized(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing X-Admin-Key"));
        }
        try {
            AssetLibrary created = adminService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    /*
    @PostMapping("/bulk-import")
    public ResponseEntity<?> bulkImport(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestBody List<AssetLibraryRequest> requests
    ) {
        if (!isAuthorized(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing X-Admin-Key"));
        }
        BulkAssetImportResult result = adminService.bulkImport(requests);
        return ResponseEntity.ok(result);
    }
    */

    @PatchMapping("/reorder")
    public ResponseEntity<?> reorderAssets(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestBody Map<String, Object> body
    ) {
        if (!isAuthorized(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing X-Admin-Key"));
        }
        try {
            Object rawIds = body.get("orderedIds");
            if (!(rawIds instanceof List<?> idList) || idList.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "orderedIds must be a non-empty list."));
            }
            List<Long> orderedIds = idList.stream()
                    .map(val -> Long.valueOf(String.valueOf(val)))
                    .toList();
            int startSortOrder = body.get("startSortOrder") instanceof Number n
                    ? Math.max(1, n.intValue())
                    : 1;
            adminService.reorderAssets(orderedIds, startSortOrder);
            return ResponseEntity.ok(Map.of("updated", orderedIds.size()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAsset(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @PathVariable Long id,
            @RequestBody AssetLibraryRequest request
    ) {
        if (!isAuthorized(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing X-Admin-Key"));
        }
        try {
            return ResponseEntity.ok(adminService.update(id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @PathVariable Long id,
            @RequestParam boolean isActive
    ) {
        if (!isAuthorized(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing X-Admin-Key"));
        }
        try {
            return ResponseEntity.ok(adminService.toggleActive(id, isActive));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteOrRestore(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean restore
    ) {
        if (!isAuthorized(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing X-Admin-Key"));
        }
        try {
            return ResponseEntity.ok(adminService.setDeleted(id, !restore));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
