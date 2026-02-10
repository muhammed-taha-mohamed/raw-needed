package com.rawneeded.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardReportDto {

    private ProductSummary product;
    private Integer totalLines;
    private Integer totalOrders;
    private Double totalSalesAmount;
    private Map<String, Long> statusCounts;
    private List<ProductCardReportOrderDto> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummary {
        private String id;
        private String name;
        private String origin;
        private String image;
        private String unit;
        private Boolean inStock;
        private Integer stockQuantity;
        private String categoryId;
        private String categoryName;
        private String subCategoryId;
        private String subCategoryName;
    }
}
