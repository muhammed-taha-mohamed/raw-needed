package com.rawneeded.service;

import com.rawneeded.dto.subscription.QuotationResponseDto;

import java.util.List;

public interface IAdminService {
    List<QuotationResponseDto> getAllPendingQuotations();
    QuotationResponseDto approveQuotation(String quotationId);
    QuotationResponseDto rejectQuotation(String quotationId, String reason);
}
