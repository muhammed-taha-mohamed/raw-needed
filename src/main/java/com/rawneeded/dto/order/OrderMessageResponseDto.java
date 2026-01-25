package com.rawneeded.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderMessageResponseDto {
    private String id;
    private String orderId;
    private String userId;
    private String userName;
    private String userOrganizationName;
    private String message;
    private String image;
    private LocalDateTime createdAt;
}
