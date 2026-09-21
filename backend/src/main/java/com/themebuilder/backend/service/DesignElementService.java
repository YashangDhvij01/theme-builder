package com.themebuilder.backend.service;

import com.themebuilder.backend.entity.DesignElement;
import com.themebuilder.backend.repository.DesignElementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DesignElementService {

    private final DesignElementRepository designElementRepository;

    public DesignElementService(DesignElementRepository designElementRepository) {
        this.designElementRepository = designElementRepository;
    }

    public DesignElement createElement(DesignElement element) {
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
        existingElement.setText(element.getText());
        existingElement.setFontSize(element.getFontSize());
        existingElement.setFontFamily(element.getFontFamily());
        existingElement.setColor(element.getColor());
        existingElement.setBackgroundColor(element.getBackgroundColor());
        existingElement.setFontWeight(element.getFontWeight());
        existingElement.setFontStyle(element.getFontStyle());

        return designElementRepository.save(existingElement);
    }

    public void deleteElement(Long id) {
        designElementRepository.deleteById(id);
    }
}