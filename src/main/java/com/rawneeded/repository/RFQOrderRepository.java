package com.rawneeded.repository;

import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.model.RFQOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RFQOrderRepository extends MongoRepository<RFQOrder, String> {
    Page<RFQOrder> findByOwnerIdOrderByCreatedAtDesc(String ownerId, Pageable pageable);

    Page<RFQOrder> findByOwnerIdAndStatusOrderByCreatedAtDesc(String ownerId, OrderStatus status,  Pageable pageable);

    List<RFQOrder> findByOwnerIdOrderByCreatedAtDesc(String ownerId);
    List<RFQOrder> findByUserIdOrderByCreatedAtDesc(String userId);
    

}