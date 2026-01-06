package com.rawneeded.service;

import com.rawneeded.dto.subscription.CalculatePriceRequestDto;
import com.rawneeded.dto.subscription.CalculatePriceResponseDto;
import com.rawneeded.dto.subscription.CreateSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IUserSubscriptionService {
    CalculatePriceResponseDto calculatePrice(CalculatePriceRequestDto requestDto);
    
    UserSubscriptionResponseDto submitUserSubscription(UserSubscriptionRequestDto requestDto);



    UserSubscriptionResponseDto getUserSubscriptionByOwnerId(String ownerId);
    UserSubscriptionResponseDto getUserSubscriptionById(String id);
    UserSubscriptionResponseDto updateUserSubscription(String id, UserSubscriptionRequestDto requestDto);
    void deleteUserSubscription(String id);
    
    // Active subscription management (without file upload)
    UserSubscriptionResponseDto createSubscription(String userId, CreateSubscriptionRequestDto requestDto);
    UserSubscriptionResponseDto getUserSubscription();
    UserSubscriptionResponseDto updateUsedUsers(String subscriptionId, int usedUsers);

    Page<UserSubscriptionResponseDto> getAllPendingUserSubscriptions(Pageable pageable);

    @Transactional
    UserSubscriptionResponseDto approveUserSubscription(String userSubscriptionId);

    @Transactional
    UserSubscriptionResponseDto rejectUserSubscription(String userSubscriptionId, String reason);
}
