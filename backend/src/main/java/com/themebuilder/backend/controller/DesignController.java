package com.themebuilder.backend.controller;

import com.themebuilder.backend.entity.Design;
import com.themebuilder.backend.service.DesignService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/designs")
public class DesignController {

    private final DesignService designService;

    public DesignController(DesignService designService) {
        this.designService = designService;
    }

    @PostMapping
    public Design createDesign(@RequestBody Design design) {
        return designService.createDesign(design);
    }

    @GetMapping
    public List<Design> getAllDesigns() {
        return designService.getAllDesigns();
    }

    @GetMapping("/{id}")
    public Design getDesignById(@PathVariable Long id) {
        return designService.getDesignById(id);
    }

    @PutMapping("/{id}")
    public Design updateDesign(
            @PathVariable Long id,
            @RequestBody Design design) {

        return designService.updateDesign(id, design);
    }

    @DeleteMapping("/{id}")
    public void deleteDesign(@PathVariable Long id) {
        designService.deleteDesign(id);
    }
}