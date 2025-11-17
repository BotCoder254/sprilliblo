package com.blog.blog_backend.controller;

import com.blog.blog_backend.model.Post;
import com.blog.blog_backend.model.Tenant;
import com.blog.blog_backend.repository.PostRepository;
import com.blog.blog_backend.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/tenants")
    public ResponseEntity<List<Tenant>> getAllTenants() {
        return ResponseEntity.ok(tenantRepository.findAll());
    }

    @GetMapping("/tenants/{slug}")
    public ResponseEntity<Tenant> getTenantBySlug(@PathVariable String slug) {
        return tenantRepository.findBySlug(slug)
                .map(tenant -> ResponseEntity.ok(tenant))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tenants/{tenantId}/posts")
    public ResponseEntity<List<Post>> getPostsByTenant(@PathVariable String tenantId) {
        List<Post> posts = postRepository.findByTenantIdAndStatus(tenantId, Post.PostStatus.PUBLISHED);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/all")
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postRepository.findAll());
    }

    @GetMapping("/posts/published")
    public ResponseEntity<Map<String, Object>> getPublishedPostsCount() {
        List<Post> allPosts = postRepository.findAll();
        long publishedCount = allPosts.stream()
                .filter(post -> post.getStatus() == Post.PostStatus.PUBLISHED)
                .count();
        
        return ResponseEntity.ok(Map.of(
                "totalPosts", allPosts.size(),
                "publishedPosts", publishedCount,
                "posts", allPosts
        ));
    }
}