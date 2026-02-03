package com.retailmanagement.dto.response;

import com.retailmanagement.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String typeDisplay;
    private String icon;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}