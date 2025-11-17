package com.blog.blog_backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.blog.blog_backend.model.Notification;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String tenantId;
    private String userId;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private String actionUrl;
    private Map<String, Object> payload;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}