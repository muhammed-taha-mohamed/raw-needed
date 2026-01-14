package com.rawneeded.dto.notification;

import com.rawneeded.enumeration.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDto {
    private String id;
    private String userId;
    private NotificationType type;
    private String title; // Will be set based on user language preference
    private String message; // Will be set based on user language preference
    private String titleEn;
    private String titleAr;
    private String messageEn;
    private String messageAr;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String relatedEntityId;
    private String relatedEntityType;
    private String metadata;
}
