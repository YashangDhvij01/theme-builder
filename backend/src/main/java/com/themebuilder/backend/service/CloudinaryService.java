package com.themebuilder.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.themebuilder.backend.config.CloudinaryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final CloudinaryConfig cloudinaryConfig;

    @Value("${cloudinary.folder:theme-builder/user-uploads}")
    private String baseFolder;

    @Value("${cloudinary.max-file-size:10485760}") // 10 MB default
    private long maxFileSize;

    // Magic bytes for binary image formats
    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF_MAGIC = new byte[]{'R', 'I', 'F', 'F'};
    private static final byte[] WEBP_MAGIC = new byte[]{'W', 'E', 'B', 'P'};

    // Suspicious pattern detector for SVG uploads
    private static final Pattern DANGEROUS_SVG_PATTERN = Pattern.compile(
            "(<\\s*script|javascript\\s*:|data\\s*:\\s*text/html|on\\w+\\s*=|xlink:href\\s*=\\s*[\"']?\\s*javascript|<\\s*iframe|<\\s*object|<\\s*embed)",
            Pattern.CASE_INSENSITIVE
    );

    public CloudinaryService(Cloudinary cloudinary, CloudinaryConfig cloudinaryConfig) {
        this.cloudinary = cloudinary;
        this.cloudinaryConfig = cloudinaryConfig;
    }

    /**
     * Upload an image to Cloudinary with validation and optimization.
     */
    public Map<String, Object> uploadImage(MultipartFile file, String subFolder) throws IOException {
        if (!cloudinaryConfig.isConfigured()) {
            throw new IllegalStateException(
                    "Cloudinary is not configured. Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, " +
                    "and CLOUDINARY_API_SECRET in application.properties or environment variables."
            );
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file.");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    "File size (" + (file.getSize() / 1024 / 1024) + " MB) exceeds maximum allowed size of " +
                    (maxFileSize / 1024 / 1024) + " MB."
            );
        }

        // Validate content format using magic bytes
        String detectedFormat = validateAndDetectImageFormat(file);

        String targetFolder = (baseFolder != null && !baseFolder.isBlank())
                ? (subFolder != null && !subFolder.isBlank() ? baseFolder + "/" + subFolder.trim() : baseFolder)
                : "theme-builder/user-uploads";

        Map<String, Object> options = new HashMap<>();
        options.put("folder", targetFolder);
        options.put("resource_type", "image");
        options.put("overwrite", false);

        // Upload to Cloudinary
        Map<?, ?> rawResult = cloudinary.uploader().upload(file.getBytes(), options);

        String publicId = String.valueOf(rawResult.get("public_id"));
        String rawSecureUrl = String.valueOf(rawResult.get("secure_url"));

        // Insert automatic format & quality optimization (f_auto,q_auto) into the CDN delivery URL
        String optimizedUrl = applyCdnOptimizations(rawSecureUrl);

        Integer width = rawResult.get("width") instanceof Number ? ((Number) rawResult.get("width")).intValue() : null;
        Integer height = rawResult.get("height") instanceof Number ? ((Number) rawResult.get("height")).intValue() : null;
        Long bytes = rawResult.get("bytes") instanceof Number ? ((Number) rawResult.get("bytes")).longValue() : file.getSize();
        String format = rawResult.get("format") != null ? String.valueOf(rawResult.get("format")) : detectedFormat;

        Map<String, Object> response = new HashMap<>();
        response.put("publicId", publicId);
        response.put("secureUrl", optimizedUrl);
        response.put("format", format);
        response.put("width", width);
        response.put("height", height);
        response.put("bytes", bytes);
        response.put("originalFilename", file.getOriginalFilename());

        return response;
    }

    /**
     * Delete an image from Cloudinary by its public ID.
     */
    public boolean deleteImage(String publicId) {
        if (!cloudinaryConfig.isConfigured() || publicId == null || publicId.isBlank()) {
            return false;
        }

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equalsIgnoreCase(String.valueOf(result.get("result")));
        } catch (Exception e) {
            System.err.println("Cloudinary delete failed for publicId: " + publicId + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Inspect file bytes to verify actual image format and reject malicious content.
     */
    private String validateAndDetectImageFormat(MultipartFile file) throws IOException {
        byte[] header = new byte[16];
        try (InputStream in = file.getInputStream()) {
            int read = in.read(header);
            if (read < 3) {
                throw new IllegalArgumentException("Invalid file: unable to read header bytes.");
            }
        }

        // 1. JPEG
        if (header[0] == JPEG_MAGIC[0] && header[1] == JPEG_MAGIC[1] && header[2] == JPEG_MAGIC[2]) {
            return "jpg";
        }

        // 2. PNG
        if (header.length >= 8) {
            boolean isPng = true;
            for (int i = 0; i < PNG_MAGIC.length; i++) {
                if (header[i] != PNG_MAGIC[i]) {
                    isPng = false;
                    break;
                }
            }
            if (isPng) return "png";
        }

        // 3. WEBP (RIFF .... WEBP)
        if (header.length >= 12) {
            boolean isRiff = header[0] == RIFF_MAGIC[0] && header[1] == RIFF_MAGIC[1]
                    && header[2] == RIFF_MAGIC[2] && header[3] == RIFF_MAGIC[3];
            boolean isWebp = header[8] == WEBP_MAGIC[0] && header[9] == WEBP_MAGIC[1]
                    && header[10] == WEBP_MAGIC[2] && header[11] == WEBP_MAGIC[3];
            if (isRiff && isWebp) return "webp";
        }

        // 4. SVG (Text format validation + security sanitization)
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        if ("image/svg+xml".equalsIgnoreCase(contentType) || filename.endsWith(".svg")) {
            byte[] fileBytes = file.getBytes();
            String svgContent = new String(fileBytes, StandardCharsets.UTF_8);
            String trimmed = svgContent.trim();
            if (!trimmed.startsWith("<?xml") && !trimmed.startsWith("<svg") && !trimmed.startsWith("<!DOCTYPE svg")) {
                throw new IllegalArgumentException("Malformed SVG image format.");
            }
            if (DANGEROUS_SVG_PATTERN.matcher(svgContent).find()) {
                throw new SecurityException("SVG file contains unsafe scripts or embedded objects and cannot be accepted.");
            }
            return "svg";
        }

        throw new IllegalArgumentException("Unsupported image format. Allowed formats: JPEG, PNG, WEBP, and sanitized SVG.");
    }

    /**
     * Apply automatic modern format (`f_auto`) and quality optimization (`q_auto`) to Cloudinary URLs.
     */
    private String applyCdnOptimizations(String secureUrl) {
        if (secureUrl == null) return null;
        if (!secureUrl.contains("/image/upload/")) {
            return secureUrl;
        }

        if (secureUrl.contains("/f_auto") || secureUrl.contains("q_auto")) {
            return secureUrl;
        }

        // Insert /f_auto,q_auto/ after /image/upload/
        return secureUrl.replace("/image/upload/", "/image/upload/f_auto,q_auto/");
    }
}
