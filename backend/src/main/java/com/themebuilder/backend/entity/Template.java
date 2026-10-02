package com.themebuilder.backend.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "template")
public class Template extends BaseEntity {

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String backgroundColor;

    @OneToMany(
            mappedBy = "template",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DesignElement> elements = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public List<DesignElement> getElements() {
        return elements;
    }

    public void setElements(List<DesignElement> elements) {
        this.elements = elements;
    }
}