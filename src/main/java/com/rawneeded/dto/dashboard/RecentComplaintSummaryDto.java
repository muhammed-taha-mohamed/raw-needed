package com.rawneeded.dto.dashboard;

import com.rawneeded.enumeration.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentComplaintSummaryDto {
    private String id;
    private String subject;
    private ComplaintStatus status;
    private String userId;
    private LocalDateTime createdAt;
}
