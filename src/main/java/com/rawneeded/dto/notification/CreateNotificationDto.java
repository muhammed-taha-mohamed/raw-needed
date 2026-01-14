package com.rawneeded.dto.notification;

import com.rawneeded.enumeration.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNotificationDto {
    private String userId;
    private NotificationType type;
    private String titleEn;
    private String titleAr;
    private String messageEn;
    private String messageAr;
    private String relatedEntityId;
    private String relatedEntityType;
    private String metadata;
}
