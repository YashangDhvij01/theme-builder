
package com.themebuilder.backend.entity;

import tools.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "design")
public class Design extends BaseEntity {

    private String name;

    private String backgroundColor;

    private String pageSize;

    private String orientation;

    // Stores complete multi-page design data as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode pages;

    @OneToMany(
            mappedBy = "design",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DesignElement> elements = new ArrayList<>();

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public JsonNode getPages() {
        return pages;
    }

    public void setPages(JsonNode pages) {
        this.pages = pages;
    }

    public List<DesignElement> getElements() {
        return elements;
    }

    public void setElements(List<DesignElement> elements) {
        this.elements = elements;
    }
}
