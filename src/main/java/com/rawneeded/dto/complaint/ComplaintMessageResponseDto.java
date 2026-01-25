package com.rawneeded.dto.complaint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ComplaintMessageResponseDto {
    private String id;
    private String complaintId;
    private String userId;
    private String userName;
    private String message;
    private String image;
    private boolean isAdmin;
    private LocalDateTime createdAt;
}
