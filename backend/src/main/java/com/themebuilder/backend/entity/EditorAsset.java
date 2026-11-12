
package com.themebuilder.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(
        name = "editor_assets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_editor_assets_type_key",
                        columnNames = {"asset_type", "asset_key"}
                )
        }
)
public class EditorAsset extends BaseEntity {

    @Column(name = "asset_key", nullable = false, length = 150)
    private String assetKey;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "asset_type", nullable = false, length = 20)
    private String assetType;

    @Column(length = 100)
    private String category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "asset_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> assetData = new HashMap<>();

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public EditorAsset() {
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

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
