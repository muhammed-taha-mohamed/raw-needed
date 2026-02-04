package com.rawneeded.service;

import com.rawneeded.dto.subscription.CalculatePriceRequestDto;
import com.rawneeded.dto.subscription.CalculatePriceResponseDto;
import com.rawneeded.dto.subscription.UserSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserSubscriptionService {

    CalculatePriceResponseDto calculatePrice(CalculatePriceRequestDto requestDto);

    UserSubscriptionResponseDto submitUserSubscription(UserSubscriptionRequestDto requestDto);

    User putUserOnFreeTrail(User user);

    UserSubscriptionResponseDto getUserSubscription();

    void updateUsedUsers(String subscriptionId, boolean add);

    // Pending subscription management
    Page<UserSubscriptionResponseDto> getAllPendingUserSubscriptions(Pageable pageable);

    /** Admin: list approved (non-pending) user subscriptions */
    Page<UserSubscriptionResponseDto> getApprovedUserSubscriptions(Pageable pageable);

    UserSubscriptionResponseDto approveUserSubscription(String userSubscriptionId);

    UserSubscriptionResponseDto rejectUserSubscription(String userSubscriptionId, String reason);
    
    // Deduct search and add points for customer
    boolean deductSearchAndAddPoints(String userId);
}
