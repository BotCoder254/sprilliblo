package com.blog.blog_backend.controller;

import com.blog.blog_backend.dto.PostRequest;
import com.blog.blog_backend.dto.PostResponse;
import com.blog.blog_backend.model.Post;
import com.blog.blog_backend.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants/{tenantId}/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @PathVariable String tenantId,
            @Valid @RequestBody PostRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            PostResponse response = postService.createPost(tenantId, userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Post.PostStatus status,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String author) {
        try {
            Page<PostResponse> posts = postService.getPosts(tenantId, page, size, status, tag, category, author);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable String tenantId,
            @PathVariable String postId) {
        try {
            PostResponse post = postService.getPost(tenantId, postId);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(
            @PathVariable String tenantId,
            @PathVariable String slug) {
        try {
            PostResponse post = postService.getPostBySlug(tenantId, slug);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String tenantId,
            @PathVariable String postId,
            @Valid @RequestBody PostRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            PostResponse response = postService.updatePost(tenantId, postId, userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String tenantId,
            @PathVariable String postId,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }

            postService.deletePost(tenantId, postId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/check-slug/{slug}")
    public ResponseEntity<Map<String, Object>> checkSlugAvailability(
            @PathVariable String tenantId,
            @PathVariable String slug) {
        try {
            List<String> suggestions = postService.generateSlugSuggestions(tenantId, slug);
            boolean available = suggestions.isEmpty();
            Map<String, Object> response = Map.of(
                    "available", available,
                    "suggestions", suggestions
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}