package com.rawneeded.repository;

import com.rawneeded.enumeration.UserSubscriptionStatus;
import com.rawneeded.model.AdSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdSubscriptionRepository extends MongoRepository<AdSubscription, String> {

    List<AdSubscription> findBySupplierIdOrderByRequestedAtDesc(String supplierId);

    Page<AdSubscription> findByStatus(Pageable pageable, UserSubscriptionStatus status);

    long countByStatus(UserSubscriptionStatus status);

    long countByRequestedAtAfter(LocalDateTime date);

    /** Approved subscription still valid and with remaining ads (no date check - validity is per ad) */
    Optional<AdSubscription> findFirstBySupplierIdAndStatusAndRemainingAdsGreaterThanOrderByApprovedAtDesc(
            String supplierId, UserSubscriptionStatus status, int remainingAdsMin);

    /** Active subscriptions: approved with at least one ad remaining (no end date - subscription is ads-based only) */
    long countByStatusAndRemainingAdsGreaterThan(UserSubscriptionStatus status, int remainingAds);

    long countByStatusAndEndDateAfterAndRemainingAdsGreaterThan(UserSubscriptionStatus userSubscriptionStatus, LocalDateTime now, int i);
}
