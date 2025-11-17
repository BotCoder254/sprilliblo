package com.blog.blog_backend.repository;

import com.blog.blog_backend.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends MongoRepository<Media, String> {
    Page<Media> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
    Page<Media> findByTenantIdAndMimeTypeStartingWithOrderByCreatedAtDesc(String tenantId, String mimeTypePrefix, Pageable pageable);
    List<Media> findByTenantIdAndUploadedByOrderByCreatedAtDesc(String tenantId, String uploadedBy);
    Optional<Media> findByTenantIdAndId(String tenantId, String id);
    long countByTenantId(String tenantId);
}