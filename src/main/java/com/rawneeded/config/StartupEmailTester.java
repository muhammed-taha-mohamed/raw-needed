package com.rawneeded.config;

import com.rawneeded.dto.MailDto;
import com.rawneeded.enumeration.TemplateName;
import com.rawneeded.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Sends dummy transactional emails to the configured recipient (e.g. for testing layout and branding).
 * Triggered via the email-test controller, not on startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupEmailTester {

    @Value("${app.email.dummy-recipient:}")
    private String dummyRecipient;

    private final NotificationService emailService;

    
    public void sendDummyEmails() {
        if (dummyRecipient == null || dummyRecipient.isBlank()) {
            log.warn("app.email.send-dummy-on-startup=true but app.email.dummy-recipient is empty; skipping dummy emails.");
            return;
        }
        log.info("Sending dummy transactional emails to: {}", dummyRecipient);
        List<Map<String, Object>> orderItems = List.of(
                Map.of("quantity", "10", "productName", "منتج تجريبي / Dummy Product"),
                Map.of("quantity", "5", "productName", "صنف اختبار / Test Item")
        );
        try {
            send(MailDto.builder().toEmail(dummyRecipient).subject("[Test] طلب جديد | New Order")
                    .templateName(TemplateName.ORDER_CREATED_SUPPLIER)
                    .model(Map.of("supplierName", "مورد تجريبي", "orderNumber", "ORD-DUMMY-001",
                            "customerName", "عميل تجريبي", "customerOrg", "منظمة تجريبية", "items", orderItems)).build());
            send(MailDto.builder().toEmail(dummyRecipient).subject("[Test] تم إلغاء الطلب | Order Cancelled")
                    .templateName(TemplateName.ORDER_CANCELLED_SUPPLIER)
                    .model(Map.of("supplierName", "مورد تجريبي", "orderNumber", "ORD-DUMMY-001")).build());
            send(MailDto.builder().toEmail(dummyRecipient).subject("[Test] موافقة على الطلب | Order Approved")
                    .templateName(TemplateName.ORDER_APPROVED_SUPPLIER)
                    .model(Map.of("supplierName", "مورد تجريبي", "productName", "منتج تجريبي")).build());
            send(MailDto.builder().toEmail(dummyRecipient).subject("[Test] تم إكمال الطلب | Order Completed")
                    .templateName(TemplateName.ORDER_COMPLETED_CUSTOMER)
                    .model(Map.of("customerName", "عميل تجريبي", "productName", "منتج تجريبي")).build());
            send(MailDto.builder().toEmail(dummyRecipient).subject("[Test] رد المورد | Supplier Response")
                    .templateName(TemplateName.ORDER_REPLY_CUSTOMER)
                    .model(Map.of("customerName", "عميل تجريبي", "supplierName", "مورد تجريبي", "productName", "منتج تجريبي")).build());
            send(MailDto.builder().toEmail(dummyRecipient).subject("[Test] رسالة في الطلب | Order Message")
                    .templateName(TemplateName.ORDER_MESSAGE)
                    .model(Map.of("recipientName", "مستلم تجريبي", "senderName", "مرسل تجريبي", "senderOrg", "منظمة تجريبية",
                            "orderNumber", "ORD-DUMMY-001", "messageText", "هذه رسالة تجريبية / This is a dummy message.")).build());
            send(MailDto.builder().toEmail(dummyRecipient).subject("[Test] شكوى جديدة | New Complaint")
                    .templateName(TemplateName.COMPLAINT_CREATED_ADMIN)
                    .model(Map.of("userName", "مستخدم تجريبي", "subject", "موضوع تجريبي", "description", "نص الشكوى التجريبي.")).build());
            send(MailDto.builder().toEmail(dummyRecipient).subject("[Test] رد على الشكوى | Complaint Reply")
                    .templateName(TemplateName.COMPLAINT_REPLY_USER)
                    .model(Map.of("userName", "مستخدم تجريبي", "subject", "موضوع تجريبي")).build());
            send(MailDto.builder().toEmail(dummyRecipient).subject("[Test] تم إغلاق الشكوى | Complaint Closed")
                    .templateName(TemplateName.COMPLAINT_CLOSED_USER)
                    .model(Map.of("userName", "مستخدم تجريبي", "subject", "موضوع تجريبي")).build());
            log.info("Dummy emails sent successfully to {}", dummyRecipient);
        } catch (Exception e) {
            log.error("Failed to send dummy emails: {}", e.getMessage(), e);
        }
    }

    private void send(MailDto dto) {
        try {
            emailService.sendEmail(dto);
        } catch (Exception e) {
            log.warn("Dummy email failed for template {}: {}", dto.getTemplateName(), e.getMessage());
        }
    }
}
