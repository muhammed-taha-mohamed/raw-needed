package com.rawneeded.dto.complaint;

import com.rawneeded.enumeration.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ComplaintResponseDto {
    private String id;
    private String userId;
    private String userName;
    private String subject;
    private String description;
    private String image;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private List<ComplaintMessageResponseDto> messages;
}
