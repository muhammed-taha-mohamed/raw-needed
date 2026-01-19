package com.rawneeded.dto.advertisement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AdvertisementResponseDto {
    private String id;
    private String userId;
    private String image;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
}
