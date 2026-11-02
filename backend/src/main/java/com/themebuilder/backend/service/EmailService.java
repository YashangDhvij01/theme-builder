
package com.themebuilder.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
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

        String imagePrefix = "data:image/png;base64,";
        int imageStart = htmlContent.indexOf(imagePrefix);

        if (imageStart == -1) {
            throw new IllegalArgumentException(
                    "The design image was not found in the HTML."
            );
        }

        int base64Start = imageStart + imagePrefix.length();
        int imageEnd = htmlContent.indexOf('"', base64Start);

        if (imageEnd == -1) {
            throw new IllegalArgumentException(
                    "The design image data is invalid."
            );
        }

        String base64Image = htmlContent.substring(base64Start, imageEnd);

        byte[] imageBytes = java.util.Base64.getDecoder()
                .decode(base64Image);

        String updatedHtml = htmlContent.substring(0, imageStart)
                + "cid:designImage"
                + htmlContent.substring(imageEnd);

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8"
        );

        helper.setFrom(senderEmail);
        helper.setTo(recipientEmail);
        helper.setSubject("Design shared with you: " + designName);
        helper.setText(updatedHtml, true);

        DataSource imageSource = new ByteArrayDataSource(
                imageBytes, "image/png"
        );

        helper.addInline("designImage", imageSource);

        mailSender.send(message);
    }
}