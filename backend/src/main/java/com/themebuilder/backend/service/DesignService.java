package com.themebuilder.backend.service;

import com.themebuilder.backend.entity.Design;
import com.themebuilder.backend.entity.DesignElement;
import com.themebuilder.backend.repository.DesignRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DesignService {

    private final DesignRepository designRepository;

    public DesignService(DesignRepository designRepository) {
        this.designRepository = designRepository;
    }

    public Design createDesign(Design design) {

        LocalDateTime now = LocalDateTime.now();

        design.setCreatedAt(now);
        design.setUpdatedAt(now);

        if (design.getElements() != null) {

            for (DesignElement element : design.getElements()) {
                element.setDesign(design);
                element.setCreatedAt(now);
                element.setUpdatedAt(now);
            }
        }

        return designRepository.save(design);
    }

    public List<Design> getAllDesigns() {
        return designRepository.findAll();
    }

    public Design getDesignById(Long id) {

        return designRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Design not found"));
    }

    public Design updateDesign(Long id, Design design) {

        Design existingDesign = designRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Design not found"));

        existingDesign.setName(design.getName());
        existingDesign.setBackgroundColor(design.getBackgroundColor());

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

    public void deleteDesign(Long id) {
        designRepository.deleteById(id);
    }
}