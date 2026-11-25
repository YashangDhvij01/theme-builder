package com.themebuilder.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "user_uploads",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_uploads_public_id", columnNames = {"public_id"})
        },
        indexes = {
                @Index(name = "idx_user_uploads_public_id", columnList = "public_id"),
                @Index(name = "idx_user_uploads_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_user_uploads_created", columnList = "created_at")
        }
)
public class UserUpload extends BaseEntity {

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "public_id", nullable = false, length = 255)
    private String publicId;

    @Column(name = "secure_url", nullable = false, length = 1000)
    private String secureUrl;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(length = 20)
    private String format;

    private Integer width;

    private Integer height;

    private Long bytes;

    public UserUpload() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }
}
