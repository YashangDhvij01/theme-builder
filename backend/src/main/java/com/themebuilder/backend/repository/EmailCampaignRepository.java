package com.themebuilder.backend.repository;

import com.themebuilder.backend.entity.EmailCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {

    List<EmailCampaign> findAllByOrderByUpdatedAtDescIdDesc();

    Page<EmailCampaign> findAllByOrderByUpdatedAtDescIdDesc(Pageable pageable);

    Page<EmailCampaign> findByNameContainingIgnoreCaseOrderByUpdatedAtDescIdDesc(String name, Pageable pageable);
}
