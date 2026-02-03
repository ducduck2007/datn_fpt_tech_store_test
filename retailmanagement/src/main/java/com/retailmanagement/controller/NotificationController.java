package com.retailmanagement.controller;


import com.retailmanagement.dto.response.NotificationResponse;
import com.retailmanagement.entity.Notification;
import com.retailmanagement.service.CustomerService;
import com.retailmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CustomerService customerService;

    /**
     * Lấy danh sách thông báo của khách hàng hiện tại
     */
    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly
    ) {
        Integer customerId = getCurrentCustomerId();

        List<Notification> notifications = unreadOnly
                ? notificationService.getUnreadNotifications(customerId)
                : notificationService.getAllNotifications(customerId);

        List<NotificationResponse> response = notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    @GetMapping("/my/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Integer customerId = getCurrentCustomerId();
        long count = notificationService.countUnread(customerId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Đánh dấu thông báo đã đọc
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Integer customerId = getCurrentCustomerId();
        notificationService.markAsRead(id, customerId);
        return ResponseEntity.ok().build();
    }

    /**
     * Đánh dấu tất cả đã đọc
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Integer customerId = getCurrentCustomerId();
        notificationService.markAllAsRead(customerId);
        return ResponseEntity.ok().build();
    }

    /**
     * Xóa thông báo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        Integer customerId = getCurrentCustomerId();
        notificationService.deleteNotification(id, customerId);
        return ResponseEntity.noContent().build();
    }

    // Helper methods
    private Integer getCurrentCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        var customer = customerService.findByEmail(email);
        if (customer == null) {
            throw new RuntimeException("Không tìm thấy khách hàng");
        }

        return customer.getId();
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .typeDisplay(notification.getType().getDisplayName())
                .icon(notification.getType().getIcon())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
