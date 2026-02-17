package com.rawneeded.repository;

import com.rawneeded.enumeration.PlanType;
import com.rawneeded.model.SubscriptionPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPlanRepository extends MongoRepository<SubscriptionPlan, String> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);
    List<SubscriptionPlan> findByPlanType(PlanType planType);
    List<SubscriptionPlan> findByPlanTypeAndActiveTrue(PlanType planType);
    boolean existsByPlanTypeAndFreeTrue(PlanType planType);
    List<SubscriptionPlan> findByPlanTypeAndFreeTrue(PlanType planType);
    List<SubscriptionPlan> findByFreeTrue();
}
