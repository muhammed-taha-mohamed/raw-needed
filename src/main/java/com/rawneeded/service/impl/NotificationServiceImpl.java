package com.rawneeded.service.impl;

import com.rawneeded.dto.notification.CreateNotificationDto;
import com.rawneeded.dto.notification.NotificationResponseDto;
import com.rawneeded.dto.notification.UpdateNotificationDto;
import com.rawneeded.enumeration.NotificationType;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.NotificationMapper;
import com.rawneeded.model.Notification;
import com.rawneeded.repository.NotificationRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.enumeration.LanguagePreference;
import com.rawneeded.model.User;
import com.rawneeded.service.INotificationService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final MessagesUtil messagesUtil;
    private final MessageSource messageSource;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public NotificationResponseDto create(CreateNotificationDto dto) {
        log.info("Creating notification for user: {}", dto.getUserId());
        
        // Validate user exists
        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));
        
        Notification notification = notificationMapper.toEntity(dto);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created successfully with id: {}", saved.getId());
        
        return mapToResponseDto(saved);
    }

    @Override
    public NotificationResponseDto findById(String id) {
        log.info("Finding notification by id: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("NOTIFICATION_NOT_FOUND")));
        return mapToResponseDto(notification);
    }

    @Override
    public Page<NotificationResponseDto> findByUserId(Pageable pageable) {
        String token = messagesUtil.getAuthToken();
        String userId = tokenProvider.getOwnerIdFromToken(token);
        log.info("Finding notifications for user: {}", userId);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId,pageable);
        return notifications.map(this::mapToResponseDto);
    }

    @Override
    @Transactional
    public NotificationResponseDto update(String id, UpdateNotificationDto dto) {
        log.info("Updating notification: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("NOTIFICATION_NOT_FOUND")));
        
        notificationMapper.update(notification, dto);
        Notification updated = notificationRepository.save(notification);
        log.info("Notification updated successfully: {}", id);
        
        return mapToResponseDto(updated);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Deleting notification: {}", id);
        if (!notificationRepository.existsById(id)) {
            throw new AbstractException(messagesUtil.getMessage("NOTIFICATION_NOT_FOUND"));
        }
        notificationRepository.deleteById(id);
        log.info("Notification deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public NotificationResponseDto markAsRead(String id) {
        log.info("Marking notification as read: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("NOTIFICATION_NOT_FOUND")));
        
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
            log.info("Notification marked as read: {}", id);
        }
        
        return mapToResponseDto(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadFalse(userId);
        
        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
        });
        
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), userId);
    }

    @Override
    public long getUnreadCount() {
        String token = messagesUtil.getAuthToken();
        String userId = tokenProvider.getOwnerIdFromToken(token);
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void sendNotificationToSupplier(String supplierId, NotificationType type, String titleKey, String messageKey, String relatedEntityId, String relatedEntityType, Object... messageArgs) {
        log.info("Sending notification to supplier: {}", supplierId);
        String titleEn = messageSource.getMessage(titleKey, messageArgs, Locale.ENGLISH);
        String titleAr = messageSource.getMessage(titleKey, messageArgs, new Locale("ar"));
        String messageEn = messageSource.getMessage(messageKey, messageArgs, Locale.ENGLISH);
        String messageAr = messageSource.getMessage(messageKey, messageArgs, new Locale("ar"));
        
        CreateNotificationDto dto = CreateNotificationDto.builder()
                .userId(supplierId)
                .type(type)
                .titleEn(titleEn)
                .titleAr(titleAr)
                .messageEn(messageEn)
                .messageAr(messageAr)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .build();
        create(dto);
    }

    @Override
    @Transactional
    public void sendNotificationToCustomer(String customerId, NotificationType type, String titleKey, String messageKey, String relatedEntityId, String relatedEntityType, Object... messageArgs) {
        log.info("Sending notification to customer: {}", customerId);
        String titleEn = messageSource.getMessage(titleKey, messageArgs, Locale.ENGLISH);
        String titleAr = messageSource.getMessage(titleKey, messageArgs, new Locale("ar"));
        String messageEn = messageSource.getMessage(messageKey, messageArgs, Locale.ENGLISH);
        String messageAr = messageSource.getMessage(messageKey, messageArgs, new Locale("ar"));
        
        CreateNotificationDto dto = CreateNotificationDto.builder()
                .userId(customerId)
                .type(type)
                .titleEn(titleEn)
                .titleAr(titleAr)
                .messageEn(messageEn)
                .messageAr(messageAr)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .build();
        create(dto);
    }

    @Override
    @Transactional
    public void sendNotificationToUser(String userId, NotificationType type, String titleKey, String messageKey, String relatedEntityId, String relatedEntityType, Object... messageArgs) {
        log.info("Sending notification to user: {}", userId);
        String titleEn = messageSource.getMessage(titleKey, messageArgs, Locale.ENGLISH);
        String titleAr = messageSource.getMessage(titleKey, messageArgs, new Locale("ar"));
        String messageEn = messageSource.getMessage(messageKey, messageArgs, Locale.ENGLISH);
        String messageAr = messageSource.getMessage(messageKey, messageArgs, new Locale("ar"));
        
        CreateNotificationDto dto = CreateNotificationDto.builder()
                .userId(userId)
                .type(type)
                .titleEn(titleEn)
                .titleAr(titleAr)
                .messageEn(messageEn)
                .messageAr(messageAr)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .build();
        create(dto);
    }
    
    /**
     * Maps Notification entity to ResponseDto with title and message based on user's language preference
     */
    private NotificationResponseDto mapToResponseDto(Notification notification) {
        NotificationResponseDto dto = notificationMapper.toResponseDto(notification);
        
        // Get user to determine language preference
        User user = userRepository.findById(notification.getUserId()).orElse(null);
        LanguagePreference languagePreference = user != null ? user.getLanguagePreference() : LanguagePreference.EN;
        
        // Set title and message based on user's language preference
        if (languagePreference == LanguagePreference.AR) {
            dto.setTitle(notification.getTitleAr() != null ? notification.getTitleAr() : notification.getTitleEn());
            dto.setMessage(notification.getMessageAr() != null ? notification.getMessageAr() : notification.getMessageEn());
        } else {
            dto.setTitle(notification.getTitleEn() != null ? notification.getTitleEn() : notification.getTitleAr());
            dto.setMessage(notification.getMessageEn() != null ? notification.getMessageEn() : notification.getMessageAr());
        }
        
        return dto;
    }
}
