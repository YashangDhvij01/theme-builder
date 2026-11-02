
package com.themebuilder.backend.controller;

import com.themebuilder.backend.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-design")
    public ResponseEntity<String> sendDesignEmail(
            @RequestBody EmailRequest request) {

        try {
            emailService.sendDesignEmail(
                    request.getRecipientEmail(),
                    request.getDesignName(),
                    request.getHtmlContent()
            );

            return ResponseEntity.ok(
                    "HTML design email sent successfully!"
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to send design email.");
        }
    }

    public static class EmailRequest {

        private String recipientEmail;
        private String designName;
        private String htmlContent;

        public String getRecipientEmail() {
            return recipientEmail;
        }

        public void setRecipientEmail(String recipientEmail) {
            this.recipientEmail = recipientEmail;
        }

        public String getDesignName() {
            return designName;
        }

        public void setDesignName(String designName) {
            this.designName = designName;
        }

        public String getHtmlContent() {
            return htmlContent;
        }

        public void setHtmlContent(String htmlContent) {
            this.htmlContent = htmlContent;
        }
    }
}
