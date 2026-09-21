package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
}