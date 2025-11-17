package com.blog.blog_backend.controller;

import com.blog.blog_backend.dto.CommentRequest;
import com.blog.blog_backend.dto.CommentResponse;
import com.blog.blog_backend.model.Comment;
import com.blog.blog_backend.service.CommentService;
import com.blog.blog_backend.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants/{tenantId}")
public class CommentController {

    private final CommentService commentService;
    private final NotificationService notificationService;

    public CommentController(CommentService commentService, NotificationService notificationService) {
        this.commentService = commentService;
        this.notificationService = notificationService;
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable String tenantId,
            @PathVariable String postId,
            @Valid @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {

        String userId = (String) httpRequest.getAttribute("userId");
        CommentResponse comment = commentService.createComment(tenantId, postId, request, userId);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/comments")
    public ResponseEntity<Page<Comment>> getPendingComments(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {

        System.out.println("Getting pending comments for tenant: " + tenantId + ", page: " + page);
        
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            System.out.println("No userId found in request attributes");
            return ResponseEntity.status(401).build();
        }

        System.out.println("User ID: " + userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentService.getPendingComments(tenantId, pageable);
        System.out.println("Found " + comments.getTotalElements() + " pending comments");
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/comments/{commentId}/approve")
    public ResponseEntity<Void> approveComment(
            @PathVariable String tenantId,
            @PathVariable String commentId,
            HttpServletRequest httpRequest) {

        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        commentService.approveComment(tenantId, commentId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/comments/{commentId}/reject")
    public ResponseEntity<Void> rejectComment(
            @PathVariable String tenantId,
            @PathVariable String commentId,
            HttpServletRequest httpRequest) {

        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        commentService.rejectComment(tenantId, commentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String tenantId,
            @PathVariable String commentId,
            HttpServletRequest httpRequest) {

        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        commentService.deleteComment(tenantId, commentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-pending-comment")
    public ResponseEntity<String> createTestPendingComment(
            @PathVariable String tenantId,
            HttpServletRequest httpRequest) {

        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        // Create a test pending comment
        CommentRequest testRequest = new CommentRequest();
        testRequest.setAuthorName("Test User");
        testRequest.setAuthorEmail("test@example.com");
        testRequest.setBody("This is a test pending comment for moderation.");
        testRequest.setHoneypot("");

        // Force it to be pending by passing null userId
        CommentResponse comment = commentService.createComment(tenantId, "test-post-id", testRequest, null);
        return ResponseEntity.ok("Created test pending comment: " + comment.getId());
    }

    @PostMapping("/test-notifications")
    public ResponseEntity<String> createTestNotifications(
            @PathVariable String tenantId,
            HttpServletRequest httpRequest) {

        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            // Create test notifications
            notificationService.createCommentNotification(
                    tenantId, userId, "Test Post Title", "Test Commenter", "test-post-slug"
            );
            
            notificationService.createPostPublishedNotification(
                    tenantId, userId, "Test Published Post", "test-published-slug"
            );
            
            return ResponseEntity.ok("Created test notifications");
        } catch (Exception e) {
            return ResponseEntity.ok("Error creating notifications: " + e.getMessage());
        }
    }
}