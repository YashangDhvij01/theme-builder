
package com.themebuilder.backend.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.activation.DataSource;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private record ImageAttachment(
            String contentId,
            DataSource dataSource
    ) {}

    public void sendDesignEmail(
            String recipientEmail,
            String designName,
            String htmlContent
    ) throws Exception {

        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalStateException(
                    "SPRING_MAIL_USERNAME must be configured to send emails."
            );
        }

        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IllegalArgumentException(
                    "HTML content cannot be empty."
            );
        }

        List<ImageAttachment> images = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "src=([\"'])data:(image/[A-Za-z0-9.+-]+);base64,([^\"']+)\\1",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(htmlContent);

        StringBuffer updatedHtml = new StringBuffer();

        int imageNumber = 0;

        while (matcher.find()) {

            String contentType = matcher.group(2);
            String base64Data = matcher.group(3);

            byte[] imageBytes = Base64.getDecoder().decode(
                    base64Data.replaceAll("\\s+", "")
            );

            String contentId = "design-image-" + imageNumber++;

            DataSource dataSource = new ByteArrayDataSource(
                    imageBytes,
                    contentType
            );

            images.add(new ImageAttachment(contentId, dataSource));

            matcher.appendReplacement(
                    updatedHtml,
                    Matcher.quoteReplacement(
                            "src=\"cid:" + contentId + "\""
                    )
            );
        }

        matcher.appendTail(updatedHtml);

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                true,
                "UTF-8"
        );

        helper.setFrom(senderEmail);
        helper.setTo(recipientEmail);
        helper.setSubject("Design shared with you: " + designName);

        helper.setText(updatedHtml.toString(), true);

        for (ImageAttachment image : images) {
            helper.addInline(
                    image.contentId(),
                    image.dataSource()
            );
        }

        mailSender.send(message);
    }
}
