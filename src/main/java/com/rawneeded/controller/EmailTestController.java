package com.rawneeded.controller;

import com.rawneeded.config.StartupEmailTester;
import com.rawneeded.dto.ResponsePayload;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("api/v1/test")
@RequiredArgsConstructor
public class EmailTestController {

    @Value("${app.email.dummy-recipient:}")
    private String dummyRecipient;

    private final StartupEmailTester startupEmailTester;

    @PostMapping("/emails/dummy")
    @Operation(summary = "Send dummy transactional emails", description = "Sends test emails for all transactional templates to app.email.dummy-recipient (e.g. mtaha@acuanix.com)")
    public ResponseEntity<ResponsePayload> sendDummyEmails() {
        if (dummyRecipient == null || dummyRecipient.isBlank()) {
            return ResponseEntity.badRequest().body(ResponsePayload.builder()
                    .date(LocalDateTime.now())
                    .error(Map.of("message", "app.email.dummy-recipient is not set"))
                    .build());
        }
        startupEmailTester.sendDummyEmails();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "recipient", dummyRecipient, "message", "Dummy emails sent"))
                .build());
    }
}
