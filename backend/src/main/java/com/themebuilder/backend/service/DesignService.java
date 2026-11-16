
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

    @Transactional(readOnly = true)
    public Design getDesignById(Long id) {

        return designRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Design not found")
                );
    }

    @Transactional
    public Design updateDesign(Long id, Design design) {

        Design existingDesign = designRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Design not found")
                );

        existingDesign.setName(design.getName());
        existingDesign.setBackgroundColor(
                design.getBackgroundColor()
        );

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
