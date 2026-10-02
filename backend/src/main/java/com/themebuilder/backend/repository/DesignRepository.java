package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.Design;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignRepository extends JpaRepository<Design, Long> {
}