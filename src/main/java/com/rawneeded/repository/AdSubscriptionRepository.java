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

    /** Approved subscription still valid and with remaining ads */
    Optional<AdSubscription> findFirstBySupplierIdAndStatusAndEndDateAfterAndRemainingAdsGreaterThanOrderByApprovedAtDesc(
            String supplierId, UserSubscriptionStatus status, LocalDateTime now, int remainingAdsMin);
}
