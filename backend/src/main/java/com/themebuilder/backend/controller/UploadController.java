package com.themebuilder.backend.controller;

import com.themebuilder.backend.entity.UserUpload;
import com.themebuilder.backend.repository.DesignElementRepository;
import com.themebuilder.backend.repository.UserUploadRepository;
import com.themebuilder.backend.service.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@CrossOrigin(origins = "http://localhost:5173")
public class UploadController {

    private final CloudinaryService cloudinaryService;
    private final UserUploadRepository userUploadRepository;
    private final DesignElementRepository designElementRepository;

    public UploadController(
            CloudinaryService cloudinaryService,
            UserUploadRepository userUploadRepository,
            DesignElementRepository designElementRepository
    ) {
        this.cloudinaryService = cloudinaryService;
        this.userUploadRepository = userUploadRepository;
        this.designElementRepository = designElementRepository;
    }

    /**
     * 1. POST /api/uploads/image
     * Upload an image file to Cloudinary and record its metadata in PostgreSQL.
     */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId
    ) {
        try {
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, null);

            LocalDateTime now = LocalDateTime.now();

            UserUpload userUpload = new UserUpload();
            userUpload.setUserId(userId);
            userUpload.setPublicId(String.valueOf(uploadResult.get("publicId")));
            userUpload.setSecureUrl(String.valueOf(uploadResult.get("secureUrl")));
            userUpload.setOriginalFilename(String.valueOf(uploadResult.get("originalFilename")));
            userUpload.setFormat(uploadResult.get("format") != null ? String.valueOf(uploadResult.get("format")) : null);
            userUpload.setWidth((Integer) uploadResult.get("width"));
            userUpload.setHeight((Integer) uploadResult.get("height"));
            userUpload.setBytes((Long) uploadResult.get("bytes"));
            userUpload.setCreatedAt(now);
            userUpload.setUpdatedAt(now);

            UserUpload saved = userUploadRepository.save(userUpload);

            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("secureUrl", saved.getSecureUrl());
            response.put("publicId", saved.getPublicId());
            response.put("format", saved.getFormat());
            response.put("width", saved.getWidth());
            response.put("height", saved.getHeight());
            response.put("bytes", saved.getBytes());
            response.put("originalFilename", saved.getOriginalFilename());
            response.put("createdAt", saved.getCreatedAt());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "CONFIGURATION_ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        } catch (IllegalArgumentException | SecurityException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "VALIDATION_ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "UPLOAD_FAILED");
            error.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 2. GET /api/uploads
     * Fetch user's uploaded images gallery, ordered by newest first.
     */
    @GetMapping
    public ResponseEntity<?> getUploads(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));

        if (userId != null && !userId.isBlank()) {
            Page<UserUpload> userPage = userUploadRepository.findByUserIdOrderByCreatedAtDesc(userId.trim(), pageable);
            return ResponseEntity.ok(userPage);
        }

        Page<UserUpload> allPage = userUploadRepository.findAllByOrderByCreatedAtDesc(pageable);
        return ResponseEntity.ok(allPage);
    }

    /**
     * 3. DELETE /api/uploads/{id}
     * Safely delete an uploaded image from Cloudinary and PostgreSQL.
     * Prevents deletion if the image is referenced by an active saved design.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUpload(
            @PathVariable Long id,
            @RequestParam(value = "force", defaultValue = "false") boolean force
    ) {
        return userUploadRepository.findById(id).map(upload -> {
            // Check if actively referenced in any saved designs
            if (!force && upload.getSecureUrl() != null) {
                // Check if any design element references this URL or public ID
                boolean inUse = designElementRepository.existsBySrcContaining(upload.getSecureUrl());
                if (!inUse && upload.getPublicId() != null) {
                    inUse = designElementRepository.existsBySrcContaining(upload.getPublicId());
                }

                if (inUse) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "RESOURCE_IN_USE");
                    error.put("message", "Cannot delete image because it is referenced in an active saved design.");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
                }
            }

            // Remove from Cloudinary
            if (upload.getPublicId() != null && !upload.getPublicId().isBlank()) {
                cloudinaryService.deleteImage(upload.getPublicId());
            }

            // Remove from Database
            userUploadRepository.delete(upload);

            Map<String, Object> response = new HashMap<>();
            response.put("deleted", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
