package com.rawneeded.service;

import com.rawneeded.dto.subscription.CreatePlanRequestDto;
import com.rawneeded.dto.subscription.SubscriptionPlanResponseDto;
import com.rawneeded.dto.subscription.UpdatePlanRequestDto;

import java.util.List;

public interface IPlanService {
    List<SubscriptionPlanResponseDto> getAllPlans();
    SubscriptionPlanResponseDto getPlanById(String planId);
    SubscriptionPlanResponseDto createPlan(CreatePlanRequestDto requestDto);
    SubscriptionPlanResponseDto updatePlan(String planId, UpdatePlanRequestDto requestDto);
    void deletePlan(String planId);
    SubscriptionPlanResponseDto activatePlan(String planId);
    SubscriptionPlanResponseDto deactivatePlan(String planId);
}
