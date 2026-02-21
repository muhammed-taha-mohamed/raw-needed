package com.rawneeded.dto.advertisement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementViewStatsDto {
    private String viewerId;
    private String viewerName;
    private String viewerEmail;
    private LocalDateTime viewedAt;
}
