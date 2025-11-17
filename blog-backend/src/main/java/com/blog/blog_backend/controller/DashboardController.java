package com.blog.blog_backend.controller;

import com.blog.blog_backend.model.Post;
import com.blog.blog_backend.model.Comment;
import com.blog.blog_backend.service.PostService;
import com.blog.blog_backend.service.CommentService;
import com.blog.blog_backend.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.blog.blog_backend.dto.NotificationResponse;

@RestController
@RequestMapping("/api/tenants/{tenantId}/dashboard")
public class DashboardController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @PathVariable String tenantId,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            // Get posts statistics
            List<Post> allPosts = postService.findByTenantId(tenantId);
            long publishedPosts = allPosts.stream()
                    .filter(post -> post.getStatus() == Post.PostStatus.PUBLISHED)
                    .count();
            long draftPosts = allPosts.stream()
                    .filter(post -> post.getStatus() == Post.PostStatus.DRAFT)
                    .count();
            
            // Calculate total views
            long totalViews = allPosts.stream()
                    .filter(post -> post.getStatus() == Post.PostStatus.PUBLISHED)
                    .mapToLong(post -> post.getViews() != null ? post.getViews() : 0)
                    .sum();

            // Get comments statistics
            List<Comment> allComments = commentService.findByTenantId(tenantId);
            long pendingComments = allComments.stream()
                    .filter(comment -> comment.getStatus() == Comment.CommentStatus.PENDING)
                    .count();
            long approvedComments = allComments.stream()
                    .filter(comment -> comment.getStatus() == Comment.CommentStatus.APPROVED)
                    .count();

            // Get notification count
            long unreadNotifications = notificationService.getUnreadCount(tenantId, userId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPosts", allPosts.size());
            stats.put("publishedPosts", publishedPosts);
            stats.put("draftPosts", draftPosts);
            stats.put("totalViews", totalViews);
            stats.put("totalComments", allComments.size());
            stats.put("pendingComments", pendingComments);
            stats.put("approvedComments", approvedComments);
            stats.put("unreadNotifications", unreadNotifications);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<Map<String, Object>> getRecentActivity(
            @PathVariable String tenantId,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            // Get recent published posts (last 10)
            List<Post> recentPosts = postService.findRecentPublished(tenantId, 10);
            
            // Get recent notifications (last 5)
            List<NotificationResponse> recentNotifications = notificationService.getRecentNotifications(tenantId, userId);

            Map<String, Object> activity = new HashMap<>();
            activity.put("recentPosts", recentPosts);
            activity.put("recentNotifications", recentNotifications);

            return ResponseEntity.ok(activity);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}