package com.themebuilder.backend.dto;

import com.themebuilder.backend.entity.AssetLibrary;

import java.util.Map;

/**
 * API response DTO for /api/theme-elements/{kind} backed by AssetLibrary.
 */
public class ThemeElementResponse {

    private Long id;
    private String assetKey;
    private String name;
    private String assetType;
    private String category;
    private Map<String, Object> assetData;
    private Integer sortOrder;
    private Boolean isActive;

    public static ThemeElementResponse fromEntity(AssetLibrary asset) {
        ThemeElementResponse dto = new ThemeElementResponse();
        dto.setId(asset.getId());
        dto.setAssetKey(asset.getAssetKey());
        dto.setName(asset.getName());
        dto.setAssetType(asset.getType());
        dto.setCategory(asset.getCategory());
        dto.setAssetData(asset.getJsonData());
        dto.setSortOrder(asset.getSortOrder());
        dto.setIsActive(asset.getIsActive());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetKey() {
        return assetKey;
    }

    public void setAssetKey(String assetKey) {
        this.assetKey = assetKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Map<String, Object> getAssetData() {
        return assetData;
    }

    public void setAssetData(Map<String, Object> assetData) {
        this.assetData = assetData;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
