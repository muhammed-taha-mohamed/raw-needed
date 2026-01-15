package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.notification.CreateNotificationDto;
import com.rawneeded.dto.notification.NotificationResponseDto;
import com.rawneeded.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/notifications")
@RestController
public class NotificationController {

    private final INotificationService notificationService;

    // ================= CREATE =================

    @PostMapping
    @Operation(
            summary = "Create a new notification",
            description = "Create a new notification for a user"
    )
    public ResponseEntity<ResponsePayload> create(@RequestBody CreateNotificationDto dto) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", notificationService.create(dto)))
                .build()
        );
    }

    // ================= READ =================

    @GetMapping("/{id}")
    @Operation(
            summary = "Get notification by id",
            description = "Fetch notification details using notification id"
    )
    public ResponseEntity<ResponsePayload> findById(@PathVariable String id) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", notificationService.findById(id)))
                .build()
        );
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get notifications by user id",
            description = "Get paginated notifications list for a user"
    )
    public ResponseEntity<ResponsePayload> findByUserId(
            Pageable pageable
    ) {
        Page<NotificationResponseDto> page = notificationService.findByUserId(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", page))
                .build()
        );
    }



    @GetMapping("/user/unread-count")
    @Operation(
            summary = "Get unread notifications count",
            description = "Get count of unread notifications for a user"
    )
    public ResponseEntity<ResponsePayload> getUnreadCount() {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "count", notificationService.getUnreadCount()))
                .build()
        );
    }



    @PatchMapping("/{id}/mark-read")
    @Operation(
            summary = "Mark notification as read",
            description = "Mark a specific notification as read"
    )
    public ResponseEntity<ResponsePayload> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", notificationService.markAsRead(id)))
                .build()
        );
    }

    @PatchMapping("/user/{userId}/mark-all-read")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark all notifications as read for a specific user"
    )
    public ResponseEntity<ResponsePayload> markAllAsRead(@PathVariable String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "All notifications marked as read"))
                .build()
        );
    }

    // ================= DELETE =================

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete notification",
            description = "Delete notification by notification id"
    )
    public ResponseEntity<ResponsePayload> delete(@PathVariable String id) {
        notificationService.delete(id);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "Notification deleted successfully"))
                .build()
        );
    }
}
