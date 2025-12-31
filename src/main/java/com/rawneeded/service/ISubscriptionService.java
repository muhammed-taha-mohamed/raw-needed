package com.rawneeded.service;

import com.rawneeded.dto.subscription.QuotationRequestDto;
import com.rawneeded.dto.subscription.QuotationResponseDto;
import com.rawneeded.dto.subscription.SubscriptionPlanResponseDto;

import java.util.List;

public interface ISubscriptionService {
    List<SubscriptionPlanResponseDto> getAllPlans();
    SubscriptionPlanResponseDto getPlanById(String planId);
    QuotationResponseDto submitQuotation(String userId, QuotationRequestDto requestDto);
    QuotationResponseDto getQuotationByOwnerId(String ownerId);
}
