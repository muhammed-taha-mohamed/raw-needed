package com.rawneeded.service;

import com.rawneeded.dto.advertisement.AdSubscriptionResponseDto;
import com.rawneeded.dto.advertisement.CreateAdSubscriptionRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAdSubscriptionService {

    /** Supplier: create ad package subscription request */
    AdSubscriptionResponseDto createSubscription(CreateAdSubscriptionRequestDto dto);

    /** Supplier: list my subscriptions */
    List<AdSubscriptionResponseDto> getMySubscriptions();

    /** Admin: list pending requests */
    Page<AdSubscriptionResponseDto> getPendingSubscriptions(Pageable pageable);

    /** Admin: list approved (active) subscriptions */
    Page<AdSubscriptionResponseDto> getApprovedSubscriptions(Pageable pageable);

    /** Admin: approve subscription */
    AdSubscriptionResponseDto approve(String subscriptionId);

    /** Admin: reject subscription */
    AdSubscriptionResponseDto reject(String subscriptionId);

    /** Whether supplier has an approved, valid subscription (can add ads) */
    boolean hasActiveSubscription(String supplierId);

    /** Consume one ad from supplier subscription (after creating an ad) */
    void consumeOneAd(String supplierId);
}
