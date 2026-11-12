package com.themebuilder.backend.controller;

import com.themebuilder.backend.entity.Design;
import com.themebuilder.backend.service.DesignService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/designs")
public class DesignController {

    private final DesignService designService;

    public DesignController(DesignService designService) {
        this.designService = designService;
    }

    // 1. Create a new design
    @PostMapping
    public Design createDesign(@RequestBody Design design) {

        return designService.createDesign(design);
    }

    // 2. Get all designs
    // Existing endpoint preserved
    @GetMapping
    public List<Design> getAllDesigns() {

        return designService.getAllDesigns();
    }

    // 3. NEW: Get saved designs with pagination
    // URL: /api/designs/page?page=0&size=20
    @GetMapping("/page")
    public Page<Design> getPaginatedDesigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return designService.getDesignsPaginated(page, size);
    }

    // 4. Get a single design by ID
    @GetMapping("/{id}")
    public Design getDesignById(@PathVariable Long id) {

        return designService.getDesignById(id);
    }

    // 5. Update an existing design
    @PutMapping("/{id}")
    public Design updateDesign(
            @PathVariable Long id,
            @RequestBody Design design
    ) {

        return designService.updateDesign(id, design);
    }

    // 6. Delete a design
    @DeleteMapping("/{id}")
    public void deleteDesign(@PathVariable Long id) {

        designService.deleteDesign(id);
    }
}