package com.themebuilder.backend.controller;

import com.themebuilder.backend.dto.EmailTemplateResponse;
import com.themebuilder.backend.service.EmailTemplateService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email-templates")
@CrossOrigin(origins = "http://localhost:5173")
public class EmailTemplateController {

    private final EmailTemplateService templateService;

    public EmailTemplateController(EmailTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ResponseEntity<List<EmailTemplateResponse>> getAllTemplates(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(templateService.getAllActiveTemplates(category, search));
    }

    @GetMapping("/page")
    public ResponseEntity<Page<EmailTemplateResponse>> getTemplatesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(templateService.getTemplatesPaginated(page, size, category, search));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(templateService.getDistinctCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplateResponse> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @GetMapping("/key/{templateKey}")
    public ResponseEntity<EmailTemplateResponse> getTemplateByKey(@PathVariable String templateKey) {
        return ResponseEntity.ok(templateService.getTemplateByKey(templateKey));
    }
}
