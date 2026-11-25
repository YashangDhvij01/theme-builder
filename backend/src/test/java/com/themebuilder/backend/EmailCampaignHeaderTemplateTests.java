package com.themebuilder.backend;

import com.themebuilder.backend.config.EmailCampaignHeaderTemplateDataInitializer;
import com.themebuilder.backend.dto.EmailCampaignHeaderTemplateResponse;
import com.themebuilder.backend.exception.ResourceNotFoundException;
import com.themebuilder.backend.repository.EmailCampaignHeaderTemplateRepository;
import com.themebuilder.backend.service.EmailCampaignHeaderTemplateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmailCampaignHeaderTemplateTests {

    @Autowired
    private EmailCampaignHeaderTemplateService headerTemplateService;

    @Autowired
    private EmailCampaignHeaderTemplateRepository headerTemplateRepository;

    @Autowired
    private EmailCampaignHeaderTemplateDataInitializer dataInitializer;

    @Test
    @DisplayName("Should seed exact 10 Canva header templates in display order")
    void testSeededTenHeaderTemplates() {
        List<EmailCampaignHeaderTemplateResponse> headers =
                headerTemplateService.getAllActiveHeaders(null);

        Assertions.assertEquals(10, headers.size(), "Must have exactly 10 active header templates");
        Assertions.assertEquals("canva-header-royal-blue-social", headers.get(0).getTemplateKey());
        Assertions.assertEquals("canva-header-black-patron", headers.get(9).getTemplateKey());
        Assertions.assertEquals(1, headers.get(0).getDisplayOrder());
        Assertions.assertEquals(10, headers.get(9).getDisplayOrder());
        Assertions.assertTrue(headers.get(0).getTemplateData().has("nodes"));
    }

    @Test
    @DisplayName("Should be idempotent on repeated initializer runs")
    void testInitializerIdempotency() {
        long countBefore = headerTemplateRepository.count();
        dataInitializer.run();
        long countAfter = headerTemplateRepository.count();

        Assertions.assertEquals(10, countBefore);
        Assertions.assertEquals(countBefore, countAfter, "Re-running initializer must not insert duplicates");
    }

    @Test
    @DisplayName("Should soft-deactivate header template without deleting row")
    void testSoftDeactivation() {
        EmailCampaignHeaderTemplateResponse target =
                headerTemplateService.getHeaderByKey("canva-header-lime-arrow");

        headerTemplateService.deactivateHeader(target.getId());

        List<EmailCampaignHeaderTemplateResponse> active =
                headerTemplateService.getAllActiveHeaders(null);
        Assertions.assertEquals(9, active.size(), "Active headers should exclude soft-deactivated item");
        Assertions.assertTrue(
                headerTemplateRepository.findById(target.getId()).isPresent(),
                "Soft-deactivated row must still exist in database"
        );
        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> headerTemplateService.getHeaderByKey("canva-header-lime-arrow")
        );
    }
}
