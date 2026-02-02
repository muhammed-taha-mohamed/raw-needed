package com.rawneeded.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BulkUploadResultDto {
    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<RowErrorDto> errors;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class RowErrorDto {
        private int rowNumber;
        private String productName;
        private String errorMessage;
    }
}
