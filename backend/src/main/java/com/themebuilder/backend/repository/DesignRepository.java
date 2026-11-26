package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.Design;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DesignRepository extends JpaRepository<Design, Long> {

    // Fetch only summary columns + pages JSONB without triggering lazy N+1 queries on design_element.
    @Query("SELECT d.id, d.name, d.backgroundColor, d.pageSize, d.orientation, d.createdAt, d.updatedAt, d.pages " +
           "FROM Design d ORDER BY d.updatedAt DESC, d.id DESC")
    Page<Object[]> findDesignSummaryRows(Pageable pageable);

    @EntityGraph(attributePaths = "elements")
    Optional<Design> findWithElementsById(Long id);
}