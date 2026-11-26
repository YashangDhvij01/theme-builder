package com.themebuilder.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DesignSummaryDto(
        Long id,
        String name,
        String backgroundColor,
        String pageSize,
        String orientation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PreviewElement> previewElements
) {
    public record PreviewElement(
            String id,
            String type,
            String text,
            Double fontSize,
            String fontFamily,
            String color,
            String fontWeight,
            String fontStyle
    ) {}
}
