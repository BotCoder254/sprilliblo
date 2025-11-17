package com.blog.blog_backend.repository;

import com.blog.blog_backend.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByTenantIdAndPostIdAndStatusOrderByCreatedAtAsc(String tenantId, String postId, Comment.CommentStatus status);

    Page<Comment> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, Comment.CommentStatus status, Pageable pageable);

    List<Comment> findByTenantIdAndStatus(String tenantId, Comment.CommentStatus status);

    long countByTenantIdAndStatus(String tenantId, Comment.CommentStatus status);
    
    List<Comment> findByTenantId(String tenantId);
}