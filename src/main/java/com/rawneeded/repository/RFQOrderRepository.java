package com.rawneeded.repository;

import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.model.RFQOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RFQOrderRepository extends MongoRepository<RFQOrder, String> {
    Page<RFQOrder> findByOwnerId(String ownerId, Pageable pageable);

    Page<RFQOrder> findByOwnerIdAndStatus(String ownerId, OrderStatus status,  Pageable pageable);

}