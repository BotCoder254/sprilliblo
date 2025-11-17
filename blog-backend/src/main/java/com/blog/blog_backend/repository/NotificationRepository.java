package com.blog.blog_backend.repository;

import com.blog.blog_backend.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByTenantIdAndUserIdOrderByCreatedAtDesc(String tenantId, String userId, Pageable pageable);

    List<Notification> findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(String tenantId, String userId);

    long countByTenantIdAndUserIdAndReadFalse(String tenantId, String userId);

    List<Notification> findTop10ByTenantIdAndUserIdOrderByCreatedAtDesc(String tenantId, String userId);

    void deleteByTenantIdAndUserId(String tenantId, String userId);
}