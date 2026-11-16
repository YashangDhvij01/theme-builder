package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.Template;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    // Find template using original frontend slug
    Optional<Template> findByTemplateKey(String templateKey);

    // Check duplicate template slug
    boolean existsByTemplateKey(String templateKey);

    // Fetch templates with pagination
    Page<Template> findAllByOrderByIdAsc(Pageable pageable);
}