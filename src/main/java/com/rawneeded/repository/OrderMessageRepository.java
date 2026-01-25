package com.rawneeded.repository;

import com.rawneeded.model.OrderMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderMessageRepository extends MongoRepository<OrderMessage, String> {
    List<OrderMessage> findByOrderIdOrderByCreatedAtAsc(String orderId);
}
