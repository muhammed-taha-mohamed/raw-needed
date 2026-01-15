package com.rawneeded.service;

import com.rawneeded.dto.notification.CreateNotificationDto;
import com.rawneeded.dto.notification.NotificationResponseDto;
import com.rawneeded.dto.notification.UpdateNotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface INotificationService {
    
    // CRUD Operations
    NotificationResponseDto create(CreateNotificationDto dto);
    
    NotificationResponseDto findById(String id);
    
    Page<NotificationResponseDto> findByUserId( Pageable pageable);

    NotificationResponseDto update(String id, UpdateNotificationDto dto);
    
    void delete(String id);
    
    // Read status operations
    NotificationResponseDto markAsRead(String id);
    
    void markAllAsRead(String userId);
    
    // Utility methods
    long getUnreadCount();

    // Send notifications to multiple users
    // These methods accept message keys that will be resolved from Messages.properties
    void sendNotificationToSupplier(String supplierId, com.rawneeded.enumeration.NotificationType type, String titleKey, String messageKey, String relatedEntityId, String relatedEntityType, Object... messageArgs);
    
    void sendNotificationToCustomer(String customerId, com.rawneeded.enumeration.NotificationType type, String titleKey, String messageKey, String relatedEntityId, String relatedEntityType, Object... messageArgs);
    
    void sendNotificationToUser(String userId, com.rawneeded.enumeration.NotificationType type, String titleKey, String messageKey, String relatedEntityId, String relatedEntityType, Object... messageArgs);
}
