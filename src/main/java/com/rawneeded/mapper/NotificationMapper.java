package com.rawneeded.mapper;

import com.rawneeded.dto.notification.CreateNotificationDto;
import com.rawneeded.dto.notification.NotificationResponseDto;
import com.rawneeded.dto.notification.UpdateNotificationDto;
import com.rawneeded.model.Notification;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "read", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    Notification toEntity(CreateNotificationDto dto);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "read", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "relatedEntityId", ignore = true)
    @Mapping(target = "relatedEntityType", ignore = true)
    void update(@MappingTarget Notification notification, UpdateNotificationDto dto);
    
    @Mapping(target = "read", source = "read")
    @Mapping(target = "title", ignore = true) // Will be set in service based on user language
    @Mapping(target = "message", ignore = true) // Will be set in service based on user language
    NotificationResponseDto toResponseDto(Notification notification);
    
    default Page<NotificationResponseDto> toResponsePages(Page<Notification> notifications) {
        return notifications.map(this::toResponseDto);
    }
    
    default List<NotificationResponseDto> toResponseList(List<Notification> notifications) {
        return notifications.stream().map(this::toResponseDto).toList();
    }
}
