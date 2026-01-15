package com.rawneeded.repository;

import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.model.RFQOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RFQOrderRepository extends MongoRepository<RFQOrder, String> {
    Page<RFQOrder> findByOwnerId(String ownerId, Pageable pageable);

    Page<RFQOrder> findByOwnerIdAndStatus(String ownerId, OrderStatus status,  Pageable pageable);
    
    // Statistics queries
    long countByOwnerId(String ownerId);
    long countByOwnerIdAndStatus(String ownerId, OrderStatus status);
    long countByUserId(String userId);
    long countByUserIdAndStatus(String userId, OrderStatus status);
    
    List<RFQOrder> findByOwnerId(String ownerId);
    List<RFQOrder> findByUserId(String userId);
    
    List<RFQOrder> findByOwnerIdAndCreatedAtBetween(String ownerId, LocalDateTime start, LocalDateTime end);
    List<RFQOrder> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);

}