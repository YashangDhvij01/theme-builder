package com.themebuilder.backend.dto;

import com.themebuilder.backend.entity.DesignElement;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

public record DesignDetailDto(
        Long id,
        String name,
        String backgroundColor,
        String pageSize,
        String orientation,
        JsonNode pages,
        List<DesignElement> elements,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
