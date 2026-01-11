package com.rawneeded.service;

import com.rawneeded.dto.subscription.CalculatePriceRequestDto;
import com.rawneeded.dto.subscription.CalculatePriceResponseDto;
import com.rawneeded.dto.subscription.CreateSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IUserSubscriptionService {

    CalculatePriceResponseDto calculatePrice(CalculatePriceRequestDto requestDto);

    UserSubscriptionResponseDto submitUserSubscription(UserSubscriptionRequestDto requestDto);

    User putUserOnFreeTrail(User user);

    UserSubscriptionResponseDto getUserSubscription();

    UserSubscriptionResponseDto updateUsedUsers(String subscriptionId, boolean add);

    // Pending subscription management
    Page<UserSubscriptionResponseDto> getAllPendingUserSubscriptions(Pageable pageable);

    UserSubscriptionResponseDto approveUserSubscription(String userSubscriptionId);

    UserSubscriptionResponseDto rejectUserSubscription(String userSubscriptionId, String reason);
}
