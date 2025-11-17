package com.blog.blog_backend.service;

import com.blog.blog_backend.dto.CommentRequest;
import com.blog.blog_backend.dto.CommentResponse;
import com.blog.blog_backend.model.Comment;
import com.blog.blog_backend.model.Post;
import com.blog.blog_backend.repository.CommentRepository;
import com.blog.blog_backend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Autowired
    private NotificationService notificationService;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public CommentResponse createComment(String tenantId, String postId, CommentRequest request, String userId) {
        // Spam protection
        if (request.getHoneypot() != null && !request.getHoneypot().isEmpty()) {
            throw new RuntimeException("Spam detected");
        }

        Comment comment = new Comment();
        comment.setTenantId(tenantId);
        comment.setPostId(postId);
        comment.setAuthorName(request.getAuthorName().trim());
        comment.setAuthorEmail(request.getAuthorEmail().trim().toLowerCase());
        comment.setBody(sanitizeComment(request.getBody()));

        if (userId != null) {
            comment.setAuthorId(userId);
            comment.setStatus(Comment.CommentStatus.APPROVED); // Auto-approve for logged-in users
        } else {
            comment.setStatus(Comment.CommentStatus.PENDING); // Moderate anonymous comments
        }

        comment = commentRepository.save(comment);

        // Create notification for post author
        try {
            Optional<Post> post = postRepository.findById(postId);
            if (post.isPresent() && post.get().getAuthorId() != null) {
                notificationService.createCommentNotification(
                        tenantId,
                        post.get().getAuthorId(),
                        post.get().getTitle(),
                        comment.getAuthorName(),
                        post.get().getSlug()
                );
            }
        } catch (Exception e) {
            // Log error but don't fail comment creation
            System.err.println("Failed to create comment notification: " + e.getMessage());
        }

        return mapToResponse(comment);
    }

    public List<CommentResponse> getApprovedComments(String tenantId, String postId) {
        List<Comment> comments = commentRepository.findByTenantIdAndPostIdAndStatusOrderByCreatedAtAsc(
                tenantId, postId, Comment.CommentStatus.APPROVED);
        return comments.stream().map(this::mapToResponse).toList();
    }

    public Page<Comment> getPendingComments(String tenantId, Pageable pageable) {
        return commentRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(
                tenantId, Comment.CommentStatus.PENDING, pageable);
    }

    public void approveComment(String tenantId, String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized");
        }

        comment.setStatus(Comment.CommentStatus.APPROVED);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public void rejectComment(String tenantId, String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized");
        }

        comment.setStatus(Comment.CommentStatus.REJECTED);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public void deleteComment(String tenantId, String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized");
        }

        commentRepository.delete(comment);
    }

    private String sanitizeComment(String body) {
        if (body == null) return "";

        return body.trim()
                .replaceAll("<[^>]*>", "") // Remove HTML tags
                .replaceAll("\\s+", " "); // Normalize whitespace
    }

    private CommentResponse mapToResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthorName(),
                comment.getBody(),
                comment.getStatus(),
                comment.getCreatedAt()
        );
    }
}