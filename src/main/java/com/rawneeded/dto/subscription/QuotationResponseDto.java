package com.rawneeded.dto.subscription;

import com.rawneeded.enumeration.QuotationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class QuotationResponseDto {
    private String id;
    private String ownerId;
    private String planId;
    private String planName;
    private String filePath;
    private QuotationStatus status;
    private LocalDateTime submissionDate;
}

