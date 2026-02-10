package com.rawneeded.service;

import com.rawneeded.dto.report.ProductCardReportDto;
import com.rawneeded.dto.report.SupplierInsightsReportDto;
import com.rawneeded.dto.report.SupplierSalesReportDto;
import com.rawneeded.dto.report.SupplierProductOptionDto;

import java.util.List;

public interface ISupplierReportService {
    List<SupplierProductOptionDto> getSupplierProducts();
    ProductCardReportDto getProductCardReport(String productId);
    SupplierSalesReportDto getSalesReport(Integer month, Integer year);
    SupplierInsightsReportDto getInsightsReport();
}
