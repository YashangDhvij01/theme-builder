
package com.themebuilder.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDesignEmail(
            String recipientEmail,
            String designName,
            String htmlContent
    ) throws Exception {

        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IllegalArgumentException(
                    "HTML content cannot be empty."
            );
        }

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                message, false, "UTF-8"
        );

        helper.setFrom(senderEmail);
        helper.setTo(recipientEmail);
        helper.setSubject("Design shared with you: " + designName);

        // Send the complete HTML directly as the email body.
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
