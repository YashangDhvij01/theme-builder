package com.themebuilder.backend.service;

import com.themebuilder.backend.entity.DesignElement;
import com.themebuilder.backend.repository.DesignElementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DesignElementService {

    private final DesignElementRepository designElementRepository;

    public DesignElementService(DesignElementRepository designElementRepository) {
        this.designElementRepository = designElementRepository;
    }

    public DesignElement createElement(DesignElement element) {

        LocalDateTime now = LocalDateTime.now();

        element.setCreatedAt(now);
        element.setUpdatedAt(now);

        return designElementRepository.save(element);
    }

    public List<DesignElement> getAllElements() {
        return designElementRepository.findAll();
    }

    public DesignElement updateElement(Long id, DesignElement element) {

        DesignElement existingElement = designElementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Element not found"));

        existingElement.setType(element.getType());
        existingElement.setX(element.getX());
        existingElement.setY(element.getY());
        existingElement.setWidth(element.getWidth());
        existingElement.setHeight(element.getHeight());
        existingElement.setScaleX(element.getScaleX());
        existingElement.setScaleY(element.getScaleY());
        existingElement.setAngle(element.getAngle());
        existingElement.setOpacity(element.getOpacity());
        existingElement.setText(element.getText());
        existingElement.setFontSize(element.getFontSize());
        existingElement.setFontFamily(element.getFontFamily());
        existingElement.setColor(element.getColor());
        existingElement.setBackgroundColor(element.getBackgroundColor());
        existingElement.setFontWeight(element.getFontWeight());
        existingElement.setFontStyle(element.getFontStyle());
        existingElement.setSrc(element.getSrc());
        existingElement.setLineHeight(element.getLineHeight());
        existingElement.setCharSpacing(element.getCharSpacing());
        existingElement.setTextAlign(element.getTextAlign());
        existingElement.setStrokeColor(element.getStrokeColor());
        existingElement.setStrokeWidth(element.getStrokeWidth());
        existingElement.setLineDash(element.getLineDash());
        existingElement.setShapeSides(element.getShapeSides());

        existingElement.setUpdatedAt(LocalDateTime.now());

        return designElementRepository.save(existingElement);
    }

    public void deleteElement(Long id) {
        designElementRepository.deleteById(id);
    }
}