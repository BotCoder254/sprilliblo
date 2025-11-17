package com.blog.blog_backend.repository;

import com.blog.blog_backend.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    Page<Post> findByTenantIdOrderByUpdatedAtDesc(String tenantId, Pageable pageable);

    Page<Post> findByTenantIdAndStatusOrderByUpdatedAtDesc(String tenantId, Post.PostStatus status, Pageable pageable);

    Optional<Post> findByTenantIdAndSlug(String tenantId, String slug);

    Optional<Post> findByTenantIdAndId(String tenantId, String id);

    boolean existsByTenantIdAndSlug(String tenantId, String slug);

    List<Post> findByTenantIdAndAuthorIdOrderByUpdatedAtDesc(String tenantId, String authorId);

    long countByTenantId(String tenantId);

    long countByTenantIdAndStatus(String tenantId, Post.PostStatus status);

    // Public queries
    Page<Post> findByTenantIdAndStatusOrderByPublishedAtDesc(String tenantId, Post.PostStatus status, Pageable pageable);

    Page<Post> findByTenantIdAndStatusAndTitleContainingIgnoreCaseOrderByPublishedAtDesc(String tenantId, Post.PostStatus status, String title, Pageable pageable);

    Page<Post> findByTenantIdAndStatusAndTagsContainingOrderByPublishedAtDesc(String tenantId, Post.PostStatus status, String tag, Pageable pageable);

    Page<Post> findByTenantIdAndStatusAndAuthorContainingIgnoreCaseOrderByPublishedAtDesc(String tenantId, Post.PostStatus status, String author, Pageable pageable);

    Optional<Post> findByTenantIdAndSlugAndStatus(String tenantId, String slug, Post.PostStatus status);

    Page<Post> findByTenantIdAndStatusAndSlugNotOrderByPublishedAtDesc(String tenantId, Post.PostStatus status, String slug, Pageable pageable);

    Page<Post> findByTenantIdAndStatusAndTagsInAndSlugNotOrderByPublishedAtDesc(String tenantId, Post.PostStatus status, List<String> tags, String slug, Pageable pageable);

    List<Post> findByTenantIdAndStatus(String tenantId, Post.PostStatus status);
}