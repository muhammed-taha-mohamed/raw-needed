package com.rawneeded.repository;

import com.rawneeded.enumeration.UserSubscriptionStatus;
import com.rawneeded.model.AddSearchesRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AddSearchesRequestRepository extends MongoRepository<AddSearchesRequest, String> {

    List<AddSearchesRequest> findByStatusOrderByCreatedAtDesc(UserSubscriptionStatus status);

    Page<AddSearchesRequest> findByStatusOrderByCreatedAtDesc(UserSubscriptionStatus status, Pageable pageable);

    long countByStatus(UserSubscriptionStatus status);
}
