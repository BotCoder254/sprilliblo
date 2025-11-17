package com.blog.blog_backend.controller;

import com.blog.blog_backend.dto.CommentRequest;
import com.blog.blog_backend.dto.CommentResponse;
import com.blog.blog_backend.model.Post;
import com.blog.blog_backend.model.Tenant;
import com.blog.blog_backend.service.CommentService;
import com.blog.blog_backend.service.PostService;
import com.blog.blog_backend.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/tenants/{tenantSlug}")
public class PublicCommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private TenantService tenantService;

    @GetMapping("/posts/{slug}/comments")
    public ResponseEntity<List<CommentResponse>> getPostComments(
            @PathVariable String tenantSlug,
            @PathVariable String slug) {

        System.out.println("Getting comments for tenant: " + tenantSlug + ", post: " + slug);

        Optional<Tenant> tenant = tenantService.findBySlug(tenantSlug);
        if (tenant.isEmpty()) {
            System.out.println("Tenant not found: " + tenantSlug);
            return ResponseEntity.notFound().build();
        }

        Optional<Post> post = postService.findPublishedBySlug(tenant.get().getId(), slug);
        if (post.isEmpty()) {
            System.out.println("Post not found: " + slug);
            return ResponseEntity.notFound().build();
        }

        List<CommentResponse> comments = commentService.getApprovedComments(tenant.get().getId(), post.get().getId());
        System.out.println("Found " + comments.size() + " approved comments");

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(2)))
                .body(comments);
    }

    @PostMapping("/approve-all-comments")
    public ResponseEntity<String> approveAllComments(@PathVariable String tenantSlug) {
        Optional<Tenant> tenant = tenantService.findBySlug(tenantSlug);
        if (tenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        int approvedCount = commentService.approveAllPendingComments(tenant.get().getId());
        return ResponseEntity.ok("Approved " + approvedCount + " comments");
    }

    @PostMapping("/posts/{slug}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable String tenantSlug,
            @PathVariable String slug,
            @Valid @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {

        System.out.println("Creating comment for tenant: " + tenantSlug + ", post: " + slug);
        System.out.println("Request body: " + request.getAuthorName() + ", " + request.getAuthorEmail());

        Optional<Tenant> tenant = tenantService.findBySlug(tenantSlug);
        if (tenant.isEmpty()) {
            System.out.println("Tenant not found: " + tenantSlug);
            return ResponseEntity.notFound().build();
        }

        Optional<Post> post = postService.findPublishedBySlug(tenant.get().getId(), slug);
        if (post.isEmpty()) {
            System.out.println("Post not found: " + slug);
            return ResponseEntity.notFound().build();
        }

        String userId = (String) httpRequest.getAttribute("userId");
        System.out.println("User ID: " + userId);
        
        try {
            CommentResponse comment = commentService.createComment(tenant.get().getId(), post.get().getId(), request, userId);
            System.out.println("Comment created successfully: " + comment.getId());
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            System.err.println("Error creating comment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}