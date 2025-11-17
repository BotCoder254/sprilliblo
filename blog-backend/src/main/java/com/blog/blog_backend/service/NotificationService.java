package com.blog.blog_backend.service;

import com.blog.blog_backend.dto.NotificationRequest;
import com.blog.blog_backend.dto.NotificationResponse;
import com.blog.blog_backend.model.Notification;
import com.blog.blog_backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public NotificationResponse createNotification(String tenantId, NotificationRequest request) {
        Notification notification = new Notification();
        notification.setTenantId(tenantId);
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setActionUrl(request.getActionUrl());
        notification.setPayload(request.getPayload());
        notification.setCreatedAt(LocalDateTime.now());

        notification = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        NotificationResponse response = mapToResponse(notification);
        messagingTemplate.convertAndSendToUser(
                request.getUserId(),
                "/queue/notifications",
                response
        );

        return response;
    }

    public void createSystemNotification(String tenantId, String userId, String title, String message, String actionUrl) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.SYSTEM_ANNOUNCEMENT);
        request.setTitle(title);
        request.setMessage(message);
        request.setActionUrl(actionUrl);
        request.setPayload(Map.of("system", true));

        createNotification(tenantId, request);
    }

    public void createCommentNotification(String tenantId, String userId, String postTitle, String commenterName, String postSlug) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.COMMENT_REPLY);
        request.setTitle("New Comment");
        request.setMessage(commenterName + " commented on \"" + postTitle + "\"");
        request.setActionUrl("/posts/" + postSlug + "#comments");
        request.setPayload(Map.of(
                "postSlug", postSlug,
                "commenterName", commenterName,
                "postTitle", postTitle
        ));

        createNotification(tenantId, request);
    }

    public void createPostPublishedNotification(String tenantId, String userId, String postTitle, String postSlug) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.POST_PUBLISHED);
        request.setTitle("Post Published");
        request.setMessage("Your post \"" + postTitle + "\" has been published");
        request.setActionUrl("/posts/" + postSlug);
        request.setPayload(Map.of(
                "postSlug", postSlug,
                "postTitle", postTitle
        ));

        createNotification(tenantId, request);
    }

    public Page<NotificationResponse> getNotifications(String tenantId, String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, userId, pageable);

        return notifications.map(this::mapToResponse);
    }

    public List<NotificationResponse> getRecentNotifications(String tenantId, String userId) {
        List<Notification> notifications = notificationRepository.findTop10ByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, userId);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(String tenantId, String userId) {
        List<Notification> notifications = notificationRepository.findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(tenantId, userId);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String tenantId, String userId) {
        return notificationRepository.countByTenantIdAndUserIdAndReadFalse(tenantId, userId);
    }

    public NotificationResponse markAsRead(String tenantId, String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getTenantId().equals(tenantId) || !notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        return mapToResponse(notification);
    }

    public void markAllAsRead(String tenantId, String userId) {
        List<Notification> unreadNotifications = notificationRepository.findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(tenantId, userId);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteNotification(String tenantId, String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getTenantId().equals(tenantId) || !notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        notificationRepository.delete(notification);
    }

    public void deleteAllNotifications(String tenantId, String userId) {
        notificationRepository.deleteByTenantIdAndUserId(tenantId, userId);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTenantId(),
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getActionUrl(),
                notification.getPayload(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}