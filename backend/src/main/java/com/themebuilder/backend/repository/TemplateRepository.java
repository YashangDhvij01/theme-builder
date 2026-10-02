package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Long> {
}