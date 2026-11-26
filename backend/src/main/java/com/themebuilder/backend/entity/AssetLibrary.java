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
        name = "asset_library",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "asset_library_type_asset_key_unique",
                        columnNames = {"type", "asset_key"}
                )
        }
)
public class AssetLibrary extends BaseEntity {

    @Column(name = "asset_key", nullable = false, length = 200)
    private String assetKey;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "subcategory", length = 100)
    private String subcategory;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> jsonData = new HashMap<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    public AssetLibrary() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Map<String, Object> getJsonData() {
        return jsonData;
    }

    public void setJsonData(Map<String, Object> jsonData) {
        this.jsonData = jsonData;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
