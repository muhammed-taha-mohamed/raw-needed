package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.error.exceptions.AbstractException;
import freemarker.template.Configuration;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {


    private final JavaMailSender javaMailSender;
    private final Configuration freemarkerConfig;

    public void sendEmail(MailDto mailDto) {
        try {
            log.info("Start sending email to: {}", mailDto.getToEmail());
            if (mailDto.getModel() == null) mailDto.setModel(new HashMap<>());
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(mailDto.getToEmail());
            helper.setSubject(mailDto.getSubject());

            // Process Freemarker template
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(
                    freemarkerConfig.getTemplate(mailDto.getTemplateName().getName()), mailDto.getModel());

            helper.setText(content, true);

            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", mailDto.getToEmail());
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new AbstractException("Failed to send email: " + e.getMessage());
        }
    }

}
