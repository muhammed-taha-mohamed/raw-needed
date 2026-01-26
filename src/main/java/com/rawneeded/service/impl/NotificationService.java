package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.util.MessagesUtil;
import freemarker.template.Configuration;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {


    @Value("${app.email.logo-url}")
    private String logoUrl;
    private final JavaMailSender javaMailSender;
    private final Configuration freemarkerConfig;
    private final MessagesUtil messagesUtil;

    public void sendEmail(MailDto mailDto) {
        try {
            log.info("Start sending email to: {}", mailDto.getToEmail());

            // Ensure model is mutable
            Map<String, Object> model = mailDto.getModel();
            if (model == null) {
                model = new HashMap<>();
            } else {
                model = new HashMap<>(model);
            }

            if (logoUrl != null && !logoUrl.isEmpty()) {
                model.put("logoUrl", logoUrl);
            }
            mailDto.setModel(model);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("info@rawneeded.com");
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
            throw new AbstractException(messagesUtil.getMessage("NOTIFICATION_EMAIL_FAIL"));
        }
    }

}
