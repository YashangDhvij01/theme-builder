package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.UserUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserUploadRepository extends JpaRepository<UserUpload, Long> {

    List<UserUpload> findAllByOrderByCreatedAtDesc();

    Page<UserUpload> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<UserUpload> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<UserUpload> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<UserUpload> findByPublicId(String publicId);

    boolean existsBySecureUrl(String secureUrl);
}
