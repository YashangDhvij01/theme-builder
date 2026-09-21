package com.themebuilder.backend.service;

import com.themebuilder.backend.entity.Theme;
import com.themebuilder.backend.repository.ThemeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme createTheme(Theme theme) {
        return themeRepository.save(theme);
    }

    public List<Theme> getAllThemes() {
        return themeRepository.findAll();
    }
}