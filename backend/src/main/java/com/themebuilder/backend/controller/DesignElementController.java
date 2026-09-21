package com.themebuilder.backend.controller;

import com.themebuilder.backend.entity.DesignElement;
import com.themebuilder.backend.service.DesignElementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/elements")
public class DesignElementController {

    private final DesignElementService designElementService;

    public DesignElementController(DesignElementService designElementService) {
        this.designElementService = designElementService;
    }

    @PostMapping
    public DesignElement createElement(@RequestBody DesignElement element) {
        return designElementService.createElement(element);
    }

    @GetMapping
    public List<DesignElement> getAllElements() {
        return designElementService.getAllElements();
    }

    @PutMapping("/{id}")
    public DesignElement updateElement(
            @PathVariable Long id,
            @RequestBody DesignElement element) {

        return designElementService.updateElement(id, element);
    }

    @DeleteMapping("/{id}")
    public void deleteElement(@PathVariable Long id) {
        designElementService.deleteElement(id);
    }
}