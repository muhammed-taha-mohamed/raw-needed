package com.rawneeded.service;

import com.rawneeded.dto.subscription.CreatePlanRequestDto;
import com.rawneeded.dto.subscription.CreateSubscriptionRequestDto;
import com.rawneeded.dto.subscription.QuotationRequestDto;
import com.rawneeded.dto.subscription.QuotationResponseDto;
import com.rawneeded.dto.subscription.SubscriptionPlanResponseDto;
import com.rawneeded.dto.subscription.UpdatePlanRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;

import java.util.List;

public interface ISubscriptionService {
    List<SubscriptionPlanResponseDto> getAllPlans();
    SubscriptionPlanResponseDto getPlanById(String planId);
    SubscriptionPlanResponseDto createPlan(CreatePlanRequestDto requestDto);
    SubscriptionPlanResponseDto updatePlan(String planId, UpdatePlanRequestDto requestDto);
    QuotationResponseDto submitQuotation(String userId, QuotationRequestDto requestDto);
    QuotationResponseDto getQuotationByOwnerId(String ownerId);
    UserSubscriptionResponseDto createSubscription(String userId, CreateSubscriptionRequestDto requestDto);
    UserSubscriptionResponseDto getUserSubscription(String userId);
    UserSubscriptionResponseDto updateUsedUsers(String subscriptionId, int usedUsers);
}
