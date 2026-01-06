package com.rawneeded.repository;

import com.rawneeded.model.UserSubscription;
import com.rawneeded.enumeration.UserSubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends MongoRepository<UserSubscription, String> {
    Optional<UserSubscription> findByUserId(String userId);
    List<UserSubscription> findByPlanId(String planId);
    Optional<UserSubscription> findByUserIdAndStatus(String userId, UserSubscriptionStatus status);
    Page<UserSubscription> findByStatus(Pageable pageable,UserSubscriptionStatus status);
}
