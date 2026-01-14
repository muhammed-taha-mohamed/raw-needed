package com.rawneeded.repository;

import com.rawneeded.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    
    long countByUserIdAndReadFalse(String userId);
    
    List<Notification> findByUserIdAndReadFalse(String userId);
    
    Page<Notification> findByUserIdAndReadOrderByCreatedAtDesc(String userId, boolean read, Pageable pageable);
    
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, com.rawneeded.enumeration.NotificationType type);
}
