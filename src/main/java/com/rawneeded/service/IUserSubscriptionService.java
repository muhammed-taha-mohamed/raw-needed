package com.rawneeded.service;

import com.rawneeded.dto.subscription.AddSearchesRequestDto;
import com.rawneeded.dto.subscription.AddSearchesSubmitDto;
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

    /** Customer: submit request to add more searches (partial renewal). */
    AddSearchesRequestDto submitAddSearchesRequest(AddSearchesSubmitDto dto);

    /** Calculate price for adding N searches (current plan's pricePerSearch). */
    double calculateAddSearchesPrice(int numberOfSearches);

    /** Admin: list pending add-searches requests */
    Page<AddSearchesRequestDto> getPendingAddSearchesRequests(Pageable pageable);

    /** Admin: approve add-searches request and add searches to subscription */
    AddSearchesRequestDto approveAddSearchesRequest(String requestId);

    /** Admin: reject add-searches request */
    AddSearchesRequestDto rejectAddSearchesRequest(String requestId, String reason);

    // Deduct search or use 1 point for customer (no points given on search)
    boolean deductSearchAndAddPoints(String userId);

    /** Add 1 point to customer when a supplier responds to their order. */
    void addPointForSupplierResponse(String customerUserId);
}
