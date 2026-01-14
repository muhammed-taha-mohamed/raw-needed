package com.rawneeded.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNotificationDto {
    private String titleEn;
    private String titleAr;
    private String messageEn;
    private String messageAr;
    private String metadata;
}
