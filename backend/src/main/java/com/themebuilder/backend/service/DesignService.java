package com.themebuilder.backend.service;

import com.themebuilder.backend.entity.Design;
import com.themebuilder.backend.entity.DesignElement;
import com.themebuilder.backend.repository.DesignRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DesignService {

    private final DesignRepository designRepository;

    public DesignService(DesignRepository designRepository) {
        this.designRepository = designRepository;
    }

    // 1. Create a new design
    @Transactional
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

    // 2. Fetch all designs
    // Existing method preserved
    @Transactional(readOnly = true)
    public List<Design> getAllDesigns() {

        return designRepository.findAll();
    }

    // 3. NEW: Fetch saved designs with pagination
    @Transactional(readOnly = true)
    public Page<Design> getDesignsPaginated(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100."
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        return designRepository
                .findAllByOrderByUpdatedAtDescIdDesc(pageable);
    }

    // 4. Fetch a single design by ID
    @Transactional(readOnly = true)
    public Design getDesignById(Long id) {

        return designRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Design not found")
                );
    }

    // 5. Update an existing design
    @Transactional
    public Design updateDesign(Long id, Design design) {

        Design existingDesign = designRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Design not found")
                );

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

    // 6. Delete a design
    @Transactional
    public void deleteDesign(Long id) {

        designRepository.deleteById(id);
    }
}