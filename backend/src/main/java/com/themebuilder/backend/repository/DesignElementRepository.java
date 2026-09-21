package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.DesignElement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignElementRepository extends JpaRepository<DesignElement, Long> {
}