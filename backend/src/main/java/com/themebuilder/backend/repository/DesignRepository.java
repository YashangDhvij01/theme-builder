package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.Design;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignRepository extends JpaRepository<Design, Long> {

    // Existing CRUD methods inherited from JpaRepository remain available.

    // Fetch saved designs with pagination.
    // Latest updated designs appear first.
    Page<Design> findAllByOrderByUpdatedAtDescIdDesc(Pageable pageable);
}