package com.blog.blog_backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
@CompoundIndex(def = "{'tenantId': 1, 'userId': 1, 'createdAt': -1}")
public class Notification {
    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String userId;

    private NotificationType type;
    private String title;
    private String message;
    private String actionUrl;
    private Map<String, Object> payload;

    @Indexed
    private boolean read = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime readAt;

    public enum NotificationType {
        COMMENT_REPLY,
        COMMENT_APPROVED,
        COMMENT_REJECTED,
        POST_PUBLISHED,
        POST_LIKED,
        USER_INVITED,
        USER_JOINED,
        SYSTEM_ANNOUNCEMENT,
        MEDIA_UPLOADED,
        TENANT_UPDATED
    }
}