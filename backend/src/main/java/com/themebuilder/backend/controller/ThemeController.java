package com.themebuilder.backend.controller;

import com.themebuilder.backend.entity.Theme;
import com.themebuilder.backend.service.ThemeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public Theme createTheme(@RequestBody Theme theme) {
        return themeService.createTheme(theme);
    }

    @GetMapping
    public List<Theme> getAllThemes() {
        return themeService.getAllThemes();
    }
}