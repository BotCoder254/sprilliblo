package com.blog.blog_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.blog.blog_backend.model.Notification;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String userId;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private String actionUrl;
    private Map<String, Object> payload;
}