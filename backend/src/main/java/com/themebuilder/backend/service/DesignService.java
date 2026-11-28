
package com.themebuilder.backend.service;

import com.themebuilder.backend.dto.DesignDetailDto;
import com.themebuilder.backend.dto.DesignSummaryDto;
import com.themebuilder.backend.entity.Design;
import com.themebuilder.backend.entity.DesignElement;
import com.themebuilder.backend.repository.DesignRepository;
import tools.jackson.databind.JsonNode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DesignService {

    private final DesignRepository designRepository;

    public DesignService(DesignRepository designRepository) {
        this.designRepository = designRepository;
    }

    @Transactional
    public Design createDesign(Design design) {

        LocalDateTime now = LocalDateTime.now();

        design.setCreatedAt(now);
        design.setUpdatedAt(now);

        prepareElements(design, now);

        return designRepository.save(design);
    }

    private void prepareElements(Design design, LocalDateTime now) {

        if (design.getElements() == null) {
            design.setElements(new ArrayList<>());
            return;
        }

        for (DesignElement element : design.getElements()) {
            element.setDesign(design);
            element.setCreatedAt(now);
            element.setUpdatedAt(now);
        }
    }

    @Transactional(readOnly = true)
    public List<Design> getAllDesigns() {
        return designRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<DesignSummaryDto> getDesignsPaginated(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative.");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100.");
        }

        Pageable pageable = PageRequest.of(page, size);

        return designRepository
                .findDesignSummaryRows(pageable)
                .map(this::toSummaryDto);
    }

    private DesignSummaryDto toSummaryDto(Object[] row) {
        JsonNode pagesNode = (JsonNode) row[7];
        return new DesignSummaryDto(
                (Long) row[0],
                (String) row[1],
                (String) row[2],
                (String) row[3],
                (String) row[4],
                (LocalDateTime) row[5],
                (LocalDateTime) row[6],
                extractPreviewElements(pagesNode),
                extractFirstPageElements(pagesNode)
        );
    }

    private JsonNode extractFirstPageElements(JsonNode pagesNode) {
        if (pagesNode == null || !pagesNode.isArray() || pagesNode.isEmpty()) {
            return null;
        }
        JsonNode elementsNode = pagesNode.get(0).get("elements");
        return (elementsNode != null && elementsNode.isArray()) ? elementsNode : null;
    }

    private List<DesignSummaryDto.PreviewElement> extractPreviewElements(JsonNode pagesNode) {
        List<DesignSummaryDto.PreviewElement> previews = new ArrayList<>(3);
        if (pagesNode == null || !pagesNode.isArray() || pagesNode.isEmpty()) {
            return previews;
        }
        JsonNode elementsNode = pagesNode.get(0).get("elements");
        if (elementsNode == null || !elementsNode.isArray()) {
            return previews;
        }
        for (JsonNode el : elementsNode) {
            String type = el.path("type").asText("");
            if (!"heading".equals(type) && !"subheading".equals(type) && !"text".equals(type)) {
                continue;
            }
            previews.add(new DesignSummaryDto.PreviewElement(
                    el.path("id").asText("preview-" + previews.size()),
                    type,
                    el.path("text").asText("Text"),
                    el.hasNonNull("fontSize") ? el.get("fontSize").asDouble() : 18.0,
                    el.path("fontFamily").asText("Arial, sans-serif"),
                    el.path("color").asText("#111827"),
                    el.path("fontWeight").asText("normal"),
                    el.path("fontStyle").asText("normal")
            ));
            if (previews.size() == 3) {
                break;
            }
        }
        return previews;
    }

    @Transactional(readOnly = true)
    public DesignDetailDto getDesignById(Long id) {
        Design design = designRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Design not found"));

        JsonNode pages = design.getPages();
        boolean hasPages = pages != null && pages.isArray() && !pages.isEmpty();
        List<DesignElement> elements = hasPages
                ? List.of()
                : new ArrayList<>(design.getElements());

        return new DesignDetailDto(
                design.getId(),
                design.getName(),
                design.getBackgroundColor(),
                design.getPageSize(),
                design.getOrientation(),
                pages,
                elements,
                design.getCreatedAt(),
                design.getUpdatedAt()
        );
    }

    @Transactional
    public Design updateDesign(Long id, Design design) {

        Design existingDesign = designRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Design not found"));

        existingDesign.setName(design.getName());
        existingDesign.setBackgroundColor(
                design.getBackgroundColor());

        existingDesign.setPageSize(design.getPageSize());
        existingDesign.setOrientation(design.getOrientation());

        // Update all pages JSON.
        existingDesign.setPages(design.getPages());

        // Preserve existing relational elements for legacy support.
        existingDesign.getElements().clear();

        LocalDateTime now = LocalDateTime.now();

        if (design.getElements() != null) {

            for (DesignElement element : design.getElements()) {

                element.setDesign(existingDesign);
                element.setCreatedAt(now);
                element.setUpdatedAt(now);

                existingDesign.getElements().add(element);
            }
        }

        existingDesign.setUpdatedAt(now);

        return designRepository.save(existingDesign);
    }

    @Transactional
    public void deleteDesign(Long id) {
        designRepository.deleteById(id);
    }
}
